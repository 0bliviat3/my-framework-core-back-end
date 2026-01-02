package com.wan.framework.apikey.constant;

import lombok.Getter;

@Getter
public enum ApiKeyExceptionMessage {
    // ApiKey
    NOT_FOUND_API_KEY("APIKEY_001", "API Key를 찾을 수 없습니다."),
    INVALID_API_KEY("APIKEY_002", "유효하지 않은 API Key입니다."),
    EXPIRED_API_KEY("APIKEY_003", "만료된 API Key입니다."),
    DISABLED_API_KEY("APIKEY_004", "비활성화된 API Key입니다."),
    DUPLICATE_API_KEY_NAME("APIKEY_005", "이미 존재하는 API Key 이름입니다."),

    // Permission
    PERMISSION_DENIED("APIKEY_010", "권한이 없습니다."),
    DUPLICATE_PERMISSION("APIKEY_011", "이미 존재하는 권한입니다."),

    // Authentication
    MISSING_API_KEY("APIKEY_020", "API Key가 제공되지 않았습니다."),
    MISSING_AUTHORIZATION_HEADER("APIKEY_021", "Authorization 헤더가 없거나 Bearer 형식이 아닙니다."),
    INVALID_BEARER_TOKEN("APIKEY_022", "Bearer 토큰 형식이 올바르지 않습니다.");

    private final String code;
    private final String message;

    ApiKeyExceptionMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
