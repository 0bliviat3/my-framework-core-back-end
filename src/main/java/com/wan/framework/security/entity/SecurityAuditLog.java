package com.wan.framework.security.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SecurityAuditLog 엔티티
 * - 보안 관련 작업 로그 관리
 */
@Entity
@Table(name = "t_security_audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SecurityAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    
    @Column(name = "user_id", length = 50)
    private String userId;
    
    @Column(name = "action", length = 50, nullable = false)
    private String action; // CREATE, UPDATE, DELETE, ACCESS
    
    @Column(name = "resource_type", length = 50)
    private String resourceType; // USER, ROLE, PERMISSION
    
    @Column(name = "resource_id", length = 100)
    private String resourceId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "details", columnDefinition = "JSON")
    private String details;
    
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}