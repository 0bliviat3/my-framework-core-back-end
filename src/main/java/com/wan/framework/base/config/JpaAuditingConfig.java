package com.wan.framework.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 * - @CreatedBy, @LastModifiedBy 자동 설정
 * - @CreatedDate, @LastModifiedDate 자동 설정
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider(SessionAuditorAware sessionAuditorAware) {
        return sessionAuditorAware;
    }
}
