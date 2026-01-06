package com.wan.framework.proxy.repository;

import com.wan.framework.proxy.domain.ApiExecutionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 실행 이력 Repository
 */
@Repository
public interface ApiExecutionHistoryRepository extends JpaRepository<ApiExecutionHistory, Long> {

    /**
     * API 엔드포인트별 실행 이력 조회 (페이징)
     */
    Page<ApiExecutionHistory> findByApiEndpointIdOrderByExecutedAtDesc(Long apiEndpointId, Pageable pageable);

    /**
     * API 코드별 실행 이력 조회 (페이징)
     */
    Page<ApiExecutionHistory> findByApiCodeOrderByExecutedAtDesc(String apiCode, Pageable pageable);

    /**
     * 기간별 실행 이력 조회
     */
    List<ApiExecutionHistory> findByExecutedAtBetweenOrderByExecutedAtDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 성공/실패별 조회
     */
    Page<ApiExecutionHistory> findByIsSuccessOrderByExecutedAtDesc(Boolean isSuccess, Pageable pageable);

    /**
     * API 엔드포인트별 성공률 통계
     */
    @Query("SELECT " +
            "COUNT(h) as totalCount, " +
            "SUM(CASE WHEN h.isSuccess = true THEN 1 ELSE 0 END) as successCount, " +
            "SUM(CASE WHEN h.isSuccess = false THEN 1 ELSE 0 END) as failureCount, " +
            "AVG(h.executionTimeMs) as avgExecutionTime " +
            "FROM ApiExecutionHistory h " +
            "WHERE h.apiEndpointId = :apiEndpointId")
    Object getExecutionStats(@Param("apiEndpointId") Long apiEndpointId);

    /**
     * 최근 실행 이력 조회
     */
    List<ApiExecutionHistory> findTop10ByApiCodeOrderByExecutedAtDesc(String apiCode);
}
