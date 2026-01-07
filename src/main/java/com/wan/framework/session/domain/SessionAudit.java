package com.wan.framework.session.domain;

import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.DataStateCode.I;

/**
 * 세션 감사 로그 엔티티
 * 세션 생성/삭제/만료 이벤트 기록
 */
@Entity
@Table(name = "t_session_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;  // LOGIN, LOGOUT, EXPIRED, FORCE_LOGOUT

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @Column(name = "data_state")
    @Enumerated(EnumType.STRING)
    private DataStateCode dataState;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.dataState = I;
        if (this.eventTime == null) {
            this.eventTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedTime = LocalDateTime.now();
    }
}
