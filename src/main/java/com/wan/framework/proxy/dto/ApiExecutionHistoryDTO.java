package com.wan.framework.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 실행 이력 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExecutionHistoryDTO {

    private Long id;
    private Long apiEndpointId;
    private String apiCode;
    private String executedUrl;
    private String httpMethod;
    private String requestHeaders;
    private String requestBody;
    private Integer responseStatusCode;
    private String responseHeaders;
    private String responseBody;
    private Long executionTimeMs;
    private Boolean isSuccess;
    private String errorMessage;
    private Integer retryAttempt;
    private String executionTrigger;
    private String executedBy;
    private LocalDateTime executedAt;
}
