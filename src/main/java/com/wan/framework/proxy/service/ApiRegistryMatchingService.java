package com.wan.framework.proxy.service;

import com.wan.framework.permission.constant.ApiStatus;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.repository.ApiRegistryRepository;
import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.exception.ProxyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * API Registry 매핑 서비스
 * - ApiEndpoint와 ApiRegistry 자동 매핑
 * - URI 패턴 매칭 (정확한 매칭 우선, 패턴 매칭 대체)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiRegistryMatchingService {

    private final ApiRegistryRepository apiRegistryRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * targetUrl과 httpMethod로 ApiRegistry 찾기
     *
     * @param targetUrl API 대상 URL (예: /api/users/{id}, http://localhost:8080/api/users/123)
     * @param httpMethod HTTP 메서드
     * @return ApiRegistry ID
     */
    public Long findMatchingApiRegistryId(String targetUrl, String httpMethod) {
        log.debug("Finding ApiRegistry for URL: {} METHOD: {}", targetUrl, httpMethod);

        // 1. URL에서 URI 경로 추출
        String uriPath = extractUriPath(targetUrl);
        log.debug("Extracted URI path: {}", uriPath);

        // 2. 정확한 매칭 시도 (가장 빠름)
        Optional<ApiRegistry> exactMatch = apiRegistryRepository
            .findActiveApiByMethodAndUri(httpMethod.toUpperCase(), uriPath);

        if (exactMatch.isPresent()) {
            log.info("Found exact match: {} {} -> Registry ID: {}",
                httpMethod, uriPath, exactMatch.get().getApiId());
            return exactMatch.get().getApiId();
        }

        // 3. 패턴 매칭 시도 (예: /api/users/{id})
        List<ApiRegistry> candidates = apiRegistryRepository
            .findActiveApisByMethod(httpMethod.toUpperCase());

        for (ApiRegistry candidate : candidates) {
            if (pathMatcher.match(candidate.getUriPattern(), uriPath)) {
                log.info("Found pattern match: {} {} matches {} -> Registry ID: {}",
                    httpMethod, uriPath, candidate.getUriPattern(), candidate.getApiId());
                return candidate.getApiId();
            }
        }

        // 4. 매칭 실패
        log.warn("No matching ApiRegistry found for {} {}", httpMethod, uriPath);
        throw new ProxyException(ProxyExceptionMessage.API_REGISTRY_NOT_FOUND);
    }

    /**
     * URL에서 URI 경로 추출
     * - http://localhost:8080/api/users/123 -> /api/users/123
     * - /api/users/123 -> /api/users/123
     */
    private String extractUriPath(String url) {
        if (url == null || url.isEmpty()) {
            return "/";
        }

        // 프로토콜이 있는 전체 URL인 경우
        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                URI uri = new URI(url);
                String path = uri.getPath();
                return path != null && !path.isEmpty() ? path : "/";
            } catch (Exception e) {
                log.warn("Failed to parse URL: {}", url, e);
                return url;
            }
        }

        // 이미 경로만 있는 경우
        // 쿼리 파라미터 제거
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            return url.substring(0, queryIndex);
        }

        return url;
    }

    /**
     * ApiRegistry ID로 엔티티 조회 (검증용)
     */
    public ApiRegistry getApiRegistry(Long apiRegistryId) {
        return apiRegistryRepository.findById(apiRegistryId)
            .filter(api -> api.getStatus() == ApiStatus.ACTIVE)
            .orElseThrow(() -> new ProxyException(ProxyExceptionMessage.API_REGISTRY_NOT_FOUND));
    }
}
