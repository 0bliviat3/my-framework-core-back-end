package com.wan.framework.permission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DetailedPermission 엔티티
 * - 세부 권한 정보 관리 (HTTP 메서드, URI 패턴 등)
 */
@Entity
@Table(name = "t_detailed_permission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DetailedPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @Column(name = "http_method", length = 10)
    private String httpMethod;
    
    @Column(name = "uri_pattern", length = 500)
    private String uriPattern;
    
    @Column(name = "allowed", nullable = false)
    @Builder.Default
    private Boolean allowed = true;
    
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to")
    private LocalDateTime validTo;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // 비즈니스 메서드
    public void updateInfo(String httpMethod, String uriPattern, Boolean allowed) {
        this.httpMethod = httpMethod;
        this.uriPattern = uriPattern;
        this.allowed = allowed;
    }
}