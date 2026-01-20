package com.wan.framework.history.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorHistoryExceptionMessage {
    NOT_FOUND("ERROR_HISTORY_001", "에러 로그를 찾을 수 없습니다"),
    INVALID_DATE_RANGE("ERROR_HISTORY_002", "유효하지 않은 날짜 범위입니다");

    private final String code;
    private final String message;
}
