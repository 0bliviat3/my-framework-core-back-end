package com.wan.framework.permission.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Role DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {
    private Long roleId;
    private String roleCode;
    private String roleName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
