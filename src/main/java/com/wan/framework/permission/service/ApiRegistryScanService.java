package com.wan.framework.permission.service;

import com.wan.framework.base.constant.PublicApiConstants;
import com.wan.framework.permission.constant.ApiStatus;
import com.wan.framework.permission.constant.PermissionConstants;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.repository.ApiRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * API 자동 스캔 서비스
 * - 애플리케이션 시작 시 모든 API를 스캔하여 DB에 등록
 * - 신규 API INSERT, 변경된 API UPDATE, 삭제된 API INACTIVE 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiRegistryScanService {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ApiRegistryRepository apiRegistryRepository;

    /**
     * 애플리케이션 시작 완료 후 API 스캔
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void scanAndRegisterApis() {
        log.info("===== API Registry Scan Started =====");

        try {
            // 1. 현재 애플리케이션의 모든 API 엔드포인트 스캔
            Map<String, ApiInfo> scannedApis = scanAllApis();
            log.info("Scanned {} APIs from application", scannedApis.size());

            // 2. DB에 저장된 기존 API 조회
            List<ApiRegistry> existingApis = apiRegistryRepository.findAll();
            Map<String, ApiRegistry> existingApiMap = existingApis.stream()
                    .collect(Collectors.toMap(
                            ApiRegistry::getApiIdentifier,
                            api -> api
                    ));

            log.info("Found {} existing APIs in database", existingApiMap.size());

            // 3. 신규/변경/삭제 API 처리
            int insertCount = 0;
            int updateCount = 0;
            int deactivateCount = 0;

            // 3-1. 스캔된 API 처리 (INSERT or UPDATE)
            for (Map.Entry<String, ApiInfo> entry : scannedApis.entrySet()) {
                String identifier = entry.getKey();
                ApiInfo apiInfo = entry.getValue();

                ApiRegistry existing = existingApiMap.get(identifier);

                if (existing == null) {
                    // 신규 API INSERT
                    // 공개 API인지 확인하여 authRequired 설정
                    boolean isPublicApi = PublicApiConstants.isPublicApi(apiInfo.uriPattern);

                    ApiRegistry newApi = ApiRegistry.builder()
                            .serviceId(apiInfo.serviceId)
                            .httpMethod(apiInfo.httpMethod)
                            .uriPattern(apiInfo.uriPattern)
                            .controllerName(apiInfo.controllerName)
                            .handlerMethod(apiInfo.handlerMethod)
                            .description(apiInfo.description)
                            .authRequired(!isPublicApi)  // 공개 API는 인증 불필요
                            .status(ApiStatus.ACTIVE)
                            .build();

                    apiRegistryRepository.save(newApi);
                    insertCount++;
                    log.debug("INSERT: {} {} {} (authRequired={})",
                            apiInfo.httpMethod, apiInfo.uriPattern, apiInfo.handlerMethod, !isPublicApi);

                } else {
                    // 기존 API UPDATE (컨트롤러명, 핸들러명, 설명, authRequired 변경 가능)
                    boolean updated = false;

                    if (!Objects.equals(existing.getControllerName(), apiInfo.controllerName) ||
                        !Objects.equals(existing.getHandlerMethod(), apiInfo.handlerMethod) ||
                        !Objects.equals(existing.getDescription(), apiInfo.description)) {

                        existing.updateInfo(apiInfo.controllerName, apiInfo.handlerMethod, apiInfo.description);
                        updated = true;
                    }

                    // 공개 API 여부 확인 후 authRequired 업데이트
                    boolean isPublicApi = PublicApiConstants.isPublicApi(apiInfo.uriPattern);
                    boolean expectedAuthRequired = !isPublicApi;

                    if (existing.getAuthRequired() != expectedAuthRequired) {
                        existing.setAuthRequired(expectedAuthRequired);
                        updated = true;
                        log.info("UPDATE authRequired: {} {} -> authRequired={}",
                                apiInfo.httpMethod, apiInfo.uriPattern, expectedAuthRequired);
                    }

                    // INACTIVE 상태였다면 다시 ACTIVE로 변경
                    if (existing.getStatus() == ApiStatus.INACTIVE) {
                        existing.activate();
                        updated = true;
                    }

                    if (updated) {
                        updateCount++;
                        log.debug("UPDATE: {} {} {}", apiInfo.httpMethod, apiInfo.uriPattern, apiInfo.handlerMethod);
                    }

                    // 처리된 API는 맵에서 제거
                    existingApiMap.remove(identifier);
                }
            }

            // 3-2. 남은 API는 삭제된 것으로 간주 → INACTIVE 처리
            for (ApiRegistry api : existingApiMap.values()) {
                if (api.getStatus() == ApiStatus.ACTIVE) {
                    api.deactivate();
                    deactivateCount++;
                    log.debug("DEACTIVATE: {} {} {}", api.getHttpMethod(), api.getUriPattern(), api.getHandlerMethod());
                }
            }

            log.info("===== API Registry Scan Completed =====");
            log.info("INSERT: {}, UPDATE: {}, DEACTIVATE: {}", insertCount, updateCount, deactivateCount);

        } catch (Exception e) {
            log.error("Failed to scan and register APIs", e);
            throw e;
        }
    }

    /**
     * 애플리케이션의 모든 API 스캔
     */
    private Map<String, ApiInfo> scanAllApis() {
        Map<String, ApiInfo> apiMap = new HashMap<>();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods =
                requestMappingHandlerMapping.getHandlerMethods();

        log.debug("Total handler methods found: {}", handlerMethods.size());

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // HTTP Methods 추출
            Set<String> methods = mappingInfo.getMethodsCondition().getMethods().stream()
                    .map(Enum::name)
                    .collect(Collectors.toSet());

            // HTTP Method가 없으면 모든 메서드 허용으로 간주 (실제로는 GET이 기본)
            if (methods.isEmpty()) {
                methods = Set.of("GET");
            }

            // URI Patterns 추출 (Spring Boot 3.x 호환)
            Set<String> patterns = new HashSet<>();

            // PathPatternsCondition 사용 (Spring Boot 3.x)
            if (mappingInfo.getPathPatternsCondition() != null) {
                patterns = mappingInfo.getPathPatternsCondition().getPatterns().stream()
                        .map(Object::toString)
                        .collect(Collectors.toSet());
                log.debug("Using PathPatternsCondition: {}", patterns);
            }
            // PatternsCondition 사용 (Fallback for older versions)
            else if (mappingInfo.getPatternsCondition() != null) {
                patterns = mappingInfo.getPatternsCondition().getPatterns();
                log.debug("Using PatternsCondition: {}", patterns);
            }

            // 패턴이 없으면 스킵
            if (patterns.isEmpty()) {
                log.warn("No patterns found for handler: {}.{}",
                        handlerMethod.getBeanType().getSimpleName(),
                        handlerMethod.getMethod().getName());
                continue;
            }

            // Controller 및 Handler 정보
            String controllerName = handlerMethod.getBeanType().getName();
            String handlerMethodName = handlerMethod.getMethod().getName();

            // 각 HTTP Method와 URI Pattern 조합으로 API 등록
            for (String method : methods) {
                for (String pattern : patterns) {
                    String identifier = PermissionConstants.DEFAULT_SERVICE_ID + "::" + method + "::" + pattern;

                    ApiInfo apiInfo = ApiInfo.builder()
                            .serviceId(PermissionConstants.DEFAULT_SERVICE_ID)
                            .httpMethod(method)
                            .uriPattern(pattern)
                            .controllerName(controllerName)
                            .handlerMethod(handlerMethodName)
                            .description(controllerName + "." + handlerMethodName)
                            .build();

                    apiMap.put(identifier, apiInfo);
                    log.debug("Registered API: {} {} -> {}.{}",
                            method, pattern,
                            handlerMethod.getBeanType().getSimpleName(),
                            handlerMethodName);
                }
            }
        }

        return apiMap;
    }

    /**
     * API 정보 내부 클래스
     */
    @lombok.Builder
    @lombok.Data
    private static class ApiInfo {
        private String serviceId;
        private String httpMethod;
        private String uriPattern;
        private String controllerName;
        private String handlerMethod;
        private String description;
    }
}
