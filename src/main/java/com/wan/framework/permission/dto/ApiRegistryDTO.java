package com.wan.framework.permission.dto;

import com.wan.framework.permission.constant.ApiStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * API Registry DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiRegistryDTO {
    private Long apiId;
    private String serviceId;
    private String httpMethod;
    private String uriPattern;
    private String controllerName;
    private String handlerMethod;
    private String description;
    private Boolean authRequired;
    private ApiStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
