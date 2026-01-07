package com.wan.framework.session.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
