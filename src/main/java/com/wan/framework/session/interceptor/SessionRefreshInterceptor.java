package com.wan.framework.session.interceptor;

import com.wan.framework.session.config.SessionProperties;
import com.wan.framework.session.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 세션 갱신 인터셉터
 * - 일정 조건 충족 시 자동 세션 TTL 갱신
 * - Sliding Window 방식
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionRefreshInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;
    private final SessionProperties sessionProperties;

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) {

        // 세션 갱신 기능이 비활성화된 경우
        if (!sessionProperties.getRefresh().isEnabled()) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;  // 세션이 없으면 갱신 불필요
        }

        try {
            // 세션 남은 시간 계산
            int maxInactiveInterval = session.getMaxInactiveInterval();
            long lastAccessedTime = session.getLastAccessedTime();
            long currentTime = System.currentTimeMillis();
            long elapsedTime = (currentTime - lastAccessedTime) / 1000;

            // 임계값 확인 (예: 50% 경과 시 갱신)
            double threshold = sessionProperties.getRefresh().getThreshold();
            if (elapsedTime >= (maxInactiveInterval * threshold)) {
                sessionService.refreshSession(request);
                log.debug("Session refreshed: sessionId={}", session.getId());
            }

        } catch (Exception e) {
            log.error("Failed to refresh session", e);
            // 갱신 실패해도 요청은 계속 진행
        }

        return true;
    }
}
