package com.wan.framework.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class FrameworkWebMVCConfig implements WebMvcConfigurer {

    private final FrameworkInterceptor frameworkInterceptor;

    public FrameworkWebMVCConfig(final FrameworkInterceptor frameworkInterceptor) {
        log.debug("load config");
        this.frameworkInterceptor = frameworkInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(frameworkInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/sign-up", "/api-docs", "/swagger-ui/**", "/v3/api-docs/**", "/js/**", "/css/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:9527") // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // 쿠키/세션 포함 허용 시
    }
}
