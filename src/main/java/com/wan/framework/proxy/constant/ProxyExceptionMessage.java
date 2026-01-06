package com.wan.framework.proxy.constant;

import lombok.Getter;

/**
 * Proxy API 예외 메시지
 */
@Getter
public enum ProxyExceptionMessage {

    // API 엔드포인트 관련
    API_ENDPOINT_NOT_FOUND("PROXY_001", "API 엔드포인트를 찾을 수 없습니다."),
    API_ENDPOINT_DISABLED("PROXY_002", "비활성화된 API 엔드포인트입니다."),
    API_CODE_ALREADY_EXISTS("PROXY_003", "이미 존재하는 API 코드입니다."),
    INVALID_HTTP_METHOD("PROXY_004", "유효하지 않은 HTTP 메서드입니다."),
    INVALID_URL_FORMAT("PROXY_005", "유효하지 않은 URL 형식입니다."),

    // API 실행 관련
    API_EXECUTION_FAILED("PROXY_006", "API 실행에 실패했습니다."),
    API_TIMEOUT("PROXY_007", "API 실행 시간이 초과되었습니다."),
    API_CONNECTION_FAILED("PROXY_008", "API 서버에 연결할 수 없습니다."),
    INVALID_RESPONSE("PROXY_009", "유효하지 않은 응답입니다."),

    // 파라미터 관련
    MISSING_REQUIRED_PARAMETER("PROXY_010", "필수 파라미터가 누락되었습니다."),
    INVALID_PARAMETER_FORMAT("PROXY_011", "파라미터 형식이 올바르지 않습니다."),
    TEMPLATE_PARSING_FAILED("PROXY_012", "템플릿 파싱에 실패했습니다."),

    // 재시도 관련
    MAX_RETRY_EXCEEDED("PROXY_013", "최대 재시도 횟수를 초과했습니다."),

    // 히스토리 관련
    EXECUTION_HISTORY_NOT_FOUND("PROXY_014", "실행 이력을 찾을 수 없습니다.");

    private final String code;
    private final String message;

    ProxyExceptionMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
