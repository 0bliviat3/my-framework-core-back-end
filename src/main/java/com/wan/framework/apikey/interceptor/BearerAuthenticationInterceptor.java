package com.wan.framework.apikey.interceptor;

import com.wan.framework.apikey.dto.ApiKeyDTO;
import com.wan.framework.apikey.exception.ApiKeyException;
import com.wan.framework.apikey.service.ApiKeyPermissionValidator;
import com.wan.framework.apikey.service.ApiKeyService;
import com.wan.framework.apikey.service.ApiKeyUsageHistoryService;
import com.wan.framework.apikey.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.wan.framework.apikey.constant.ApiKeyExceptionMessage.MISSING_AUTHORIZATION_HEADER;

@Slf4j
@Component
@RequiredArgsConstructor
public class BearerAuthenticationInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String API_KEY_ATTRIBUTE = "apiKey";
    private static final String API_KEY_ID_ATTRIBUTE = "apiKeyId";

    private final ApiKeyService apiKeyService;
    private final ApiKeyUsageHistoryService usageHistoryService;
    private final RateLimitService rateLimitService;
    private final ApiKeyPermissionValidator permissionValidator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            // 1. Authorization 헤더 추출
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                throw new ApiKeyException(MISSING_AUTHORIZATION_HEADER);
            }

            // 2. Bearer 토큰 추출
            String rawApiKey = authHeader.substring(BEARER_PREFIX.length()).trim();

            // 3. API Key 검증 (형식, 만료, 활성화 상태)
            ApiKeyDTO apiKeyDTO = apiKeyService.validateApiKey(rawApiKey);

            // 4. Rate Limit 확인
            rateLimitService.checkRateLimit(apiKeyDTO.getId());

            // 5. 권한 검증 (URI와 HTTP Method)
            permissionValidator.validatePermission(apiKeyDTO, requestUri, requestMethod);

            // 6. Request Attribute에 API Key 정보 저장 (Controller에서 사용 가능)
            request.setAttribute(API_KEY_ATTRIBUTE, apiKeyDTO);
            request.setAttribute(API_KEY_ID_ATTRIBUTE, apiKeyDTO.getId());

            // 7. 사용 횟수 증가
            apiKeyService.incrementUsageCount(apiKeyDTO.getId());

            // 8. 성공 이력 기록
            usageHistoryService.recordUsage(
                    apiKeyDTO.getId(),
                    requestUri,
                    requestMethod,
                    ipAddress,
                    userAgent,
                    HttpServletResponse.SC_OK,
                    true,
                    null
            );

            log.debug("API Key 인증 성공: prefix={}, uri={}, method={}",
                    apiKeyDTO.getApiKeyPrefix(), requestUri, requestMethod);
            return true;

        } catch (ApiKeyException e) {
            log.warn("API Key 인증 실패: uri={}, ip={}, error={}", requestUri, ipAddress, e.getMessage());

            // 실패 이력 기록 (API Key ID를 모르는 경우가 있으므로 조건부)
            usageHistoryService.recordUsage(
                    null,
                    requestUri,
                    requestMethod,
                    ipAddress,
                    userAgent,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    false,
                    e.getMessage()
            );

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;

        } catch (Exception e) {
            log.error("API Key 인증 중 예외 발생: uri={}, error={}", requestUri, e.getMessage(), e);

            usageHistoryService.recordUsage(
                    null,
                    requestUri,
                    requestMethod,
                    ipAddress,
                    userAgent,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    false,
                    e.getMessage()
            );

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    /**
     * 클라이언트 IP 주소 추출
     * 프록시 환경을 고려하여 X-Forwarded-For 헤더를 우선 확인
     */
    private String getClientIpAddress(HttpServletRequest request) {
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

        // X-Forwarded-For에 여러 IP가 있는 경우 첫 번째 IP만 추출
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
