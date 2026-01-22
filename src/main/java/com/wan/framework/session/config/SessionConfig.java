package com.wan.framework.session.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Spring Session Redis 설정
 */
@Slf4j
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
@RequiredArgsConstructor
public class SessionConfig {

    private final SessionProperties sessionProperties;

    @org.springframework.beans.factory.annotation.Value("${COOKIE_SECURE:not-set}")
    private String cookieSecureEnv;

    @org.springframework.beans.factory.annotation.Value("${COOKIE_SAME_SITE:not-set}")
    private String cookieSameSiteEnv;

    /**
     * 쿠키 시리얼라이저 설정
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();

        SessionProperties.Cookie cookie = sessionProperties.getCookie();
        serializer.setCookieName(cookie.getName());
        serializer.setCookiePath(cookie.getPath());
        serializer.setUseHttpOnlyCookie(cookie.isHttpOnly());

        // SameSite=None일 때는 반드시 Secure=true 필요 (브라우저 정책)
        boolean isSecure = cookie.isSecure();
        String sameSite = cookie.getSameSite();
        if ("None".equalsIgnoreCase(sameSite) && !isSecure) {
            log.warn("SameSite=None requires Secure=true. Forcing Secure flag.");
            isSecure = true;
        }

        serializer.setUseSecureCookie(isSecure);
        serializer.setSameSite(sameSite);
        serializer.setCookieMaxAge(cookie.getMaxAge());

        if (cookie.getDomain() != null && !cookie.getDomain().isEmpty()) {
            serializer.setDomainName(cookie.getDomain());
        }

        // 설정 로깅 (환경변수와 실제 바인딩 값 비교)
        log.info("========================================");
        log.info("Session Cookie Configuration");
        log.info("========================================");
        log.info("[Environment Variables]");
        log.info("  COOKIE_SECURE (env): {}", cookieSecureEnv);
        log.info("  COOKIE_SAME_SITE (env): {}", cookieSameSiteEnv);
        log.info("");
        log.info("[SessionProperties (Bound Values)]");
        log.info("  Cookie Name: {}", cookie.getName());
        log.info("  Cookie Path: {}", cookie.getPath());
        log.info("  HttpOnly: {}", cookie.isHttpOnly());
        log.info("  Secure (config): {} (Expected from env: {})", cookie.isSecure(), cookieSecureEnv);
        log.info("  Secure (final): {} {}", isSecure,
                isSecure != cookie.isSecure() ? "(forced by SameSite=None)" : "");
        log.info("  SameSite: {} (Expected from env: {})", sameSite, cookieSameSiteEnv);
        log.info("  MaxAge: {} seconds", cookie.getMaxAge());
        log.info("  Domain: {}", cookie.getDomain() == null ? "(empty)" : cookie.getDomain());
        log.info("");
        log.info("[Security Settings]");
        log.info("  IP Validation: {}", sessionProperties.getSecurity().isValidateIp());
        log.info("  UserAgent Validation: {}", sessionProperties.getSecurity().isValidateUserAgent());
        log.info("");
        log.info("[Session Management]");
        log.info("  Refresh Enabled: {}", sessionProperties.getRefresh().isEnabled());
        log.info("  Refresh Threshold: {}", sessionProperties.getRefresh().getThreshold());
        log.info("  Concurrent Sessions: {}", sessionProperties.getConcurrent().isEnabled());
        log.info("  Max Sessions: {}", sessionProperties.getConcurrent().getMaxSessions());
        log.info("========================================");

        return serializer;
    }

    /**
     * Spring Session Redis 직렬화 설정
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer(new ObjectMapper());
    }
}
