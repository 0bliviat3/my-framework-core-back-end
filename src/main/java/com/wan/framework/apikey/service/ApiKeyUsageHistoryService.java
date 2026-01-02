package com.wan.framework.apikey.service;

import com.wan.framework.apikey.domain.ApiKey;
import com.wan.framework.apikey.domain.ApiKeyUsageHistory;
import com.wan.framework.apikey.dto.ApiKeyUsageHistoryDTO;
import com.wan.framework.apikey.mapper.ApiKeyUsageHistoryMapper;
import com.wan.framework.apikey.repository.ApiKeyRepository;
import com.wan.framework.apikey.repository.ApiKeyUsageHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiKeyUsageHistoryService {

    private final ApiKeyUsageHistoryRepository repository;
    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyUsageHistoryMapper mapper;

    /**
     * 사용 이력 기록
     */
    @Transactional
    public void recordUsage(Long apiKeyId, String requestUri, String requestMethod,
                           String ipAddress, String userAgent, Integer responseStatus,
                           Boolean isSuccess, String errorMessage) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElse(null);

        if (apiKey == null) {
            log.warn("API Key를 찾을 수 없음: {}", apiKeyId);
            return;
        }

        ApiKeyUsageHistory history = ApiKeyUsageHistory.builder()
                .apiKey(apiKey)
                .requestUri(requestUri)
                .requestMethod(requestMethod)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .responseStatus(responseStatus)
                .isSuccess(isSuccess)
                .errorMessage(errorMessage)
                .usedAt(LocalDateTime.now())
                .build();

        repository.save(history);
    }

    /**
     * API Key별 사용 이력 조회 (페이징)
     */
    public Page<ApiKeyUsageHistoryDTO> findByApiKeyId(Long apiKeyId, Pageable pageable) {
        return repository.findByApiKeyId(apiKeyId, pageable)
                .map(mapper::toDto);
    }

    /**
     * API Key별 사용 이력 조회 (기간 지정)
     */
    public List<ApiKeyUsageHistoryDTO> findByApiKeyIdAndDateRange(
            Long apiKeyId, LocalDateTime startDate, LocalDateTime endDate) {
        return mapper.toDtoList(
                repository.findByApiKeyIdAndUsedAtBetween(apiKeyId, startDate, endDate)
        );
    }

    /**
     * 성공/실패 여부로 필터링
     */
    public Page<ApiKeyUsageHistoryDTO> findByApiKeyIdAndSuccess(
            Long apiKeyId, Boolean isSuccess, Pageable pageable) {
        return repository.findByApiKeyIdAndIsSuccess(apiKeyId, isSuccess, pageable)
                .map(mapper::toDto);
    }

    /**
     * 전체 사용 이력 조회 (관리자용)
     */
    public Page<ApiKeyUsageHistoryDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDto);
    }

    /**
     * 특정 API Key의 총 사용 횟수
     */
    public Long countByApiKeyId(Long apiKeyId) {
        return repository.countByApiKeyId(apiKeyId);
    }

    /**
     * 특정 API Key의 성공/실패 횟수
     */
    public Long countByApiKeyIdAndSuccess(Long apiKeyId, Boolean isSuccess) {
        return repository.countByApiKeyIdAndIsSuccess(apiKeyId, isSuccess);
    }
}
