package com.wan.framework.permission.constant;

/**
 * 권한 관리 상수
 */
public final class PermissionConstants {

    private PermissionConstants() {
        // Utility class
    }

    // Redis Cache Keys
    public static final String CACHE_ROLE_API_PERMISSION = "ROLE_API_PERMISSION::";
    public static final String CACHE_API_REGISTRY = "API_REGISTRY::";

    // Cache TTL
    public static final long CACHE_TTL_HOURS = 24;

    // System Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Service ID
    public static final String DEFAULT_SERVICE_ID = "framework";
}
