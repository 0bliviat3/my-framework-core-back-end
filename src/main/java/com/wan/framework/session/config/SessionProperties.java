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
    }

    @Data
    public static class Refresh {
        private boolean enabled = true;
        private double threshold = 0.5;  // 50%
    }
}
