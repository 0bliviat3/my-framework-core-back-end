package com.wan.framework.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 세션 통계 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionStatsDTO {
    private Long totalSessions;
    private Long activeSessions;
    private Long expiredToday;
    private Long averageSessionDuration;
    private List<Map<String, Object>> topUsers;
}
