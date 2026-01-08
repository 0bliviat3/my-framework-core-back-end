package com.wan.framework.apikey.service;

import com.wan.framework.apikey.constant.ApiKeyExceptionMessage;
import com.wan.framework.apikey.dto.ApiKeyDTO;
import com.wan.framework.apikey.exception.ApiKeyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

import static com.wan.framework.apikey.constant.ApiKeyExceptionMessage.PERMISSION_DENIED;

/**
 * API Key 권한 검증 서비스
 * - 요청 URI와 HTTP Method에 대한 권한 확인
 * - 와일드카드 패턴 지원
 */
@Slf4j
@Service
public class ApiKeyPermissionValidator {

    /**
     * API Key가 특정 요청에 대한 권한을 가지고 있는지 확인
     *
     * @param apiKey        검증할 API Key
     * @param requestUri    요청 URI (예: /api/users/123)
     * @param requestMethod HTTP Method (예: GET, POST)
     * @return 권한 유무
     */
    public boolean hasPermission(ApiKeyDTO apiKey, String requestUri, String requestMethod) {
        if (apiKey == null || apiKey.getPermissions() == null || apiKey.getPermissions().isEmpty()) {
            log.warn("API Key has no permissions: {}", apiKey != null ? apiKey.getApiKeyPrefix() : "null");
            return false;
        }

        List<String> permissions = apiKey.getPermissions();

        return permissions.stream().anyMatch(permissionString ->
                matchesPermission(permissionString, requestUri, requestMethod)
        );
    }

    /**
     * 권한 확인 및 예외 발생
     *
     * @param apiKey        검증할 API Key
     * @param requestUri    요청 URI
     * @param requestMethod HTTP Method
     * @throws ApiKeyException 권한 없을 시
     */
    public void validatePermission(ApiKeyDTO apiKey, String requestUri, String requestMethod) {
        if (!hasPermission(apiKey, requestUri, requestMethod)) {
            log.error("Permission denied for API Key: {}, URI: {}, Method: {}",
                    apiKey.getApiKeyPrefix(), requestUri, requestMethod);
            throw new ApiKeyException(PERMISSION_DENIED);
        }

        log.debug("Permission granted for API Key: {}, URI: {}, Method: {}",
                apiKey.getApiKeyPrefix(), requestUri, requestMethod);
    }

    /**
     * 권한 패턴과 실제 요청 매칭
     * <p>
     * 권한 형식:
     * - "resource:method" (예: "/api/users:GET")
     * - "resource:*" (예: "/api/users:*" - 모든 메서드 허용)
     * - "*:method" (예: "*:GET" - 모든 리소스에 대한 GET)
     * - "*:*" (모든 요청 허용)
     * <p>
     * 와일드카드 지원:
     * - "/api/users/*" - /api/users/123 등 매칭
     * - "/api/users/**" - /api/users/123/posts 등 하위 경로 모두 매칭
     *
     * @param permissionString 권한 문자열
     * @param requestUri       요청 URI
     * @param requestMethod    HTTP Method
     * @return 매칭 여부
     */
    private boolean matchesPermission(String permissionString, String requestUri, String requestMethod) {
        String[] parts = permissionString.split(":");

        if (parts.length != 2) {
            log.warn("Invalid permission format: {}", permissionString);
            return false;
        }

        String resourcePattern = parts[0].trim();
        String allowedMethod = parts[1].trim();

        // 메서드 확인
        boolean methodMatches = allowedMethod.equals("*") || allowedMethod.equalsIgnoreCase(requestMethod);

        if (!methodMatches) {
            return false;
        }

        // 리소스 패턴 확인
        return matchesResourcePattern(resourcePattern, requestUri);
    }

    /**
     * 리소스 패턴과 URI 매칭
     *
     * @param resourcePattern 리소스 패턴 (와일드카드 포함 가능)
     * @param requestUri      요청 URI
     * @return 매칭 여부
     */
    private boolean matchesResourcePattern(String resourcePattern, String requestUri) {
        // 전체 허용
        if (resourcePattern.equals("*") || resourcePattern.equals("/**")) {
            return true;
        }

        // 정확한 매칭
        if (resourcePattern.equals(requestUri)) {
            return true;
        }

        // 와일드카드 패턴을 정규식으로 변환
        String regex = convertToRegex(resourcePattern);

        try {
            Pattern pattern = Pattern.compile(regex);
            boolean matches = pattern.matcher(requestUri).matches();

            log.debug("Pattern matching: {} vs {} (regex: {}) = {}",
                    resourcePattern, requestUri, regex, matches);

            return matches;

        } catch (Exception e) {
            log.error("Failed to compile regex pattern: {}", regex, e);
            return false;
        }
    }

    /**
     * 와일드카드 패턴을 정규식으로 변환
     *
     * @param pattern 와일드카드 패턴
     * @return 정규식 문자열
     */
    private String convertToRegex(String pattern) {
        // 특수 문자 이스케이프
        String regex = pattern
                .replace(".", "\\.")
                .replace("?", "\\?")
                .replace("+", "\\+")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("|", "\\|")
                .replace("^", "\\^")
                .replace("$", "\\$");

        // /** -> 모든 하위 경로 매칭
        regex = regex.replace("/**", "/.*");

        // /* -> 단일 경로 세그먼트 매칭
        regex = regex.replace("/*", "/[^/]+");

        // * -> 임의의 문자 매칭 (슬래시 제외)
        regex = regex.replace("*", "[^/]+");

        // 정규식 시작과 끝 추가
        return "^" + regex + "$";
    }

    /**
     * 권한 형식 검증
     *
     * @param permission 권한 문자열
     * @return 유효 여부
     */
    public boolean isValidPermissionFormat(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }

        String[] parts = permission.split(":");
        if (parts.length != 2) {
            return false;
        }

        String resource = parts[0].trim();
        String method = parts[1].trim();

        // 리소스와 메서드가 비어있지 않아야 함
        return !resource.isEmpty() && !method.isEmpty();
    }

    /**
     * 권한 설명 생성
     *
     * @param permission 권한 문자열
     * @return 권한 설명
     */
    public String getPermissionDescription(String permission) {
        if (!isValidPermissionFormat(permission)) {
            return "Invalid permission format";
        }

        String[] parts = permission.split(":");
        String resource = parts[0].trim();
        String method = parts[1].trim();

        String resourceDesc = switch (resource) {
            case "*", "/**" -> "모든 리소스";
            default -> resource;
        };

        String methodDesc = method.equals("*") ? "모든 메서드" : method;

        return String.format("%s에 대한 %s 권한", resourceDesc, methodDesc);
    }
}
