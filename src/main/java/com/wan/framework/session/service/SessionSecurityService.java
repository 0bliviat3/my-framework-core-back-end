package com.wan.framework.session.service;

import com.wan.framework.session.config.SessionProperties;
import com.wan.framework.session.exception.SessionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.wan.framework.session.constant.SessionConstants.*;
import static com.wan.framework.session.constant.SessionExceptionMessage.*;

/**
 * 세션 보안 서비스
 * - IP/User-Agent 검증
 * - 세션 탈취 감지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionSecurityService {

    private final SessionProperties sessionProperties;

    /**
     * 세션 보안 검증
     */
    public void validateSessionSecurity(HttpSession session, HttpServletRequest request) {
        // IP 검증
        if (sessionProperties.getSecurity().isValidateIp()) {
            validateIpAddress(session, request);
        }

        // User-Agent 검증
        if (sessionProperties.getSecurity().isValidateUserAgent()) {
            validateUserAgent(session, request);
        }
    }

    /**
     * IP 주소 검증
     */
    private void validateIpAddress(HttpSession session, HttpServletRequest request) {
        String sessionIp = (String) session.getAttribute(ATTR_IP_ADDRESS);
        if (sessionIp == null) {
            return;  // IP가 저장되지 않은 경우 검증 생략
        }

        String requestIp = getClientIP(request);
        if (!sessionIp.equals(requestIp)) {
            log.warn("IP mismatch detected - sessionId: {}, expected: {}, actual: {}",
                    session.getId(), sessionIp, requestIp);
            throw new SessionException(IP_MISMATCH);
        }
    }

    /**
     * User-Agent 검증
     */
    private void validateUserAgent(HttpSession session, HttpServletRequest request) {
        String sessionUA = (String) session.getAttribute(ATTR_USER_AGENT);
        if (sessionUA == null) {
            return;  // UA가 저장되지 않은 경우 검증 생략
        }

        String requestUA = request.getHeader("User-Agent");
        if (!sessionUA.equals(requestUA)) {
            log.warn("User-Agent mismatch detected - sessionId: {}",
                    session.getId());
            throw new SessionException(USER_AGENT_MISMATCH);
        }
    }

    /**
     * 클라이언트 IP 추출 (프록시 환경 고려)
     */
    private String getClientIP(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");

        // 신뢰할 수 있는 프록시에서 온 요청인지 확인
        if (forwardedFor != null && !forwardedFor.isEmpty() && isTrustedProxy(remoteAddr)) {
            // X-Forwarded-For 헤더의 첫 번째 IP 사용 (실제 클라이언트 IP)
            String[] ips = forwardedFor.split(",");
            return ips[0].trim();
        }

        // 프록시가 아니거나 신뢰할 수 없는 경우 직접 연결 IP 사용
        return remoteAddr;
    }

    /**
     * 신뢰할 수 있는 프록시 IP인지 확인
     */
    private boolean isTrustedProxy(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // 설정된 신뢰할 수 있는 프록시 목록 확인
        java.util.List<String> trustedProxies = sessionProperties.getSecurity().getTrustedProxies();
        if (trustedProxies == null || trustedProxies.isEmpty()) {
            // 설정이 없으면 로컬 IP만 신뢰
            return ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
        }

        return trustedProxies.contains(ip);
    }
}
