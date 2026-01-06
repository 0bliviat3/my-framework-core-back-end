package com.wan.framework.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Proxy API 실행 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyExecutionRequest {

    /**
     * API 코드 (필수)
     */
    private String apiCode;

    /**
     * 동적 파라미터
     * - URL 템플릿 변수 치환
     * - 요청 바디 템플릿 변수 치환
     * 예: {"userId": "12345", "action": "approve"}
     */
    private Map<String, Object> parameters;

    /**
     * 실행 트리거 (MANUAL, SCHEDULER, BATCH)
     */
    private String executionTrigger;

    /**
     * 실행자
     */
    private String executedBy;
}
