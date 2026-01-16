package com.wan.framework.apikey.domain;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.base.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.AbleState.ABLE;

@Entity
@Table(name = "t_api_key", indexes = {
    @Index(name = "idx_api_key", columnList = "api_key", unique = true),
    @Index(name = "idx_created_by", columnList = "created_by")
})
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_key", nullable = false, unique = true, length = 64)
    private String apiKey; // API Key (SHA-256 해시)

    @Column(name = "api_key_prefix", nullable = false, length = 16)
    private String apiKeyPrefix; // API Key 앞 8자리 (검색용)

    @Column(length = 500)
    private String description; // API Key 설명

    @Column(name = "able_state", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AbleState ableState = ABLE; // 활성화 상태

    @Column(name = "expired_at")
    private LocalDateTime expiredAt; // 만료일

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt; // 마지막 사용 시각

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Long usageCount = 0L; // 사용 횟수

    @Column(name = "data_state_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataStateCode dataStateCode;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.dataStateCode = DataStateCode.I;
        if (this.ableState == null) {
            this.ableState = ABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // BaseAuditEntity에서 updatedAt 처리
    }

    // 사용 횟수 증가
    public void incrementUsageCount() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount++;
    }

    // 만료 여부 확인
    public boolean isExpired() {
        if (expiredAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiredAt);
    }

    // 활성 상태 확인
    public boolean isActive() {
        return ableState == ABLE && !isExpired();
    }
}
