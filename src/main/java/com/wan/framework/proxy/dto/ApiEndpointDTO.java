package com.wan.framework.proxy.dto;

import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API 엔드포인트 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiEndpointDTO {

    private Long id;
    private String apiCode;
    private String apiName;
    private String description;
    private String targetUrl;
    private String httpMethod;
    private String requestHeaders;
    private String requestBodyTemplate;
    private Integer timeoutSeconds;
    private Integer retryCount;
    private Integer retryIntervalMs;
    private Boolean isInternal;
    private Long apiRegistryId;
    private ApiRegistryInfo apiRegistryInfo;
    private Boolean isEnabled;
    private DataStateCode dataState;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    /**
     * API Registry 간단 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiRegistryInfo {
        private Long apiId;
        private String serviceId;
        private String httpMethod;
        private String uriPattern;
        private String controllerName;
        private String handlerMethod;
        private String description;
    }
}
