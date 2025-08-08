package com.wan.framework.user.constant;

import lombok.Getter;

@Getter
public enum UserExceptionMessage {
    FAIL_HASH_PASSWORD("[ERROR] 비밀번호 해싱 오류"),
    USED_ID("[ERROR] 사용중인 id 입니다."),
    FAIL_CREATE_SALT("[ERROR] SALT 생성 오류"),
    INVALID_ID("[ERROR] 유효하지 않은 id"),
    INVALID_PASSWORD("[ERROR] 유효하지 않은 password")
    ;

    private final String message;

    UserExceptionMessage(final String message) {
        this.message = message;
    }
}
