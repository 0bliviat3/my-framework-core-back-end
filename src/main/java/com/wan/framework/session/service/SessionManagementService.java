package com.wan.framework.session.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.session.domain.SessionAudit;
import com.wan.framework.session.dto.SessionDTO;
import com.wan.framework.session.dto.SessionStatsDTO;
import com.wan.framework.session.repository.SessionAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.session.constant.SessionConstants.*;

/**
 * 세션 관리 서비스 (관리자용)
 * - 세션 통계 조회
 * - 전체 세션 조회
 * - 사용자별 세션 조회
 * - 세션 강제 종료
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionManagementService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionAuditRepository sessionAuditRepository;
    private final SessionService sessionService;

    /**
     * 세션 통계 조회
     */
    @Transactional(readOnly = true)
    public SessionStatsDTO getSessionStats() {
        log.debug("Getting session statistics");

        // Redis에서 전체 세션 수 조회
        Set<String> keys = redisTemplate.keys(REDIS_SESSION_PREFIX + "*");
        long totalSessions = keys != null ? keys.size() : 0;

        // 오늘 만료된 세션 수
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long expiredToday = sessionAuditRepository.countExpiredToday(EVENT_EXPIRED, startOfDay, D);

        // Top 사용자 (최근 감사 로그 기반)
        List<Map<String, Object>> topUsers = getTopUsers();

        return SessionStatsDTO.builder()
                .totalSessions(totalSessions)
                .activeSessions(totalSessions)  // Redis에 있는 세션은 모두 활성 세션
                .expiredToday(expiredToday)
                .averageSessionDuration(1800L)  // 평균 세션 유지 시간 (추정)
                .topUsers(topUsers)
                .build();
    }

    /**
     * 전체 세션 목록 조회 (페이징)
     */
    public Page<SessionDTO> getAllSessions(Pageable pageable) {
        log.debug("Getting all sessions with paging");

        // Redis에서 세션 목록 조회
        Set<String> keys = redisTemplate.keys(REDIS_SESSION_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Page.empty(pageable);
        }

        // 페이징 처리는 간단히 구현 (실제로는 더 복잡한 로직 필요)
        List<SessionDTO> sessions = keys.stream()
                .limit(pageable.getPageSize())
                .skip(pageable.getOffset())
                .map(this::getSessionFromRedis)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Page.empty(pageable);  // 실제 페이징 구현 필요
    }

    /**
     * 사용자별 세션 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SessionDTO> getUserSessions(String userId) {
        log.debug("Getting sessions for user: {}", userId);

        // 사용자별 세션 인덱스에서 조회 (구현 필요)
        String userSessionKey = REDIS_USER_SESSION_PREFIX + userId;
        Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionKey);

        if (sessionIds == null || sessionIds.isEmpty()) {
            return Collections.emptyList();
        }

        return sessionIds.stream()
                .map(sid -> getSessionFromRedis(REDIS_SESSION_PREFIX + sid))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 전체 세션 종료
     */
    @Transactional
    public int terminateUserSessions(String userId, String adminId) {
        log.info("Terminating all sessions for user: {} by admin: {}", userId, adminId);

        List<SessionDTO> userSessions = getUserSessions(userId);
        userSessions.forEach(session -> sessionService.forceLogout(session.getSessionId(), adminId));

        log.info("Terminated {} sessions for user: {}", userSessions.size(), userId);
        return userSessions.size();
    }

    // ==================== Private Helper Methods ====================

    /**
     * Redis에서 세션 정보 조회
     */
    private SessionDTO getSessionFromRedis(String key) {
        try {
            @SuppressWarnings("unchecked")
            Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(key);

            if (sessionData.isEmpty()) {
                return null;
            }

            return SessionDTO.builder()
                    .sessionId(extractSessionId(key))
                    .userId((String) sessionData.get("sessionAttr:" + ATTR_USER_ID))
                    .username((String) sessionData.get("sessionAttr:" + ATTR_USERNAME))
                    .build();
        } catch (Exception e) {
            log.error("Failed to get session from Redis: key={}", key, e);
            return null;
        }
    }

    /**
     * Redis 키에서 세션 ID 추출
     */
    private String extractSessionId(String redisKey) {
        return redisKey.replace(REDIS_SESSION_PREFIX, "");
    }

    /**
     * Top 사용자 조회 (최근 로그인 기준)
     */
    private List<Map<String, Object>> getTopUsers() {
        // 간단한 구현 (실제로는 더 복잡한 쿼리 필요)
        return Collections.emptyList();
    }
}
