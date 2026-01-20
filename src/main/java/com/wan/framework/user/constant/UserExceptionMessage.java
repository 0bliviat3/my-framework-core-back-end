package com.wan.framework.user.constant;

import lombok.Getter;

@Getter
public enum UserExceptionMessage {
    FAIL_HASH_PASSWORD("[ERROR] 비밀번호 해싱 오류"),
    USED_ID("[ERROR] 사용중인 id 입니다."),
    FAIL_CREATE_SALT("[ERROR] SALT 생성 오류"),
    INVALID_ID("[ERROR] 유효하지 않은 id"),
    INVALID_PASSWORD("[ERROR] 유효하지 않은 password"),
    ADMIN_ALREADY_EXISTS("[ERROR] 이미 관리자 계정이 존재합니다."),
    INVALID_USER_INFO("[ERROR] 사용자 정보가 유효하지 않습니다."),
    ROLE_NOT_FOUND("[ERROR] 권한을 찾을 수 없습니다."),
    CANNOT_DELETE_ADMIN("[ERROR] 관리자 계정은 삭제할 수 없습니다.")
    ;

    private final String message;

    UserExceptionMessage(final String message) {
        this.message = message;
    }
}
