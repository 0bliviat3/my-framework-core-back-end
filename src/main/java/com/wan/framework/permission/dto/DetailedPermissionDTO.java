package com.wan.framework.permission.dto;

import lombok.*;

/**
 * DetailedPermission DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedPermissionDTO {
    private Long permissionId;
    private String httpMethod;
    private String uriPattern;
    private Boolean allowed;
    private String validFrom;
    private String validTo;
}