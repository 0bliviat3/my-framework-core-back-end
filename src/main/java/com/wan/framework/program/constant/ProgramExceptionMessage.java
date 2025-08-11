package com.wan.framework.program.constant;

import lombok.Getter;

@Getter
public enum ProgramExceptionMessage {

    DUPLICATED_PROGRAM_NAME("[ERROR] 중복되는 프로그램명");

    private final String message;

    ProgramExceptionMessage(final String message) {
        this.message = message;
    }
}
