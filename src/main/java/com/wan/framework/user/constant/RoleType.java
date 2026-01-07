package com.wan.framework.user.constant;

/**
 * 사용자 역할 타입
 */
public enum RoleType {
    ROLE_USER("일반 사용자"),
    ROLE_ADMIN("관리자"),
    ROLE_MANAGER("매니저");

    private final String description;

    RoleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 역할명에서 ROLE_ prefix 제거
     */
    public String getSimpleName() {
        return this.name().substring(5);  // "ROLE_" 제거
    }
}
