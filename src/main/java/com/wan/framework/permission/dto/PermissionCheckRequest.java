package com.wan.framework.permission.dto;

import lombok.*;

/**
 * 권한 체크 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionCheckRequest {
    private String httpMethod;
    private String uriPattern;
    private String roleCode;
}
