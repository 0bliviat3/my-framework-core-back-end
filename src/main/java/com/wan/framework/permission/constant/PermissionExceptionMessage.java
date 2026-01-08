package com.wan.framework.permission.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 권한 관리 예외 메시지
 */
@Getter
@RequiredArgsConstructor
public enum PermissionExceptionMessage {
    // 권한 검증
    PERMISSION_DENIED("PERMISSION_001", "접근 권한이 없습니다."),
    API_NOT_FOUND("PERMISSION_002", "API를 찾을 수 없습니다."),
    API_INACTIVE("PERMISSION_003", "비활성화된 API입니다."),

    // Role 관리
    ROLE_NOT_FOUND("PERMISSION_101", "Role을 찾을 수 없습니다."),
    ROLE_ALREADY_EXISTS("PERMISSION_102", "이미 존재하는 Role입니다."),
    CANNOT_DELETE_ADMIN_ROLE("PERMISSION_103", "ADMIN Role은 삭제할 수 없습니다."),

    // API Registry
    API_REGISTRY_SCAN_FAILED("PERMISSION_201", "API 스캔에 실패했습니다.");

    private final String code;
    private final String message;
}
