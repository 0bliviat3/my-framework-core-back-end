package com.wan.framework.base;

import com.wan.framework.apikey.interceptor.BearerAuthenticationInterceptor;
import com.wan.framework.permission.interceptor.PermissionCheckInterceptor;
import com.wan.framework.session.interceptor.SessionRefreshInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 프레임워크 웹 MVC 설정
 * - 인터셉터 등록 및 순서 관리
 * - 실행 순서: SessionRefresh -> BearerAuth -> Permission -> Framework
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FrameworkWebMVCConfig implements WebMvcConfigurer {

    private final SessionRefreshInterceptor sessionRefreshInterceptor;
    private final BearerAuthenticationInterceptor bearerAuthenticationInterceptor;
    private final PermissionCheckInterceptor permissionCheckInterceptor;
    private final FrameworkInterceptor frameworkInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering interceptors in order");

        // 1. 세션 갱신 인터셉터 (order: 1)
        // - 모든 요청에 대해 세션 TTL 자동 갱신
        registry.addInterceptor(sessionRefreshInterceptor)
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api-docs",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/js/**",
                        "/css/**",
                        "/error",
                        "/api/**"
                );

        // 2. Bearer 인증 인터셉터 (order: 2)
        // - /api/** 경로에만 적용되는 API Key 인증
        registry.addInterceptor(bearerAuthenticationInterceptor)
                .order(2)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",      // 인증 관련 제외
                        "/api/public/**"     // 공개 API 제외
                );

        // 3. 권한 검증 인터셉터 (order: 3)
        // - API 레지스트리 기반 권한 검증
        registry.addInterceptor(permissionCheckInterceptor)
                .order(3)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/users/admin/exists",      // 관리자 존재 여부 확인
                        "/users/admin/initial",     // 초기 관리자 생성
                        "/users/sign-up",           // 회원가입
                        "/sessions/login",          // 로그인
                        "/api-docs",                // API 문서
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/js/**",
                        "/css/**",
                        "/error",
                        "/api/**"
                );

        // 4. 프레임워크 기본 인터셉터 (order: 4)
        // - 로깅 및 공통 처리
        registry.addInterceptor(frameworkInterceptor)
                .order(4)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api-docs",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/js/**",
                        "/css/**"
                );

        log.info("Registered {} interceptors", 4);
    }

    // CORS 설정은 FrameworkSecurityConfig에서 관리
    // Spring Security의 CORS 설정이 우선 적용되므로 여기서는 제거
}
