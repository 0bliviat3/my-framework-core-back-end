package com.wan.framework.apikey.service;

import com.wan.framework.apikey.domain.ApiKey;
import com.wan.framework.apikey.domain.ApiKeyPermission;
import com.wan.framework.apikey.dto.ApiKeyDTO;
import com.wan.framework.apikey.dto.ApiKeyPermissionDTO;
import com.wan.framework.apikey.exception.ApiKeyException;
import com.wan.framework.apikey.mapper.ApiKeyMapper;
import com.wan.framework.apikey.mapper.ApiKeyPermissionMapper;
import com.wan.framework.apikey.repository.ApiKeyPermissionRepository;
import com.wan.framework.apikey.repository.ApiKeyRepository;
import com.wan.framework.apikey.util.ApiKeyGenerator;
import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.wan.framework.apikey.constant.ApiKeyExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyPermissionRepository apiKeyPermissionRepository;
    private final ApiKeyMapper apiKeyMapper;
    private final ApiKeyPermissionMapper apiKeyPermissionMapper;
    private final ApiKeyGenerator apiKeyGenerator;

    /**
     * API Key 생성
     *
     * @param description API Key 설명
     * @param expiredAt 만료일 (null이면 무제한)
     * @param permissions 권한 목록
     * @param createdBy 생성자
     * @return 생성된 API Key 정보 (실제 키는 이때만 반환됨)
     */
    @Transactional
    public ApiKeyDTO createApiKey(String description, LocalDateTime expiredAt,
                                   List<String> permissions, String createdBy) {
        // 1. API Key 생성
        String rawApiKey = apiKeyGenerator.generateApiKey();
        String hashedApiKey = apiKeyGenerator.hashApiKey(rawApiKey);
        String apiKeyPrefix = apiKeyGenerator.extractPrefix(rawApiKey);

        // 2. Entity 생성
        ApiKey apiKey = ApiKey.builder()
                .apiKey(hashedApiKey)
                .apiKeyPrefix(apiKeyPrefix)
                .description(description)
                .expiredAt(expiredAt)
                .usageCount(0L)
                .ableState(AbleState.ABLE)
                .createdBy(createdBy)
                .build();

        ApiKey saved = apiKeyRepository.save(apiKey);

        // 3. 권한 매핑
        if (permissions != null && !permissions.isEmpty()) {
            permissions.forEach(permission -> {
                ApiKeyPermission perm = ApiKeyPermission.builder()
                        .apiKey(saved)
                        .permission(permission)
                        .createdBy(createdBy)
                        .build();
                apiKeyPermissionRepository.save(perm);
            });
        }

        // 4. DTO 변환 (실제 API Key는 이때만 포함)
        ApiKeyDTO dto = apiKeyMapper.toDto(saved);
        dto.setRawApiKey(rawApiKey); // 최초 생성시에만 반환
        dto.setPermissions(permissions);

        log.info("API Key 생성 완료: prefix={}, createdBy={}", apiKeyPrefix, createdBy);
        return dto;
    }

    /**
     * API Key 검증 및 조회
     *
     * @param rawApiKey 원본 API Key
     * @return API Key 정보
     */
    public ApiKeyDTO validateApiKey(String rawApiKey) {
        // 1. 형식 검증
        if (!apiKeyGenerator.isValidFormat(rawApiKey)) {
            log.warn("유효하지 않은 API Key 형식: {}", rawApiKey);
            throw new ApiKeyException(INVALID_API_KEY);
        }

        // 2. 해시화하여 조회
        String hashedApiKey = apiKeyGenerator.hashApiKey(rawApiKey);
        ApiKey apiKey = apiKeyRepository.findByApiKeyAndDataStateCodeNot(hashedApiKey, DataStateCode.D)
                .orElseThrow(() -> new ApiKeyException(NOT_FOUND_API_KEY));

        // 3. 상태 검증
        if (apiKey.getAbleState() == AbleState.DISABLE) {
            log.warn("비활성화된 API Key: {}", apiKey.getApiKeyPrefix());
            throw new ApiKeyException(DISABLED_API_KEY);
        }

        // 4. 만료 검증
        if (apiKey.isExpired()) {
            log.warn("만료된 API Key: {}, expiredAt={}", apiKey.getApiKeyPrefix(), apiKey.getExpiredAt());
            throw new ApiKeyException(EXPIRED_API_KEY);
        }

        // 5. 권한 조회
        List<String> permissions = apiKeyPermissionRepository.findByApiKey(apiKey)
                .stream()
                .map(ApiKeyPermission::getPermission)
                .collect(Collectors.toList());

        ApiKeyDTO dto = apiKeyMapper.toDto(apiKey);
        dto.setPermissions(permissions);

        return dto;
    }

    /**
     * API Key 활성화/비활성화
     */
    @Transactional
    public void toggleApiKey(Long id, AbleState newState, String updatedBy) {
        ApiKey apiKey = findActiveApiKeyById(id);
        apiKey.setAbleState(newState);
        apiKey.setUpdatedBy(updatedBy);

        log.info("API Key 상태 변경: id={}, state={}, updatedBy={}", id, newState, updatedBy);
    }

    /**
     * API Key 논리 삭제
     */
    @Transactional
    public void deleteApiKey(Long id, String deletedBy) {
        ApiKey apiKey = findActiveApiKeyById(id);
        apiKey.setDataStateCode(DataStateCode.D);
        apiKey.setDeletedAt(LocalDateTime.now());
        apiKey.setUpdatedBy(deletedBy);

        log.info("API Key 논리 삭제: id={}, deletedBy={}", id, deletedBy);
    }

    /**
     * API Key 목록 조회 (페이징)
     */
    public Page<ApiKeyDTO> findAllApiKeys(Pageable pageable) {
        return apiKeyRepository.findByDataStateCodeNotOrderByCreatedAtDesc(DataStateCode.D, pageable)
                .map(apiKey -> {
                    ApiKeyDTO dto = apiKeyMapper.toDto(apiKey);
                    List<String> permissions = apiKeyPermissionRepository.findByApiKey(apiKey)
                            .stream()
                            .map(ApiKeyPermission::getPermission)
                            .collect(Collectors.toList());
                    dto.setPermissions(permissions);
                    return dto;
                });
    }

    /**
     * 내 API Key 목록 조회
     */
    public Page<ApiKeyDTO> findMyApiKeys(String createdBy, Pageable pageable) {
        return apiKeyRepository.findByCreatedByAndDataStateCodeNotOrderByCreatedAtDesc(createdBy, DataStateCode.D, pageable)
                .map(apiKey -> {
                    ApiKeyDTO dto = apiKeyMapper.toDto(apiKey);
                    List<String> permissions = apiKeyPermissionRepository.findByApiKey(apiKey)
                            .stream()
                            .map(ApiKeyPermission::getPermission)
                            .collect(Collectors.toList());
                    dto.setPermissions(permissions);
                    return dto;
                });
    }

    /**
     * API Key 단건 조회
     */
    public ApiKeyDTO findById(Long id) {
        ApiKey apiKey = findActiveApiKeyById(id);
        ApiKeyDTO dto = apiKeyMapper.toDto(apiKey);

        // 권한 목록 조회
        List<String> permissions = apiKeyPermissionRepository.findByApiKey(apiKey)
                .stream()
                .map(ApiKeyPermission::getPermission)
                .collect(Collectors.toList());
        dto.setPermissions(permissions);

        return dto;
    }

    /**
     * 권한 추가
     */
    @Transactional
    public void addPermission(Long apiKeyId, String permission, String createdBy) {
        ApiKey apiKey = findActiveApiKeyById(apiKeyId);

        // 중복 체크
        boolean exists = apiKeyPermissionRepository.existsByApiKeyAndPermission(apiKey, permission);
        if (exists) {
            throw new ApiKeyException(DUPLICATE_PERMISSION);
        }

        ApiKeyPermission perm = ApiKeyPermission.builder()
                .apiKey(apiKey)
                .permission(permission)
                .createdBy(createdBy)
                .build();

        apiKeyPermissionRepository.save(perm);
        log.info("권한 추가: apiKeyId={}, permission={}", apiKeyId, permission);
    }

    /**
     * 권한 제거
     */
    @Transactional
    public void removePermission(Long apiKeyId, String permission) {
        ApiKey apiKey = findActiveApiKeyById(apiKeyId);
        apiKeyPermissionRepository.deleteByApiKeyAndPermission(apiKey, permission);
        log.info("권한 제거: apiKeyId={}, permission={}", apiKeyId, permission);
    }

    /**
     * API Key의 모든 권한 조회
     */
    public List<ApiKeyPermissionDTO> findPermissions(Long apiKeyId) {
        ApiKey apiKey = findActiveApiKeyById(apiKeyId);
        return apiKeyPermissionMapper.toDtoList(apiKeyPermissionRepository.findByApiKey(apiKey));
    }

    /**
     * 사용 횟수 증가
     */
    @Transactional
    public void incrementUsageCount(Long id) {
        ApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ApiKeyException(NOT_FOUND_API_KEY));
        apiKey.incrementUsageCount();
    }

    /**
     * Helper: 활성 상태인 API Key 조회
     */
    private ApiKey findActiveApiKeyById(Long id) {
        return apiKeyRepository.findByIdAndDataStateCodeNot(id, DataStateCode.D)
                .orElseThrow(() -> new ApiKeyException(NOT_FOUND_API_KEY));
    }
}
