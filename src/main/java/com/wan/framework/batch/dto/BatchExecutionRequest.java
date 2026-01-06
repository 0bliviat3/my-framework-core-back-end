package com.wan.framework.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 배치 수동 실행 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchExecutionRequest {

    /**
     * 배치 ID
     */
    private String batchId;

    /**
     * 동적 파라미터 (옵션)
     * - 기본 executionParameters 덮어쓰기
     */
    private Map<String, Object> parameters;

    /**
     * 실행자
     */
    private String executedBy;
}
