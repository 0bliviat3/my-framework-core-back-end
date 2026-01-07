package com.wan.framework.session.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 세션 설정 프로퍼티
 */
@Data
@Component
@ConfigurationProperties(prefix = "session")
public class SessionProperties {

    private Cookie cookie = new Cookie();
    private Security security = new Security();
    private Refresh refresh = new Refresh();
    private Concurrent concurrent = new Concurrent();

    @Data
    public static class Cookie {
        private String name = "SESSION_ID";
        private String path = "/";
        private boolean httpOnly = true;
        private boolean secure = true;
        private String sameSite = "Strict";
        private int maxAge = 1800;  // 30분
        private String domain;
    }

    @Data
    public static class Security {
        private boolean validateIp = true;
        private boolean validateUserAgent = false;
        private java.util.List<String> trustedProxies = new java.util.ArrayList<>();  // 신뢰할 수 있는 프록시 IP
    }

    @Data
    public static class Concurrent {
        private boolean enabled = true;  // 동시 로그인 제한 활성화
        private int maxSessions = 3;      // 사용자당 최대 세션 수
        private boolean preventLogin = false;  // 최대 세션 수 초과 시 로그인 차단 (false: 가장 오래된 세션 종료)
    }

    @Data
    public static class Refresh {
        private boolean enabled = true;
        private double threshold = 0.5;  // 50%
    }
}
