package com.wan.framework.permission.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Role-API Permission DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleApiPermissionDTO {
    private Long permissionId;
    private Long roleId;
    private String roleCode;
    private Long apiId;
    private String httpMethod;
    private String uriPattern;
    private Boolean allowed;
    private LocalDateTime createdAt;
}
