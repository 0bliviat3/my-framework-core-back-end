package com.wan.framework.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 세션 정보 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDTO {
    private String sessionId;
    private String userId;
    private String username;
    private List<String> roles;
    private LocalDateTime loginTime;
    private LocalDateTime lastAccessTime;
    private String ipAddress;
    private String userAgent;
    private Integer expiresIn;      // 남은 시간 (초)
    private Integer remainingTime;  // 남은 시간 (초)
}
