package com.wan.framework.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class FrameworkSecurityConfig {

    @Value("${security.cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Value("${security.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${security.cors.max-age:3600}")
    private Long corsMaxAge;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Security Configuration");
        log.info("Allowed CORS Origins: {}", Arrays.toString(allowedOrigins));

        http
                // CSRF 비활성화 (세션 기반 인증에서는 활성화 권장하나, REST API에서는 비활성화)
                .csrf(csrf -> csrf.disable())

                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 정책 (Spring Session + Redis에서 관리)
                // 주의: maximumSessions 설정은 SessionProperties에서 관리하므로 여기서는 설정하지 않음
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // 동시 세션 제한은 SessionConcurrentService에서 처리
                )

                // 보안 헤더 설정
                .headers(headers -> headers
                        // X-Frame-Options: 클릭재킹 방지
                        .frameOptions(frame -> frame.deny())

                        // X-Content-Type-Options: MIME 스니핑 방지
                        .contentTypeOptions(contentType -> {})

                        // X-XSS-Protection: XSS 필터 활성화
                        .xssProtection(xss -> xss.disable())  // 최신 브라우저는 CSP 사용 권장

                        // Strict-Transport-Security: HTTPS 강제 (HTTPS 환경에서만)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)  // 1년
                        )

                        // Referrer-Policy: 리퍼러 정보 제어
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )

                        // Content-Security-Policy: XSS, 데이터 인젝션 방지
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' 'unsafe-inline'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data: https:; " +
                                        "font-src 'self'; " +
                                        "connect-src 'self'")
                        )
                )

                // 모든 요청 허용 (인터셉터에서 세밀한 권한 제어)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (환경변수로 분리)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 노출할 헤더 (프론트엔드에서 접근 가능)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition",
                "X-Total-Count"
        ));

        // 쿠키/세션 포함 허용 (Credentials)
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(corsMaxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS Configuration initialized - Max Age: {}s", corsMaxAge);
        return source;
    }

}
