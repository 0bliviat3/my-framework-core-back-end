package com.wan.framework.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class FrameworkWebMVCConfig implements WebMvcConfigurer {

    private final FrameworkInterceptor frameworkInterceptor;
    private final com.wan.framework.permission.interceptor.PermissionCheckInterceptor permissionCheckInterceptor;

    public FrameworkWebMVCConfig(
            final FrameworkInterceptor frameworkInterceptor,
            final com.wan.framework.permission.interceptor.PermissionCheckInterceptor permissionCheckInterceptor) {
        log.debug("load config");
        this.frameworkInterceptor = frameworkInterceptor;
        this.permissionCheckInterceptor = permissionCheckInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(frameworkInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/sign-up", "/api-docs", "/swagger-ui/**", "/v3/api-docs/**", "/js/**", "/css/**");

        // 권한 검증 인터셉터 추가
        registry.addInterceptor(permissionCheckInterceptor)
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
                        "/error"
                );
    }

    // CORS 설정은 FrameworkSecurityConfig에서 관리
    // Spring Security의 CORS 설정이 우선 적용되므로 여기서는 제거
}
