package com.wan.framework.apikey.repository;

import com.wan.framework.apikey.domain.ApiKeyUsageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ApiKeyUsageHistoryRepository extends JpaRepository<ApiKeyUsageHistory, Long> {

    // API Key ID로 사용 이력 조회 (페이징)
    Page<ApiKeyUsageHistory> findByApiKeyId(Long apiKeyId, Pageable pageable);

    // API Key ID와 기간으로 사용 이력 조회 (리스트)
    List<ApiKeyUsageHistory> findByApiKeyIdAndUsedAtBetween(
            Long apiKeyId, LocalDateTime startDate, LocalDateTime endDate);

    // API Key ID와 성공/실패 여부로 조회 (페이징)
    Page<ApiKeyUsageHistory> findByApiKeyIdAndIsSuccess(
            Long apiKeyId, Boolean isSuccess, Pageable pageable);

    // API Key ID로 총 사용 횟수
    long countByApiKeyId(Long apiKeyId);

    // API Key ID와 성공/실패 여부로 횟수
    long countByApiKeyIdAndIsSuccess(Long apiKeyId, Boolean isSuccess);
}
