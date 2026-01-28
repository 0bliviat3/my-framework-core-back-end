package com.wan.framework.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 내부 API 호출 결과
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalApiInvocationResult {

    /**
     * HTTP 상태 코드 (에뮬레이션)
     */
    private Integer statusCode;

    /**
     * 응답 바디 (JSON)
     */
    private String responseBody;

    /**
     * 성공 여부
     */
    private Boolean isSuccess;

    /**
     * 에러 메시지
     */
    private String errorMessage;

    /**
     * 실행 시간 (밀리초)
     */
    private Long executionTimeMs;
}
