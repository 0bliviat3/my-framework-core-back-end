package com.wan.framework.proxy.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정
 * - Proxy API 호출용 HTTP 클라이언트
 * - 타임아웃 설정 개선 (Deprecated 메서드 제거)
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // SimpleClientHttpRequestFactory를 사용한 타임아웃 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);     // 연결 타임아웃: 5초
        factory.setReadTimeout(30000);       // 읽기 타임아웃: 30초

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}
