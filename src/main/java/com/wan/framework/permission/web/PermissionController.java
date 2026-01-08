package com.wan.framework.permission.web;

import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.domain.RoleApiPermission;
import com.wan.framework.permission.dto.RoleDTO;
import com.wan.framework.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 권한 관리 Controller
 * - Role 관리
 * - Role-API 권한 매핑 관리
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Role 생성
     */
    @PostMapping("/roles")
    public ResponseEntity<RoleDTO> createRole(@RequestBody RoleDTO roleDTO) {
        log.info("POST /permissions/roles - roleCode: {}", roleDTO.getRoleCode());
        RoleDTO created = permissionService.createRole(roleDTO);
        return ResponseEntity.ok(created);
    }

    /**
     * Role 수정
     */
    @PutMapping("/roles/{roleId}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long roleId, @RequestBody RoleDTO roleDTO) {
        log.info("PUT /permissions/roles/{} - roleName: {}", roleId, roleDTO.getRoleName());
        RoleDTO updated = permissionService.updateRole(roleId, roleDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Role 삭제
     */
    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<String> deleteRole(@PathVariable Long roleId) {
        log.info("DELETE /permissions/roles/{}", roleId);
        permissionService.deleteRole(roleId);
        return ResponseEntity.ok("Role deleted successfully");
    }

    /**
     * Role 조회
     */
    @GetMapping("/roles/{roleId}")
    public ResponseEntity<RoleDTO> getRole(@PathVariable Long roleId) {
        log.info("GET /permissions/roles/{}", roleId);
        RoleDTO role = permissionService.getRole(roleId);
        return ResponseEntity.ok(role);
    }

    /**
     * 모든 Role 조회
     */
    @GetMapping("/roles")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("GET /permissions/roles");
        List<RoleDTO> roles = permissionService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * 활성 API 목록 조회
     */
    @GetMapping("/apis")
    public ResponseEntity<List<ApiRegistry>> getActiveApis() {
        log.info("GET /permissions/apis");
        List<ApiRegistry> apis = permissionService.getActiveApis();
        return ResponseEntity.ok(apis);
    }

    /**
     * Role별 권한 목록 조회
     */
    @GetMapping("/roles/{roleId}/permissions")
    public ResponseEntity<List<RoleApiPermission>> getRolePermissions(@PathVariable Long roleId) {
        log.info("GET /permissions/roles/{}/permissions", roleId);
        List<RoleApiPermission> permissions = permissionService.getRolePermissions(roleId);
        return ResponseEntity.ok(permissions);
    }

    /**
     * Role에 API 권한 부여
     */
    @PostMapping("/roles/{roleId}/apis/{apiId}")
    public ResponseEntity<String> grantPermission(@PathVariable Long roleId, @PathVariable Long apiId) {
        log.info("POST /permissions/roles/{}/apis/{}", roleId, apiId);
        permissionService.grantPermission(roleId, apiId);
        return ResponseEntity.ok("Permission granted successfully");
    }

    /**
     * Role의 API 권한 제거
     */
    @DeleteMapping("/roles/{roleId}/apis/{apiId}")
    public ResponseEntity<String> revokePermission(@PathVariable Long roleId, @PathVariable Long apiId) {
        log.info("DELETE /permissions/roles/{}/apis/{}", roleId, apiId);
        permissionService.revokePermission(roleId, apiId);
        return ResponseEntity.ok("Permission revoked successfully");
    }
}
