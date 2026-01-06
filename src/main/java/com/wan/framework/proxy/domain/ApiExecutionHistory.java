package com.wan.framework.proxy.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 실행 이력
 * - Proxy API를 통한 모든 호출 이력 기록
 * - 성공/실패 추적
 * - 디버깅 및 모니터링 용도
 */
@Entity
@Table(name = "t_api_execution_history", indexes = {
        @Index(name = "idx_api_endpoint_id", columnList = "api_endpoint_id"),
        @Index(name = "idx_executed_at", columnList = "executed_at"),
        @Index(name = "idx_is_success", columnList = "is_success")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * API 엔드포인트 ID
     */
    @Column(name = "api_endpoint_id", nullable = false)
    private Long apiEndpointId;

    /**
     * API 코드
     */
    @Column(nullable = false, length = 100)
    private String apiCode;

    /**
     * 실행 대상 URL
     */
    @Column(nullable = false, length = 1000)
    private String executedUrl;

    /**
     * HTTP 메서드
     */
    @Column(nullable = false, length = 10)
    private String httpMethod;

    /**
     * 요청 헤더 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String requestHeaders;

    /**
     * 요청 바디
     */
    @Column(columnDefinition = "TEXT")
    private String requestBody;

    /**
     * 응답 상태 코드
     */
    private Integer responseStatusCode;

    /**
     * 응답 헤더 (JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String responseHeaders;

    /**
     * 응답 바디
     */
    @Column(columnDefinition = "TEXT")
    private String responseBody;

    /**
     * 실행 시간 (밀리초)
     */
    private Long executionTimeMs;

    /**
     * 성공 여부
     */
    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess;

    /**
     * 에러 메시지
     */
    @Column(length = 2000)
    private String errorMessage;

    /**
     * 재시도 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retryAttempt = 0;

    /**
     * 실행 트리거 (MANUAL, SCHEDULER, BATCH)
     */
    @Column(length = 20)
    private String executionTrigger;

    /**
     * 실행자
     */
    @Column(length = 50)
    private String executedBy;

    /**
     * 실행 일시
     */
    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate() {
        this.executedAt = LocalDateTime.now();
    }
}
