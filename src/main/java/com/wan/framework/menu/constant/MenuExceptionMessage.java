package com.wan.framework.menu.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MenuExceptionMessage {

    NOT_FOUND_PARENT("[ERROR] 상위메뉴를 찾을수 없음"),
    NOT_FOUND_PROGRAM("[ERROR] 프로그램을 찾을수 없음"),
    NOT_FOUND_MENU("[ERROR] 메뉴를 찾을수 없음");

    private final String message;
}
