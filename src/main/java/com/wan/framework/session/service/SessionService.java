package com.wan.framework.session.service;

import com.wan.framework.session.config.SessionProperties;
import com.wan.framework.session.constant.SessionExceptionMessage;
import com.wan.framework.session.domain.SessionAudit;
import com.wan.framework.session.domain.UserSession;
import com.wan.framework.session.dto.SessionDTO;
import com.wan.framework.session.exception.SessionException;
import com.wan.framework.session.repository.SessionAuditRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.wan.framework.session.constant.SessionConstants.*;
import static com.wan.framework.session.constant.SessionExceptionMessage.*;

/**
 * 세션 서비스
 * - 세션 생성, 조회, 삭제, 갱신
 * - Redis 기반 세션 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionAuditRepository sessionAuditRepository;
    private final SessionProperties sessionProperties;
    private final SessionSecurityService sessionSecurityService;

    /**
     * 세션 생성
     */
    @Transactional
    public SessionDTO createSession(HttpServletRequest request,
                                   HttpServletResponse response,
                                   String userId,
                                   String username,
                                   List<String> roles) {
        log.info("Creating session for user: {}", userId);

        try {
            // 동시 로그인 제한 확인
            if (sessionProperties.getConcurrent().isEnabled()) {
                checkConcurrentSessions(userId);
            }

            // 기존 세션 무효화 (세션 고정 방지)
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // 새 세션 생성
            HttpSession session = request.getSession(true);
            String sessionId = session.getId();

            // 세션 데이터 설정
            session.setAttribute(ATTR_USER_ID, userId);
            session.setAttribute(ATTR_USERNAME, username);
            session.setAttribute(ATTR_ROLES, roles);
            session.setAttribute(ATTR_LOGIN_TIME, LocalDateTime.now().toString());
            session.setAttribute(ATTR_LAST_ACCESS_TIME, LocalDateTime.now().toString());

            // 보안 정보 저장
            String ipAddress = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");
            session.setAttribute(ATTR_IP_ADDRESS, ipAddress);
            session.setAttribute(ATTR_USER_AGENT, userAgent);

            // 세션 타임아웃 설정
            session.setMaxInactiveInterval(DEFAULT_MAX_INACTIVE_INTERVAL);

            // 쿠키 설정
            setCookie(response, sessionId);

            // 감사 로그 기록
            saveAuditLog(sessionId, userId, EVENT_LOGIN, ipAddress, userAgent, null);

            log.info("Session created successfully: sessionId={}, userId={}", sessionId, userId);

            return SessionDTO.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .username(username)
                    .roles(roles)
                    .loginTime(LocalDateTime.now())
                    .expiresIn(DEFAULT_MAX_INACTIVE_INTERVAL)
                    .build();

        } catch (Exception e) {
            log.error("Failed to create session for user: {}", userId, e);
            throw new SessionException(SESSION_CREATE_FAILED, e);
        }
    }

    /**
     * 현재 세션 정보 조회
     */
    public SessionDTO getCurrentSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SessionException(SESSION_NOT_FOUND);
        }

        // 보안 검증
        sessionSecurityService.validateSessionSecurity(session, request);

        String sessionId = session.getId();
        String userId = (String) session.getAttribute(ATTR_USER_ID);
        String username = (String) session.getAttribute(ATTR_USERNAME);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) session.getAttribute(ATTR_ROLES);
        LocalDateTime loginTime = LocalDateTime.parse((String) session.getAttribute(ATTR_LOGIN_TIME));
        LocalDateTime lastAccessTime = LocalDateTime.parse((String) session.getAttribute(ATTR_LAST_ACCESS_TIME));

        // 남은 시간 계산
        int maxInactiveInterval = session.getMaxInactiveInterval();
        long lastAccessedTime = session.getLastAccessedTime();
        long currentTime = System.currentTimeMillis();
        int remainingTime = (int) ((lastAccessedTime + (maxInactiveInterval * 1000L) - currentTime) / 1000);

        return SessionDTO.builder()
                .sessionId(sessionId)
                .userId(userId)
                .username(username)
                .roles(roles)
                .loginTime(loginTime)
                .lastAccessTime(lastAccessTime)
                .remainingTime(Math.max(0, remainingTime))
                .build();
    }

    /**
     * 세션 삭제 (로그아웃)
     */
    @Transactional
    public void deleteSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            log.warn("No session to delete");
            return;
        }

        String sessionId = session.getId();
        String userId = (String) session.getAttribute(ATTR_USER_ID);

        try {
            // 세션 무효화
            session.invalidate();

            // 쿠키 삭제
            clearCookie(response);

            // 감사 로그 기록
            String ipAddress = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");
            saveAuditLog(sessionId, userId, EVENT_LOGOUT, ipAddress, userAgent, null);

            log.info("Session deleted successfully: sessionId={}, userId={}", sessionId, userId);

        } catch (Exception e) {
            log.error("Failed to delete session: sessionId={}", sessionId, e);
            throw new SessionException(SESSION_DELETE_FAILED, e);
        }
    }

    /**
     * 세션 갱신 (TTL 연장)
     */
    public void refreshSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new SessionException(SESSION_NOT_FOUND);
        }

        // 갱신 설정 확인
        if (!sessionProperties.getRefresh().isEnabled()) {
            log.debug("Session refresh is disabled");
            return;
        }

        String sessionId = session.getId();
        int maxInactiveInterval = session.getMaxInactiveInterval();
        long lastAccessedTime = session.getLastAccessedTime();
        long currentTime = System.currentTimeMillis();

        // 남은 시간 계산
        long elapsedTime = currentTime - lastAccessedTime;
        long remainingTime = (maxInactiveInterval * 1000L) - elapsedTime;
        double remainingRatio = (double) remainingTime / (maxInactiveInterval * 1000L);

        // 임계값 확인
        double threshold = sessionProperties.getRefresh().getThreshold();
        if (remainingRatio > threshold) {
            log.debug("Session does not need refresh yet: sessionId={}, remaining={}%",
                    sessionId, String.format("%.1f", remainingRatio * 100));
            return;
        }

        // 마지막 접근 시간 업데이트
        session.setAttribute(ATTR_LAST_ACCESS_TIME, LocalDateTime.now().toString());

        // Redis TTL 갱신 (Spring Session이 자동으로 처리하지만 명시적으로 갱신)
        try {
            String redisKey = REDIS_SESSION_PREFIX + sessionId;
            redisTemplate.expire(redisKey, maxInactiveInterval, TimeUnit.SECONDS);
            log.debug("Session TTL refreshed: sessionId={}, ttl={}s", sessionId, maxInactiveInterval);
        } catch (Exception e) {
            log.warn("Failed to refresh session TTL in Redis: sessionId={}", sessionId, e);
            // TTL 갱신 실패해도 세션은 유지
        }

        log.info("Session refreshed: sessionId={}", sessionId);
    }

    /**
     * 세션 유효성 검증
     */
    public boolean validateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        try {
            // 보안 검증
            sessionSecurityService.validateSessionSecurity(session, request);
            return true;
        } catch (SessionException e) {
            log.warn("Session validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 특정 세션 강제 종료 (관리자용)
     */
    @Transactional
    public void forceLogout(String sessionId, String adminId) {
        log.info("Force logout requested by admin: sessionId={}, adminId={}", sessionId, adminId);

        try {
            // Redis에서 세션 삭제
            String redisKey = REDIS_SESSION_PREFIX + sessionId;
            redisTemplate.delete(redisKey);

            // 감사 로그 기록
            saveAuditLog(sessionId, "UNKNOWN", EVENT_FORCE_LOGOUT, null, null,
                    "Forced logout by admin: " + adminId);

            log.info("Session force logout completed: sessionId={}", sessionId);

        } catch (Exception e) {
            log.error("Failed to force logout session: sessionId={}", sessionId, e);
            throw new SessionException(SESSION_DELETE_FAILED, e);
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * 쿠키 설정
     */
    private void setCookie(HttpServletResponse response, String sessionId) {
        SessionProperties.Cookie cookieConfig = sessionProperties.getCookie();

        Cookie cookie = new Cookie(cookieConfig.getName(), sessionId);
        cookie.setPath(cookieConfig.getPath());
        cookie.setHttpOnly(cookieConfig.isHttpOnly());
        cookie.setSecure(cookieConfig.isSecure());
        cookie.setMaxAge(cookieConfig.getMaxAge());

        if (cookieConfig.getDomain() != null && !cookieConfig.getDomain().isEmpty()) {
            cookie.setDomain(cookieConfig.getDomain());
        }

        response.addCookie(cookie);
    }

    /**
     * 쿠키 삭제
     */
    private void clearCookie(HttpServletResponse response) {
        SessionProperties.Cookie cookieConfig = sessionProperties.getCookie();

        Cookie cookie = new Cookie(cookieConfig.getName(), null);
        cookie.setPath(cookieConfig.getPath());
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    /**
     * 클라이언트 IP 추출
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 동시 로그인 제한 확인
     */
    private void checkConcurrentSessions(String userId) {
        // Redis에서 해당 사용자의 모든 세션 조회
        String pattern = REDIS_SESSION_PREFIX + "*";
        Set<String> sessionKeys = redisTemplate.keys(pattern);

        if (sessionKeys == null || sessionKeys.isEmpty()) {
            return;
        }

        // 동일 사용자의 세션 필터링
        List<String> userSessions = new ArrayList<>();
        for (String key : sessionKeys) {
            try {
                Object sessionObj = redisTemplate.opsForValue().get(key);
                if (sessionObj != null) {
                    // Spring Session의 MapSession 구조에서 userId 추출
                    String sessionUserId = extractUserIdFromSession(sessionObj);
                    if (userId.equals(sessionUserId)) {
                        userSessions.add(key);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to check session: {}", key, e);
            }
        }

        int maxSessions = sessionProperties.getConcurrent().getMaxSessions();
        boolean preventLogin = sessionProperties.getConcurrent().isPreventLogin();

        log.debug("User {} has {} active sessions (max: {})", userId, userSessions.size(), maxSessions);

        if (userSessions.size() >= maxSessions) {
            if (preventLogin) {
                // 새 로그인 차단
                log.warn("Concurrent session limit exceeded for user: {}", userId);
                throw new SessionException(SessionExceptionMessage.CONCURRENT_SESSION_LIMIT_EXCEEDED);
            } else {
                // 가장 오래된 세션 종료
                terminateOldestSession(userSessions);
            }
        }
    }

    /**
     * Session 객체에서 userId 추출
     */
    private String extractUserIdFromSession(Object sessionObj) {
        try {
            if (sessionObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> sessionMap = (Map<String, Object>) sessionObj;
                Object attrs = sessionMap.get("sessionAttr:" + ATTR_USER_ID);
                return attrs != null ? attrs.toString() : null;
            }
        } catch (Exception e) {
            log.debug("Failed to extract userId from session", e);
        }
        return null;
    }

    /**
     * 가장 오래된 세션 종료
     */
    private void terminateOldestSession(List<String> sessionKeys) {
        if (sessionKeys.isEmpty()) {
            return;
        }

        // 첫 번째 세션을 가장 오래된 것으로 간주
        String oldestKey = sessionKeys.get(0);
        long oldestTime = Long.MAX_VALUE;

        for (String key : sessionKeys) {
            try {
                Object sessionObj = redisTemplate.opsForValue().get(key);
                if (sessionObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sessionMap = (Map<String, Object>) sessionObj;
                    Object creationTime = sessionMap.get("creationTime");
                    if (creationTime instanceof Long) {
                        long time = (Long) creationTime;
                        if (time < oldestTime) {
                            oldestTime = time;
                            oldestKey = key;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to check session creation time: {}", key, e);
            }
        }

        // 가장 오래된 세션 삭제
        redisTemplate.delete(oldestKey);
        String sessionId = oldestKey.replace(REDIS_SESSION_PREFIX, "");

        log.info("Terminated oldest session due to concurrent login limit: {}", sessionId);

        // 감사 로그 기록
        saveAuditLog(sessionId, "UNKNOWN", EVENT_CONCURRENT_LOGOUT, null, null,
                "Terminated due to concurrent session limit");
    }

    /**
     * 감사 로그 저장
     */
    private void saveAuditLog(String sessionId, String userId, String eventType,
                             String ipAddress, String userAgent, String additionalInfo) {
        try {
            SessionAudit audit = SessionAudit.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .eventType(eventType)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .eventTime(LocalDateTime.now())
                    .additionalInfo(additionalInfo)
                    .build();

            sessionAuditRepository.save(audit);
        } catch (Exception e) {
            log.error("Failed to save session audit log", e);
            // 감사 로그 저장 실패는 세션 생성을 막지 않음
        }
    }
}
