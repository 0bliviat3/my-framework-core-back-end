package com.wan.framework.permission.dto;

import lombok.*;

/**
 * Role DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long roleId;
    private String roleCode;
    private String roleName;
    private String description;
}