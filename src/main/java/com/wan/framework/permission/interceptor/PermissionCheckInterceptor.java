package com.wan.framework.permission.interceptor;

import com.wan.framework.permission.constant.PermissionConstants;
import com.wan.framework.permission.constant.PermissionExceptionMessage;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.exception.PermissionException;
import com.wan.framework.permission.repository.ApiRegistryRepository;
import com.wan.framework.permission.service.PermissionCacheService;
import com.wan.framework.session.constant.SessionConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * 권한 검증 인터셉터
 * - API 요청 시 사용자 권한 검증
 * - ADMIN Role은 모든 API 접근 허용
 * - authRequired=false인 API는 권한 검사 제외
 * - 패턴 매칭 지원 (/users/{userId} 등)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionCheckInterceptor implements HandlerInterceptor {

    private final ApiRegistryRepository apiRegistryRepository;
    private final PermissionCacheService permissionCacheService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();

        log.debug("Permission check: {} {}", requestMethod, requestUri);

        try {
            // 1. API Registry 조회 (정확한 매칭 먼저 시도)
            ApiRegistry api = apiRegistryRepository
                    .findActiveApiByMethodAndUri(requestMethod, requestUri)
                    .orElse(null);

            // 2. 정확한 매칭 실패 시 패턴 매칭 시도
            if (api == null) {
                api = findApiByPatternMatching(requestMethod, requestUri);
            }

            // 3. API가 등록되지 않았거나 비활성 상태면 404
            if (api == null) {
                log.warn("API not found or inactive: {} {}", requestMethod, requestUri);
                throw new PermissionException(PermissionExceptionMessage.API_NOT_FOUND);
            }

            // 4. 인증 불필요한 API는 통과
            if (!api.getAuthRequired()) {
                log.debug("Auth not required for this API: {} {}", requestMethod, requestUri);
                return true;
            }

            // 5. 세션에서 사용자 Role 조회
            HttpSession session = request.getSession(false);
            if (session == null) {
                log.warn("No session found for protected API: {} {}", requestMethod, requestUri);
                throw new PermissionException(PermissionExceptionMessage.PERMISSION_DENIED);
            }

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) session.getAttribute(SessionConstants.ATTR_ROLES);

            if (roles == null || roles.isEmpty()) {
                log.warn("No roles found in session for: {} {}", requestMethod, requestUri);
                throw new PermissionException(PermissionExceptionMessage.PERMISSION_DENIED);
            }

            // 6. ADMIN Role은 모든 API 접근 허용
            if (roles.contains(PermissionConstants.ROLE_ADMIN)) {
                log.debug("ADMIN role detected, allowing access");
                return true;
            }

            // 7. 권한 검증 (Redis 캐시 기반 - O(1))
            String apiIdentifier = api.getApiIdentifier();
            boolean hasPermission = false;

            for (String roleCode : roles) {
                if (permissionCacheService.hasPermission(roleCode, apiIdentifier)) {
                    hasPermission = true;
                    break;
                }
            }

            if (!hasPermission) {
                String userId = (String) session.getAttribute(SessionConstants.ATTR_USER_ID);
                log.warn("Permission denied: user={}, roles={}, api={} {}",
                        userId, roles, requestMethod, requestUri);
                throw new PermissionException(PermissionExceptionMessage.PERMISSION_DENIED);
            }

            log.debug("Permission granted: {} {}", requestMethod, requestUri);
            return true;

        } catch (PermissionException e) {
            // PermissionException은 그대로 throw
            throw e;
        } catch (Exception e) {
            log.error("Permission check failed: {} {}", requestMethod, requestUri, e);
            throw new PermissionException(PermissionExceptionMessage.PERMISSION_DENIED, e);
        }
    }

    /**
     * 패턴 매칭을 통한 API 조회
     * - /users/{userId} 같은 패턴과 /users/aaa 같은 실제 요청 매칭
     * - AntPathMatcher 사용
     */
    private ApiRegistry findApiByPatternMatching(String requestMethod, String requestUri) {
        // 같은 HTTP Method의 모든 활성 API 조회
        List<ApiRegistry> candidateApis = apiRegistryRepository.findActiveApisByMethod(requestMethod);

        // 패턴 매칭 시도
        for (ApiRegistry candidateApi : candidateApis) {
            String uriPattern = candidateApi.getUriPattern();

            // AntPathMatcher로 패턴 매칭
            if (pathMatcher.match(uriPattern, requestUri)) {
                log.debug("Pattern matched: {} matches pattern {}", requestUri, uriPattern);
                return candidateApi;
            }
        }

        log.debug("No pattern matched for: {} {}", requestMethod, requestUri);
        return null;
    }
}
