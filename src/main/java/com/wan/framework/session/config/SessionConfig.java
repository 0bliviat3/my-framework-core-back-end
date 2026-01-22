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
        serializer.setUseSecureCookie(cookie.isSecure());
        serializer.setSameSite(cookie.getSameSite());
        serializer.setCookieMaxAge(cookie.getMaxAge());

        if (cookie.getDomain() != null && !cookie.getDomain().isEmpty()) {
            serializer.setDomainName(cookie.getDomain());
        }

        // 설정 로깅
        log.info("=== Session Cookie Configuration ===");
        log.info("Cookie Name: {}", cookie.getName());
        log.info("Cookie Path: {}", cookie.getPath());
        log.info("HttpOnly: {}", cookie.isHttpOnly());
        log.info("Secure: {}", cookie.isSecure());
        log.info("SameSite: {}", cookie.getSameSite());
        log.info("MaxAge: {}", cookie.getMaxAge());
        log.info("Domain: {}", cookie.getDomain());
        log.info("IP Validation: {}", sessionProperties.getSecurity().isValidateIp());
        log.info("====================================");

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
