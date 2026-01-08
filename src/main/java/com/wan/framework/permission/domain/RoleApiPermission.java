package com.wan.framework.permission.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Role ↔ API 권한 매핑 엔티티
 * - Role별 API 접근 권한 관리
 */
@Entity
@Table(name = "t_role_api_permission",
    uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "api_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoleApiPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiRegistry apiRegistry;

    @Column(name = "allowed", nullable = false)
    @Builder.Default
    private Boolean allowed = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.allowed == null) {
            this.allowed = true;
        }
    }

    // 비즈니스 메서드
    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }
}
