package com.wan.framework.proxy.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.domain.ApiEndpoint;
import com.wan.framework.proxy.dto.ApiEndpointDTO;
import com.wan.framework.proxy.exception.ProxyException;
import com.wan.framework.proxy.mapper.ApiEndpointMapper;
import com.wan.framework.proxy.repository.ApiEndpointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * API 엔드포인트 서비스
 * - API 메타 정보 CRUD
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiEndpointService {

    private final ApiEndpointRepository apiEndpointRepository;
    private final ApiEndpointMapper apiEndpointMapper;

    /**
     * API 엔드포인트 생성
     */
    @Transactional
    public ApiEndpointDTO createApiEndpoint(ApiEndpointDTO dto) {
        log.info("Creating API endpoint: {}", dto.getApiCode());

        // API 코드 중복 체크
        if (apiEndpointRepository.existsByApiCodeAndDataStateNot(dto.getApiCode(), DataStateCode.D)) {
            throw new ProxyException(ProxyExceptionMessage.API_CODE_ALREADY_EXISTS);
        }

        // HTTP 메서드 검증
        validateHttpMethod(dto.getHttpMethod());

        ApiEndpoint entity = apiEndpointMapper.toEntity(dto);
        ApiEndpoint saved = apiEndpointRepository.save(entity);

        log.info("API endpoint created: {} (ID: {})", saved.getApiCode(), saved.getId());
        return apiEndpointMapper.toDto(saved);
    }

    /**
     * API 엔드포인트 수정
     */
    @Transactional
    public ApiEndpointDTO updateApiEndpoint(Long id, ApiEndpointDTO dto) {
        log.info("Updating API endpoint: {}", id);

        ApiEndpoint entity = apiEndpointRepository.findById(id)
                .filter(e -> e.getDataState() != DataStateCode.D)
                .orElseThrow(() -> new ProxyException(ProxyExceptionMessage.API_ENDPOINT_NOT_FOUND));

        // HTTP 메서드 검증
        if (dto.getHttpMethod() != null) {
            validateHttpMethod(dto.getHttpMethod());
        }

        apiEndpointMapper.updateEntityFromDto(dto, entity);
        ApiEndpoint updated = apiEndpointRepository.save(entity);

        log.info("API endpoint updated: {}", id);
        return apiEndpointMapper.toDto(updated);
    }

    /**
     * API 엔드포인트 삭제 (논리적 삭제)
     */
    @Transactional
    public void deleteApiEndpoint(Long id) {
        log.info("Deleting API endpoint: {}", id);

        ApiEndpoint entity = apiEndpointRepository.findById(id)
                .filter(e -> e.getDataState() != DataStateCode.D)
                .orElseThrow(() -> new ProxyException(ProxyExceptionMessage.API_ENDPOINT_NOT_FOUND));

        entity.setDataState(DataStateCode.D);
        apiEndpointRepository.save(entity);

        log.info("API endpoint deleted: {}", id);
    }

    /**
     * API 엔드포인트 조회 (ID)
     */
    public ApiEndpointDTO getApiEndpoint(Long id) {
        ApiEndpoint entity = apiEndpointRepository.findById(id)
                .filter(e -> e.getDataState() != DataStateCode.D)
                .orElseThrow(() -> new ProxyException(ProxyExceptionMessage.API_ENDPOINT_NOT_FOUND));

        return apiEndpointMapper.toDto(entity);
    }

    /**
     * API 엔드포인트 조회 (API 코드)
     */
    public ApiEndpoint getApiEndpointByCode(String apiCode) {
        return apiEndpointRepository.findByApiCodeAndDataStateNot(apiCode, DataStateCode.D)
                .orElseThrow(() -> new ProxyException(ProxyExceptionMessage.API_ENDPOINT_NOT_FOUND));
    }

    /**
     * API 엔드포인트 목록 조회 (전체)
     */
    public Page<ApiEndpointDTO> getAllApiEndpoints(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApiEndpoint> entities = apiEndpointRepository.findByDataStateNot(DataStateCode.D, pageable);

        return entities.map(apiEndpointMapper::toDto);
    }

    /**
     * API 엔드포인트 목록 조회 (활성화만)
     */
    public Page<ApiEndpointDTO> getEnabledApiEndpoints(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApiEndpoint> entities = apiEndpointRepository.findByDataStateNotAndIsEnabledTrue(DataStateCode.D, pageable);

        return entities.map(apiEndpointMapper::toDto);
    }

    /**
     * API 엔드포인트 활성/비활성 토글
     */
    @Transactional
    public ApiEndpointDTO toggleApiEndpoint(Long id) {
        log.info("Toggling API endpoint: {}", id);

        ApiEndpoint entity = apiEndpointRepository.findById(id)
                .filter(e -> e.getDataState() != DataStateCode.D)
                .orElseThrow(() -> new ProxyException(ProxyExceptionMessage.API_ENDPOINT_NOT_FOUND));

        entity.setIsEnabled(!entity.getIsEnabled());
        ApiEndpoint updated = apiEndpointRepository.save(entity);

        log.info("API endpoint toggled: {} -> {}", id, updated.getIsEnabled());
        return apiEndpointMapper.toDto(updated);
    }

    /**
     * HTTP 메서드 검증
     */
    private void validateHttpMethod(String httpMethod) {
        try {
            org.springframework.http.HttpMethod.valueOf(httpMethod.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProxyException(ProxyExceptionMessage.INVALID_HTTP_METHOD);
        }
    }
}
