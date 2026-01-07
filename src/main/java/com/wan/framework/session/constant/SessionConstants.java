package com.wan.framework.session.constant;

/**
 * 세션 상수
 */
public final class SessionConstants {

    private SessionConstants() {
        // Utility class
    }

    // Cookie
    public static final String DEFAULT_COOKIE_NAME = "SESSION_ID";
    public static final String DEFAULT_COOKIE_PATH = "/";
    public static final int DEFAULT_COOKIE_MAX_AGE = 1800; // 30분

    // Redis Key Prefix
    public static final String REDIS_SESSION_PREFIX = "spring:session:sessions:";
    public static final String REDIS_USER_SESSION_PREFIX = "SESSION:USER:";

    // Session Attribute Keys
    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_USERNAME = "username";
    public static final String ATTR_ROLES = "roles";
    public static final String ATTR_LOGIN_TIME = "loginTime";
    public static final String ATTR_LAST_ACCESS_TIME = "lastAccessTime";
    public static final String ATTR_IP_ADDRESS = "ipAddress";
    public static final String ATTR_USER_AGENT = "userAgent";

    // Session Event Types
    public static final String EVENT_LOGIN = "LOGIN";
    public static final String EVENT_LOGOUT = "LOGOUT";
    public static final String EVENT_EXPIRED = "EXPIRED";
    public static final String EVENT_FORCE_LOGOUT = "FORCE_LOGOUT";
    public static final String EVENT_CONCURRENT_LOGOUT = "CONCURRENT_LOGOUT";

    // Default Values
    public static final int DEFAULT_MAX_INACTIVE_INTERVAL = 1800; // 30분
    public static final double DEFAULT_REFRESH_THRESHOLD = 0.5; // 50%
}
