package com.wan.framework.board.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BoardExceptionMessage {
    DUPLICATED_TITLE("[ERROR] 중복된 게시판 이름입니다."),
    NOT_FOUND_ID("[ERROR] 존재하지 않는 meta id 입니다.")
    ;

    private final String message;
}
