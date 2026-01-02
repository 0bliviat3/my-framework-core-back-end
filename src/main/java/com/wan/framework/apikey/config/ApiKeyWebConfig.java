package com.wan.framework.apikey.config;

import com.wan.framework.apikey.interceptor.BearerAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ApiKeyWebConfig implements WebMvcConfigurer {

    private final BearerAuthenticationInterceptor bearerAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(bearerAuthenticationInterceptor)
                .addPathPatterns("/api/**") // API 경로에만 적용
                .excludePathPatterns(
                        "/api/auth/**",      // 인증 관련 제외
                        "/api/public/**"     // 공개 API 제외
                );
    }
}
