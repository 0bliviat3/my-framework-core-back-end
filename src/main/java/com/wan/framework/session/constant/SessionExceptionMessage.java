package com.wan.framework.session.constant;

import lombok.Getter;

/**
 * 세션 예외 메시지 상수
 */
@Getter
public enum SessionExceptionMessage {
    // 세션 관련
    SESSION_NOT_FOUND("[ERROR] 세션을 찾을 수 없습니다"),
    SESSION_EXPIRED("[ERROR] 세션이 만료되었습니다"),
    SESSION_INVALID("[ERROR] 유효하지 않은 세션입니다"),
    SESSION_CREATE_FAILED("[ERROR] 세션 생성에 실패했습니다"),
    SESSION_DELETE_FAILED("[ERROR] 세션 삭제에 실패했습니다"),

    // 보안 관련
    IP_MISMATCH("[ERROR] IP 주소가 일치하지 않습니다"),
    USER_AGENT_MISMATCH("[ERROR] User-Agent가 일치하지 않습니다"),
    SESSION_HIJACK_DETECTED("[ERROR] 세션 탈취가 감지되었습니다"),

    // 인증 관련
    AUTHENTICATION_FAILED("[ERROR] 인증에 실패했습니다"),
    INVALID_CREDENTIALS("[ERROR] 잘못된 인증 정보입니다"),
    USER_NOT_FOUND("[ERROR] 사용자를 찾을 수 없습니다"),

    // 동시 로그인 관련
    CONCURRENT_SESSION_LIMIT_EXCEEDED("[ERROR] 동시 로그인 세션 수를 초과했습니다"),

    // Redis 관련
    REDIS_CONNECTION_FAILED("[ERROR] Redis 연결에 실패했습니다"),
    REDIS_OPERATION_FAILED("[ERROR] Redis 작업에 실패했습니다");

    private final String message;

    SessionExceptionMessage(final String message) {
        this.message = message;
    }
}
