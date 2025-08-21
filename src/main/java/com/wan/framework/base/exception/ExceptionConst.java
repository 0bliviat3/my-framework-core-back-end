package com.wan.framework.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionConst {
    USER_ERROR("100", "회원정보 관련 오류"),
    MENU_ERROR("200", "메뉴관리 오류"),
    PROGRAM_ERROR("300", "프로그램 관리 오류"),
    INVALID_ERROR("900", "미정의 오류: 관리자에게 문의하세요");

    private final String code;
    private final String message;
}
