package com.wan.framework.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 배치 실행 이력 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExecutionDTO {

    private Long id;
    private String executionId;
    private Long batchJobId;
    private String batchId;
    private String batchName;
    private String status;
    private String triggerType;
    private Long proxyExecutionHistoryId;
    private String executionParameters;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long executionTimeMs;
    private String errorMessage;
    private String stackTrace;
    private Integer retryCount;
    private String originalExecutionId;
    private String serverInfo;
    private String executedBy;
    private LocalDateTime createdAt;
}
