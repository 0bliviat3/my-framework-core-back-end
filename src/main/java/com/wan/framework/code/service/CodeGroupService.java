package com.wan.framework.code.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.code.constant.CodeExceptionMessage;
import com.wan.framework.code.domain.CodeGroup;
import com.wan.framework.code.dto.CodeGroupDTO;
import com.wan.framework.code.exception.CodeException;
import com.wan.framework.code.mapper.CodeGroupMapper;
import com.wan.framework.code.repository.CodeGroupRepository;
import com.wan.framework.code.repository.CodeItemRepository;
import com.wan.framework.redis.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.base.constant.DataStateCode.U;
import static com.wan.framework.code.constant.CodeExceptionMessage.*;

/**
 * 공통코드 그룹 서비스
 * - Redis 캐시 통합
 * - 그룹 변경 시 자동 캐시 갱신
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGroupService {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeItemRepository codeItemRepository;
    private final CodeGroupMapper codeGroupMapper;
    private final RedisCacheService redisCacheService;

    private static final String CACHE_PREFIX = "CODE:GROUP:";
    private static final String CACHE_ALL_GROUPS = "CODE:ALL_GROUPS";
    private static final long CACHE_TTL_SECONDS = 3600; // 1시간

    /**
     * 코드 그룹 생성
     */
    @Transactional
    public CodeGroupDTO createCodeGroup(CodeGroupDTO dto) {
        log.info("Creating code group: {}", dto.getGroupCode());

        // 검증
        if (dto.getGroupCode() == null || dto.getGroupCode().isBlank()) {
            throw new CodeException(GROUP_CODE_EMPTY);
        }
        if (dto.getGroupName() == null || dto.getGroupName().isBlank()) {
            throw new CodeException(GROUP_NAME_EMPTY);
        }

        // 중복 체크
        if (codeGroupRepository.existsByGroupCodeAndDataStateNot(dto.getGroupCode(), D)) {
            throw new CodeException(GROUP_CODE_DUPLICATE);
        }

        // 저장
        CodeGroup entity = codeGroupMapper.toEntity(dto);
        CodeGroup saved = codeGroupRepository.save(entity);

        // 캐시 저장
        CodeGroupDTO result = codeGroupMapper.toDto(saved);
        cacheCodeGroup(result);
        invalidateAllGroupsCache();

        log.info("Code group created: {}", saved.getGroupCode());
        return result;
    }

    /**
     * 코드 그룹 수정
     */
    @Transactional
    public CodeGroupDTO updateCodeGroup(String groupCode, CodeGroupDTO dto) {
        log.info("Updating code group: {}", groupCode);

        CodeGroup entity = codeGroupRepository.findByGroupCodeAndDataStateNot(groupCode, D)
                .orElseThrow(() -> new CodeException(GROUP_NOT_FOUND));

        // 업데이트
        entity.setGroupName(dto.getGroupName());
        entity.setDescription(dto.getDescription());
        entity.setEnabled(dto.getEnabled());
        entity.setSortOrder(dto.getSortOrder());
        entity.setDataState(U);

        CodeGroup updated = codeGroupRepository.save(entity);

        // 캐시 갱신
        CodeGroupDTO result = codeGroupMapper.toDto(updated);
        cacheCodeGroup(result);
        invalidateAllGroupsCache();

        log.info("Code group updated: {}", groupCode);
        return result;
    }

    /**
     * 코드 그룹 삭제 (논리 삭제)
     */
    @Transactional
    public CodeGroupDTO deleteCodeGroup(String groupCode) {
        log.info("Deleting code group: {}", groupCode);

        CodeGroup entity = codeGroupRepository.findByGroupCodeAndDataStateNot(groupCode, D)
                .orElseThrow(() -> new CodeException(GROUP_NOT_FOUND));

        // 하위 항목 존재 체크
        long itemCount = codeItemRepository.countByGroupCodeAndDataStateNot(groupCode, D);
        if (itemCount > 0) {
            throw new CodeException(GROUP_HAS_ITEMS);
        }

        // 논리 삭제
        entity.setDataState(D);
        CodeGroup deleted = codeGroupRepository.save(entity);

        // 캐시 삭제
        removeCacheByGroupCode(groupCode);
        invalidateAllGroupsCache();

        log.info("Code group deleted: {}", groupCode);
        return codeGroupMapper.toDto(deleted);
    }

    /**
     * 코드 그룹 조회 (단건) - 캐시 우선
     */
    @Transactional(readOnly = true)
    public CodeGroupDTO getCodeGroup(String groupCode) {
        log.debug("Getting code group: {}", groupCode);

        // 캐시 조회
        String cacheKey = CACHE_PREFIX + groupCode;
        CodeGroupDTO cached = redisCacheService.get(cacheKey, CodeGroupDTO.class);
        if (cached != null) {
            log.debug("Cache hit for group: {}", groupCode);
            return cached;
        }

        // DB 조회
        CodeGroup entity = codeGroupRepository.findByGroupCodeAndDataStateNot(groupCode, D)
                .orElseThrow(() -> new CodeException(GROUP_NOT_FOUND));

        CodeGroupDTO result = codeGroupMapper.toDto(entity);

        // 캐시 저장
        cacheCodeGroup(result);

        return result;
    }

    /**
     * 전체 그룹 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<CodeGroupDTO> getAllCodeGroups(Pageable pageable) {
        log.debug("Getting all code groups with paging");
        return codeGroupRepository.findAllByDataStateNot(D, pageable)
                .map(codeGroupMapper::toDto);
    }

    /**
     * 전체 그룹 조회 (목록) - 캐시 우선
     */
    @Transactional(readOnly = true)
    public List<CodeGroupDTO> getAllCodeGroupsList() {
        log.debug("Getting all code groups list");

        // 캐시 조회
        @SuppressWarnings("unchecked")
        List<CodeGroupDTO> cached = redisCacheService.get(CACHE_ALL_GROUPS, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.debug("Cache hit for all groups");
            return cached;
        }

        // DB 조회
        List<CodeGroupDTO> result = codeGroupRepository.findAllByDataStateNot(D)
                .stream()
                .map(codeGroupMapper::toDto)
                .collect(Collectors.toList());

        // 캐시 저장
        redisCacheService.set(CACHE_ALL_GROUPS, result, CACHE_TTL_SECONDS);

        return result;
    }

    /**
     * 활성화된 그룹 조회
     */
    @Transactional(readOnly = true)
    public List<CodeGroupDTO> getEnabledCodeGroups() {
        log.debug("Getting enabled code groups");
        return codeGroupRepository.findAllByEnabledTrueAndDataStateNot(D)
                .stream()
                .map(codeGroupMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 그룹명으로 검색
     */
    @Transactional(readOnly = true)
    public Page<CodeGroupDTO> searchCodeGroupsByName(String groupName, Pageable pageable) {
        log.debug("Searching code groups by name: {}", groupName);
        return codeGroupRepository.findAllByGroupNameContainingAndDataStateNot(groupName, D, pageable)
                .map(codeGroupMapper::toDto);
    }

    /**
     * 그룹 활성화/비활성화 토글
     */
    @Transactional
    public CodeGroupDTO toggleCodeGroup(String groupCode) {
        log.info("Toggling code group: {}", groupCode);

        CodeGroup entity = codeGroupRepository.findByGroupCodeAndDataStateNot(groupCode, D)
                .orElseThrow(() -> new CodeException(GROUP_NOT_FOUND));

        boolean newState = !entity.getEnabled();
        entity.setEnabled(newState);
        entity.setDataState(U);

        CodeGroup updated = codeGroupRepository.save(entity);

        // 캐시 갱신
        CodeGroupDTO result = codeGroupMapper.toDto(updated);
        cacheCodeGroup(result);
        invalidateAllGroupsCache();

        log.info("Code group toggled: {} -> {}", groupCode, newState);
        return result;
    }

    /**
     * 전체 그룹 캐시 갱신
     */
    @Transactional(readOnly = true)
    public void refreshAllCache() {
        log.info("Refreshing all code group cache");

        try {
            // 전체 그룹 조회
            List<CodeGroup> allGroups = codeGroupRepository.findAllByDataStateNot(D);

            // 개별 캐시 저장
            allGroups.forEach(group -> {
                CodeGroupDTO dto = codeGroupMapper.toDto(group);
                cacheCodeGroup(dto);
            });

            // 전체 목록 캐시 저장
            List<CodeGroupDTO> dtoList = allGroups.stream()
                    .map(codeGroupMapper::toDto)
                    .collect(Collectors.toList());
            redisCacheService.set(CACHE_ALL_GROUPS, dtoList, CACHE_TTL_SECONDS);

            log.info("Code group cache refreshed: {} groups", allGroups.size());
        } catch (Exception e) {
            log.error("Failed to refresh code group cache", e);
            throw new CodeException(CACHE_REFRESH_FAILED, e);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * 그룹 캐시 저장
     */
    private void cacheCodeGroup(CodeGroupDTO dto) {
        String cacheKey = CACHE_PREFIX + dto.getGroupCode();
        redisCacheService.set(cacheKey, dto, CACHE_TTL_SECONDS);
    }

    /**
     * 그룹 캐시 삭제
     */
    private void removeCacheByGroupCode(String groupCode) {
        String cacheKey = CACHE_PREFIX + groupCode;
        redisCacheService.delete(cacheKey);
    }

    /**
     * 전체 그룹 목록 캐시 무효화
     */
    private void invalidateAllGroupsCache() {
        redisCacheService.delete(CACHE_ALL_GROUPS);
    }
}
