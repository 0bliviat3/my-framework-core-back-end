package com.wan.framework.session.domain;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 세션 도메인 객체
 * Redis에 저장되는 세션 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;           // 세션 ID (UUID)
    private String userId;              // 사용자 ID
    private String username;            // 사용자명
    private List<String> roles;         // 사용자 권한 목록
    private LocalDateTime loginTime;    // 로그인 시각
    private LocalDateTime lastAccessTime;  // 마지막 접근 시각
    private String ipAddress;           // 접속 IP (선택)
    private String userAgent;           // User-Agent (선택)
    private Integer maxInactiveInterval;  // 최대 비활성 시간 (초)
}
