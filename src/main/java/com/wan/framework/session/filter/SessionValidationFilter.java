package com.wan.framework.session.filter;

import com.wan.framework.session.service.SessionService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 세션 검증 필터
 * - 요청마다 세션 유효성 검증
 * - 만료된 세션 처리
 */
@Slf4j
@Component
@Order(2)  // SecurityFilter 다음
@RequiredArgsConstructor
public class SessionValidationFilter implements Filter {

    private final SessionService sessionService;

    // 세션 검증 제외 경로
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/sessions/login",
            "/sessions/logout",
            "/users/sign-in",
            "/users/sign-up",
            "/error",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/users/admin/exists"
    );

    @Override
    public void doFilter(ServletRequest servletRequest,
                        ServletResponse servletResponse,
                        FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();

        // 제외 경로 체크
        if (isExcludedPath(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 세션 검증
        try {
            boolean isValid = sessionService.validateSession(request);
            if (!isValid) {
                log.warn("Invalid session detected for URI: {}", requestURI);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Session expired or invalid\"}");
                return;
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Session validation error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Session validation failed\"}");
        }
    }

    /**
     * 제외 경로 확인
     */
    private boolean isExcludedPath(String requestURI) {
        return EXCLUDED_PATHS.stream().anyMatch(requestURI::startsWith);
    }
}
