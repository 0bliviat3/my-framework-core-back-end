package com.wan.framework.permission.domain;

import com.wan.framework.permission.constant.ApiStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * API Registry 엔티티
 * - 애플리케이션의 모든 API 엔드포인트 정보 관리
 */
@Entity
@Table(name = "t_api_registry",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"service_id", "http_method", "uri_pattern"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ApiRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_id")
    private Long apiId;

    @Column(name = "service_id", nullable = false, length = 50)
    private String serviceId;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "uri_pattern", nullable = false, length = 500)
    private String uriPattern;

    @Column(name = "controller_name", length = 200)
    private String controllerName;

    @Column(name = "handler_method", length = 200)
    private String handlerMethod;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "auth_required", nullable = false)
    @Builder.Default
    private Boolean authRequired = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApiStatus status = ApiStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ApiStatus.ACTIVE;
        }
        if (this.authRequired == null) {
            this.authRequired = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 메서드
    public void updateInfo(String controllerName, String handlerMethod, String description) {
        this.controllerName = controllerName;
        this.handlerMethod = handlerMethod;
        this.description = description;
    }

    public void activate() {
        this.status = ApiStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ApiStatus.INACTIVE;
    }

    public void setAuthRequired(Boolean authRequired) {
        this.authRequired = authRequired;
    }

    /**
     * API 식별자 생성
     */
    public String getApiIdentifier() {
        return serviceId + "::" + httpMethod + "::" + uriPattern;
    }
}
