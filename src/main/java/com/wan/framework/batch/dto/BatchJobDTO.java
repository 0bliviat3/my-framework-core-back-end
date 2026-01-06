package com.wan.framework.batch.dto;

import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 배치 작업 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJobDTO {

    private Long id;
    private String batchId;
    private String batchName;
    private String description;
    private String scheduleType;
    private String scheduleExpression;
    private String proxyApiCode;
    private String executionParameters;
    private Boolean enabled;
    private Integer maxRetryCount;
    private Integer retryIntervalSeconds;
    private Integer timeoutSeconds;
    private Boolean allowConcurrent;
    private LocalDateTime lastExecutedAt;
    private String lastExecutionStatus;
    private LocalDateTime nextExecutionAt;
    private DataStateCode dataState;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
