package com.wan.framework.batch.repository;

import com.wan.framework.batch.domain.BatchExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 배치 실행 이력 Repository
 */
@Repository
public interface BatchExecutionRepository extends JpaRepository<BatchExecution, Long> {

    /**
     * 실행 ID로 조회
     */
    Optional<BatchExecution> findByExecutionId(String executionId);

    /**
     * 배치 작업별 실행 이력 조회 (페이징)
     */
    Page<BatchExecution> findByBatchJobIdOrderByStartTimeDesc(Long batchJobId, Pageable pageable);

    /**
     * 배치 ID별 실행 이력 조회 (페이징)
     */
    Page<BatchExecution> findByBatchIdOrderByStartTimeDesc(String batchId, Pageable pageable);

    /**
     * 상태별 실행 이력 조회 (페이징)
     */
    Page<BatchExecution> findByStatusOrderByStartTimeDesc(String status, Pageable pageable);

    /**
     * 트리거 타입별 실행 이력 조회 (페이징)
     */
    Page<BatchExecution> findByTriggerTypeOrderByStartTimeDesc(String triggerType, Pageable pageable);

    /**
     * 기간별 실행 이력 조회
     */
    List<BatchExecution> findByStartTimeBetweenOrderByStartTimeDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * 특정 배치의 실행 중인 작업 조회
     */
    Optional<BatchExecution> findByBatchIdAndStatus(String batchId, String status);

    /**
     * 배치별 최근 실행 이력 조회
     */
    List<BatchExecution> findTop10ByBatchIdOrderByStartTimeDesc(String batchId);

    /**
     * 재시도 대상 조회 (실패 상태 + 재시도 횟수 미만)
     */
    @Query("SELECT e FROM BatchExecution e " +
           "WHERE e.status = :failStatus " +
           "AND e.retryCount < (SELECT b.maxRetryCount FROM BatchJob b WHERE b.batchId = e.batchId) " +
           "AND e.originalExecutionId IS NULL " +
           "ORDER BY e.startTime DESC")
    List<BatchExecution> findRetryTargets(@Param("failStatus") String failStatus);

    /**
     * 배치별 성공률 통계
     */
    @Query("SELECT " +
           "COUNT(e) as totalCount, " +
           "SUM(CASE WHEN e.status = 'SUCCESS' THEN 1 ELSE 0 END) as successCount, " +
           "SUM(CASE WHEN e.status = 'FAIL' THEN 1 ELSE 0 END) as failureCount, " +
           "AVG(e.executionTimeMs) as avgExecutionTime " +
           "FROM BatchExecution e " +
           "WHERE e.batchJobId = :batchJobId")
    Object getExecutionStats(@Param("batchJobId") Long batchJobId);
}
