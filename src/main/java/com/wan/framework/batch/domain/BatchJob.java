package com.wan.framework.batch.domain;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.base.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 배치 작업 정의
 * - 스케줄 기반 배치 작업 메타 정보
 * - Proxy API 연계를 통한 동적 실행
 */
@Entity
@Table(name = "t_batch_job", indexes = {
        @Index(name = "idx_batch_id", columnList = "batch_id"),
        @Index(name = "idx_enabled", columnList = "enabled")
})
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJob extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 배치 식별자 (고유)
     */
    @Column(name = "batch_id", nullable = false, unique = true, length = 100)
    private String batchId;

    /**
     * 배치명
     */
    @Column(nullable = false, length = 200)
    private String batchName;

    /**
     * 설명
     */
    @Column(length = 500)
    private String description;

    /**
     * 스케줄 타입 (CRON, INTERVAL)
     */
    @Column(nullable = false, length = 20)
    private String scheduleType;

    /**
     * 스케줄 표현식
     * - CRON: "0 0 * * * ?" (매 시간)
     * - INTERVAL: "60000" (60초, 밀리초)
     */
    @Column(nullable = false, length = 100)
    private String scheduleExpression;

    /**
     * 실행할 Proxy API 코드
     */
    @Column(nullable = false, length = 100)
    private String proxyApiCode;

    /**
     * 실행 파라미터 (JSON)
     * - Proxy API로 전달할 동적 파라미터
     * - 템플릿 변수 지원: {{executionId}}, {{batchId}}
     */
    @Column(columnDefinition = "TEXT")
    private String executionParameters;

    /**
     * 활성화 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 최대 재시도 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxRetryCount = 3;

    /**
     * 재시도 간격 (초)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retryIntervalSeconds = 60;

    /**
     * 최대 실행 시간 (초)
     * - Timeout 초과 시 강제 실패 처리
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 300;

    /**
     * 동시 실행 허용 여부
     * - false: 이전 실행이 완료되지 않으면 대기
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean allowConcurrent = false;

    /**
     * 마지막 실행 일시
     */
    private LocalDateTime lastExecutedAt;

    /**
     * 마지막 실행 상태
     */
    @Column(length = 20)
    private String lastExecutionStatus;

    /**
     * 다음 실행 예정 일시
     */
    private LocalDateTime nextExecutionAt;

    /**
     * 데이터 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    @Builder.Default
    private DataStateCode dataState = DataStateCode.I;

    // createdBy, createdAt, updatedBy, updatedAt은 BaseAuditEntity에서 상속받아 자동 처리됨

    @PrePersist
    protected void onCreate() {
        if (this.dataState == null) {
            this.dataState = DataStateCode.I;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.dataState == DataStateCode.I) {
            this.dataState = DataStateCode.U;
        }
    }
}
