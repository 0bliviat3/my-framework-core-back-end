package com.wan.framework.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Proxy API 실행 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyExecutionResponse {

    /**
     * 실행 이력 ID
     */
    private Long executionHistoryId;

    /**
     * API 코드
     */
    private String apiCode;

    /**
     * 성공 여부
     */
    private Boolean isSuccess;

    /**
     * HTTP 상태 코드
     */
    private Integer statusCode;

    /**
     * 응답 바디
     */
    private String responseBody;

    /**
     * 에러 메시지 (실패 시)
     */
    private String errorMessage;

    /**
     * 실행 시간 (밀리초)
     */
    private Long executionTimeMs;

    /**
     * 재시도 횟수
     */
    private Integer retryAttempt;

    /**
     * 실행 일시
     */
    private LocalDateTime executedAt;
}
