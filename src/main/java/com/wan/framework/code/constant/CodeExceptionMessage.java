package com.wan.framework.code.constant;

import lombok.Getter;

/**
 * 공통코드 예외 메시지 상수
 */
@Getter
public enum CodeExceptionMessage {
    // Code Group
    GROUP_NOT_FOUND("[ERROR] 코드 그룹을 찾을 수 없습니다"),
    GROUP_CODE_DUPLICATE("[ERROR] 이미 존재하는 그룹 코드입니다"),
    GROUP_CODE_EMPTY("[ERROR] 그룹 코드는 필수입니다"),
    GROUP_NAME_EMPTY("[ERROR] 그룹명은 필수입니다"),
    GROUP_HAS_ITEMS("[ERROR] 코드 항목이 존재하는 그룹은 삭제할 수 없습니다"),

    // Code Item
    ITEM_NOT_FOUND("[ERROR] 코드 항목을 찾을 수 없습니다"),
    ITEM_VALUE_DUPLICATE("[ERROR] 그룹 내 중복된 코드 값입니다"),
    ITEM_VALUE_EMPTY("[ERROR] 코드 값은 필수입니다"),
    ITEM_NAME_EMPTY("[ERROR] 코드명은 필수입니다"),

    // Cache
    CACHE_REFRESH_FAILED("[ERROR] 캐시 갱신에 실패했습니다"),
    CACHE_LOAD_FAILED("[ERROR] 캐시 로드에 실패했습니다");

    private final String message;

    CodeExceptionMessage(final String message) {
        this.message = message;
    }
}
