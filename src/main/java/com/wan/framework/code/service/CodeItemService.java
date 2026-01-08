package com.wan.framework.code.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.code.domain.CodeItem;
import com.wan.framework.code.dto.CodeItemDTO;
import com.wan.framework.code.exception.CodeException;
import com.wan.framework.code.mapper.CodeItemMapper;
import com.wan.framework.code.repository.CodeGroupRepository;
import com.wan.framework.code.repository.CodeItemRepository;
import com.wan.framework.redis.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.base.constant.DataStateCode.U;
import static com.wan.framework.code.constant.CodeExceptionMessage.*;

/**
 * 공통코드 항목 서비스
 * - Redis Hash 구조로 그룹별 코드 캐싱
 * - 빠른 조회 성능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeItemService {

    private final CodeItemRepository codeItemRepository;
    private final CodeGroupRepository codeGroupRepository;
    private final CodeItemMapper codeItemMapper;
    private final RedisCacheService redisCacheService;
    private final CodeCacheSyncService codeCacheSyncService;

    private static final String CACHE_PREFIX = "CODE:ITEMS:";
    private static final long CACHE_TTL_SECONDS = 3600; // 1시간

    /**
     * 코드 항목 생성
     */
    @Transactional
    public CodeItemDTO createCodeItem(CodeItemDTO dto) {
        log.info("Creating code item: {}-{}", dto.getGroupCode(), dto.getCodeValue());

        // 검증
        validateCodeItem(dto);

        // 그룹 존재 확인
        if (!codeGroupRepository.existsByGroupCodeAndDataStateNot(dto.getGroupCode(), D)) {
            throw new CodeException(GROUP_NOT_FOUND);
        }

        // 중복 체크
        if (codeItemRepository.existsByGroupCodeAndCodeValueAndDataStateNot(
                dto.getGroupCode(), dto.getCodeValue(), D)) {
            throw new CodeException(ITEM_VALUE_DUPLICATE);
        }

        // 저장
        CodeItem entity = codeItemMapper.toEntity(dto);
        CodeItem saved = codeItemRepository.save(entity);

        // 캐시 갱신 (그룹별)
        invalidateGroupCache(dto.getGroupCode());

        log.info("Code item created: {}", saved.getId());
        return codeItemMapper.toDto(saved);
    }

    /**
     * 코드 항목 수정
     */
    @Transactional
    public CodeItemDTO updateCodeItem(Long id, CodeItemDTO dto) {
        log.info("Updating code item: {}", id);

        CodeItem entity = codeItemRepository.findByIdAndDataStateNot(id, D)
                .orElseThrow(() -> new CodeException(ITEM_NOT_FOUND));

        // 업데이트
        entity.setCodeName(dto.getCodeName());
        entity.setDescription(dto.getDescription());
        entity.setEnabled(dto.getEnabled());
        entity.setSortOrder(dto.getSortOrder());
        entity.setAttribute1(dto.getAttribute1());
        entity.setAttribute2(dto.getAttribute2());
        entity.setAttribute3(dto.getAttribute3());
        entity.setDataState(U);

        CodeItem updated = codeItemRepository.save(entity);

        // 캐시 갱신
        invalidateGroupCache(entity.getGroupCode());

        log.info("Code item updated: {}", id);
        return codeItemMapper.toDto(updated);
    }

    /**
     * 코드 항목 삭제 (논리 삭제)
     */
    @Transactional
    public CodeItemDTO deleteCodeItem(Long id) {
        log.info("Deleting code item: {}", id);

        CodeItem entity = codeItemRepository.findByIdAndDataStateNot(id, D)
                .orElseThrow(() -> new CodeException(ITEM_NOT_FOUND));

        // 논리 삭제
        entity.setDataState(D);
        CodeItem deleted = codeItemRepository.save(entity);

        // 캐시 갱신
        invalidateGroupCache(entity.getGroupCode());

        log.info("Code item deleted: {}", id);
        return codeItemMapper.toDto(deleted);
    }

    /**
     * 코드 항목 조회 (단건)
     */
    @Transactional(readOnly = true)
    public CodeItemDTO getCodeItem(Long id) {
        log.debug("Getting code item: {}", id);

        CodeItem entity = codeItemRepository.findByIdAndDataStateNot(id, D)
                .orElseThrow(() -> new CodeException(ITEM_NOT_FOUND));

        return codeItemMapper.toDto(entity);
    }

    /**
     * 그룹별 코드 조회 - 캐시 우선
     */
    @Transactional(readOnly = true)
    public List<CodeItemDTO> getCodeItemsByGroup(String groupCode) {
        log.debug("Getting code items by group: {}", groupCode);

        // 캐시 조회
        String cacheKey = CACHE_PREFIX + groupCode;
        @SuppressWarnings("unchecked")
        List<CodeItemDTO> cached = redisCacheService.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            log.debug("Cache hit for group: {}", groupCode);
            return cached;
        }

        // DB 조회
        List<CodeItemDTO> result = codeItemRepository
                .findAllByGroupCodeAndDataStateNotOrderBySortOrder(groupCode, D)
                .stream()
                .map(codeItemMapper::toDto)
                .collect(Collectors.toList());

        // 캐시 저장
        redisCacheService.set(cacheKey, result, CACHE_TTL_SECONDS);

        return result;
    }

    /**
     * 그룹별 활성화된 코드 조회
     */
    @Transactional(readOnly = true)
    public List<CodeItemDTO> getEnabledCodeItemsByGroup(String groupCode) {
        log.debug("Getting enabled code items by group: {}", groupCode);

        return codeItemRepository
                .findAllByGroupCodeAndEnabledTrueAndDataStateNotOrderBySortOrder(groupCode, D)
                .stream()
                .map(codeItemMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 전체 코드 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<CodeItemDTO> getAllCodeItems(Pageable pageable) {
        log.debug("Getting all code items with paging");
        return codeItemRepository.findAllByDataStateNot(D, pageable)
                .map(codeItemMapper::toDto);
    }

    /**
     * 그룹별 코드 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<CodeItemDTO> getCodeItemsByGroup(String groupCode, Pageable pageable) {
        log.debug("Getting code items by group with paging: {}", groupCode);
        return codeItemRepository.findAllByGroupCodeAndDataStateNot(groupCode, D, pageable)
                .map(codeItemMapper::toDto);
    }

    /**
     * 코드명으로 검색
     */
    @Transactional(readOnly = true)
    public Page<CodeItemDTO> searchCodeItemsByName(String codeName, Pageable pageable) {
        log.debug("Searching code items by name: {}", codeName);
        return codeItemRepository.findAllByCodeNameContainingAndDataStateNot(codeName, D, pageable)
                .map(codeItemMapper::toDto);
    }

    /**
     * 그룹 코드와 코드 값으로 조회
     */
    @Transactional(readOnly = true)
    public CodeItemDTO getCodeItemByValue(String groupCode, String codeValue) {
        log.debug("Getting code item by value: {}-{}", groupCode, codeValue);

        CodeItem entity = codeItemRepository
                .findByGroupCodeAndCodeValueAndDataStateNot(groupCode, codeValue, D)
                .orElseThrow(() -> new CodeException(ITEM_NOT_FOUND));

        return codeItemMapper.toDto(entity);
    }

    /**
     * 코드 항목 활성화/비활성화 토글
     */
    @Transactional
    public CodeItemDTO toggleCodeItem(Long id) {
        log.info("Toggling code item: {}", id);

        CodeItem entity = codeItemRepository.findByIdAndDataStateNot(id, D)
                .orElseThrow(() -> new CodeException(ITEM_NOT_FOUND));

        boolean newState = !entity.getEnabled();
        entity.setEnabled(newState);
        entity.setDataState(U);

        CodeItem updated = codeItemRepository.save(entity);

        // 캐시 갱신
        invalidateGroupCache(entity.getGroupCode());

        log.info("Code item toggled: {} -> {}", id, newState);
        return codeItemMapper.toDto(updated);
    }

    /**
     * 그룹별 코드 캐시 갱신
     */
    @Transactional(readOnly = true)
    public void refreshGroupCache(String groupCode) {
        log.info("Refreshing cache for group: {}", groupCode);

        try {
            List<CodeItemDTO> items = codeItemRepository
                    .findAllByGroupCodeAndDataStateNotOrderBySortOrder(groupCode, D)
                    .stream()
                    .map(codeItemMapper::toDto)
                    .collect(Collectors.toList());

            String cacheKey = CACHE_PREFIX + groupCode;
            redisCacheService.set(cacheKey, items, CACHE_TTL_SECONDS);

            log.info("Cache refreshed for group: {}, {} items", groupCode, items.size());
        } catch (Exception e) {
            log.error("Failed to refresh cache for group: {}", groupCode, e);
            throw new CodeException(CACHE_REFRESH_FAILED, e);
        }
    }

    /**
     * 전체 코드 캐시 갱신
     * @return 그룹별 캐시 갱신 결과 (그룹코드 -> 성공여부)
     */
    @Transactional(readOnly = true)
    public Map<String, Boolean> refreshAllCache() {
        log.info("Refreshing all code item cache");

        Map<String, Boolean> results = new HashMap<>();

        try {
            // 모든 그룹 코드 조회
            List<String> groupCodes = codeGroupRepository.findAllByDataStateNot(D)
                    .stream()
                    .map(group -> group.getGroupCode())
                    .collect(Collectors.toList());

            // 각 그룹별로 캐시 갱신
            groupCodes.forEach(groupCode -> {
                try {
                    refreshGroupCache(groupCode);
                    results.put(groupCode, true);
                } catch (Exception e) {
                    log.error("Failed to refresh cache for group: {}", groupCode, e);
                    results.put(groupCode, false);
                }
            });

            long successCount = results.values().stream().filter(Boolean::booleanValue).count();
            long failCount = results.size() - successCount;

            log.info("All code item cache refreshed: {} success, {} failed out of {} groups",
                    successCount, failCount, groupCodes.size());

            if (failCount > 0) {
                log.warn("Failed groups: {}",
                        results.entrySet().stream()
                                .filter(entry -> !entry.getValue())
                                .map(Map.Entry::getKey)
                                .collect(Collectors.joining(", ")));
            }

        } catch (Exception e) {
            log.error("Failed to refresh all code item cache", e);
            throw new CodeException(CACHE_REFRESH_FAILED, e);
        }

        return results;
    }

    /**
     * 그룹별 코드를 Map으로 반환 (codeValue -> CodeItemDTO)
     */
    @Transactional(readOnly = true)
    public Map<String, CodeItemDTO> getCodeItemsAsMap(String groupCode) {
        log.debug("Getting code items as map for group: {}", groupCode);

        return getCodeItemsByGroup(groupCode).stream()
                .collect(Collectors.toMap(
                        CodeItemDTO::getCodeValue,
                        item -> item,
                        (existing, replacement) -> existing
                ));
    }

    // ==================== Private Helper Methods ====================

    /**
     * 코드 항목 검증
     */
    private void validateCodeItem(CodeItemDTO dto) {
        if (dto.getCodeValue() == null || dto.getCodeValue().isBlank()) {
            throw new CodeException(ITEM_VALUE_EMPTY);
        }
        if (dto.getCodeName() == null || dto.getCodeName().isBlank()) {
            throw new CodeException(ITEM_NAME_EMPTY);
        }
        if (dto.getGroupCode() == null || dto.getGroupCode().isBlank()) {
            throw new CodeException(GROUP_CODE_EMPTY);
        }
    }

    /**
     * 그룹 캐시 무효화 (모든 서버에 전파)
     */
    private void invalidateGroupCache(String groupCode) {
        String cacheKey = CACHE_PREFIX + groupCode;
        redisCacheService.delete(cacheKey);

        // 모든 서버에 캐시 무효화 메시지 전송
        codeCacheSyncService.invalidateItemCacheOnAllServers(groupCode);
    }
}
