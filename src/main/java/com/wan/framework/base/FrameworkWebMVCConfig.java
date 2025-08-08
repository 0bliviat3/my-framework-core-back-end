package com.wan.framework.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
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
                .excludePathPatterns("/**");
        //.excludePathPatterns("/sign-up", "/api-docs", "/swagger-ui/*", "/api/**", "/js/**", "/css/**");
    }

}
