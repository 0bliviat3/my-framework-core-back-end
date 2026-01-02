package com.wan.framework.apikey.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_api_key_usage_history", indexes = {
    @Index(name = "idx_api_key_id", columnList = "api_key_id"),
    @Index(name = "idx_used_at", columnList = "used_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyUsageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id", nullable = false)
    private ApiKey apiKey;

    @Column(name = "request_uri", nullable = false, length = 500)
    private String requestUri; // 요청 URI

    @Column(name = "request_method", nullable = false, length = 10)
    private String requestMethod; // HTTP Method (GET, POST 등)

    @Column(name = "ip_address", length = 50)
    private String ipAddress; // 요청 IP

    @Column(name = "user_agent", length = 500)
    private String userAgent; // User-Agent

    @Column(name = "response_status")
    private Integer responseStatus; // HTTP 응답 코드

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess; // 성공 여부

    @Column(name = "error_message", length = 500)
    private String errorMessage; // 에러 메시지

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt; // 사용 시각

    @PrePersist
    protected void onCreate() {
        this.usedAt = LocalDateTime.now();
    }
}
