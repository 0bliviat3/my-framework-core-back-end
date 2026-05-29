package com.wan.framework.permission.controller;

import com.wan.framework.permission.entity.DetailedPermission;
import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.service.DetailedPermissionService;
import com.wan.framework.permission.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Detailed Permission Management REST API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/permissions")
public class DetailedPermissionController {
    
    private final DetailedPermissionService detailedPermissionService;
    private final RoleService roleService;
    
    /**
     * 세부 권한 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<DetailedPermission>> getAllPermissions() {
        log.info("GET /v2/permissions");
        List<DetailedPermission> permissions = detailedPermissionService.getPermissionsByRole(null);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * 특정 역할의 세부 권한 조회
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<DetailedPermission>> getPermissionsByRole(
            @PathVariable Long roleId) {
        log.info("GET /v2/permissions/role/{}", roleId);
        Role role = roleService.getRoleById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        
        List<DetailedPermission> permissions = detailedPermissionService.getPermissionsByRole(role);
        return ResponseEntity.ok(permissions);
    }
    
    /**
     * 세부 권한 생성
     */
    @PostMapping
    public ResponseEntity<DetailedPermission> createPermission(
            @RequestBody DetailedPermission permission) {
        log.info("POST /v2/permissions - method: {}, pattern: {}", 
            permission.getHttpMethod(), permission.getUriPattern());
        DetailedPermission createdPermission = detailedPermissionService.createPermission(permission);
        return ResponseEntity.ok(createdPermission);
    }
    
    /**
     * 세부 권한 수정
     */
    @PutMapping("/{permissionId}")
    public ResponseEntity<DetailedPermission> updatePermission(
            @PathVariable Long permissionId,
            @RequestBody DetailedPermission permission) {
        log.info("PUT /v2/permissions/{} - method: {}, pattern: {}", 
            permissionId, permission.getHttpMethod(), permission.getUriPattern());
        DetailedPermission updatedPermission = detailedPermissionService.updatePermission(
            permissionId, permission.getHttpMethod(), permission.getUriPattern(), permission.getAllowed());
        return ResponseEntity.ok(updatedPermission);
    }
    
    /**
     * 세부 권한 삭제
     */
    @DeleteMapping("/{permissionId}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        log.info("DELETE /v2/permissions/{}", permissionId);
        detailedPermissionService.deletePermission(permissionId);
        return ResponseEntity.noContent().build();
    }
}