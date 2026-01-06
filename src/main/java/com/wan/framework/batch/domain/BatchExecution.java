package com.wan.framework.batch.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 배치 실행 이력
 * - 배치 작업의 모든 실행 기록
 * - 성공/실패 추적
 * - 재시도 관계 추적
 */
@Entity
@Table(name = "t_batch_execution", indexes = {
        @Index(name = "idx_batch_job_id", columnList = "batch_job_id"),
        @Index(name = "idx_batch_id", columnList = "batch_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_start_time", columnList = "start_time"),
        @Index(name = "idx_trigger_type", columnList = "trigger_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 실행 ID (고유, UUID)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String executionId;

    /**
     * 배치 작업 ID (FK)
     */
    @Column(name = "batch_job_id", nullable = false)
    private Long batchJobId;

    /**
     * 배치 ID
     */
    @Column(name = "batch_id", nullable = false, length = 100)
    private String batchId;

    /**
     * 배치명 (스냅샷)
     */
    @Column(nullable = false, length = 200)
    private String batchName;

    /**
     * 실행 상태 (WAIT, RUNNING, SUCCESS, FAIL, RETRY, TIMEOUT)
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * 실행 트리거 (SCHEDULER, MANUAL, RETRY)
     */
    @Column(name = "trigger_type", nullable = false, length = 20)
    private String triggerType;

    /**
     * Proxy API 실행 이력 ID
     */
    private Long proxyExecutionHistoryId;

    /**
     * 실행 파라미터 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String executionParameters;

    /**
     * 시작 시간
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 종료 시간
     */
    private LocalDateTime endTime;

    /**
     * 실행 시간 (밀리초)
     */
    private Long executionTimeMs;

    /**
     * 에러 메시지
     */
    @Column(length = 2000)
    private String errorMessage;

    /**
     * 스택 트레이스
     */
    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * 재시도 횟수 (현재)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 원본 실행 ID (재시도인 경우)
     */
    @Column(length = 50)
    private String originalExecutionId;

    /**
     * 서버 정보 (실행된 서버)
     */
    @Column(length = 100)
    private String serverInfo;

    /**
     * 실행자
     */
    @Column(length = 50)
    private String executedBy;

    /**
     * 생성일시
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
    }
}
