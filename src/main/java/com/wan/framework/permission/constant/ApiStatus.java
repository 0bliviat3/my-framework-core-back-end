package com.wan.framework.permission.constant;

/**
 * API 상태
 */
public enum ApiStatus {
    ACTIVE("활성"),
    INACTIVE("비활성");

    private final String description;

    ApiStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
