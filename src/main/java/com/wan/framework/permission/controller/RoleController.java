package com.wan.framework.permission.controller;

import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role Management REST API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/roles")
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 역할 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("GET /v2/roles");
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }
    
    /**
     * 특정 역할 조회
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<Role> getRole(@PathVariable Long roleId) {
        log.info("GET /v2/roles/{}", roleId);
        return roleService.getRoleById(roleId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 역할 생성 (ROLE_ADMIN만 접근 가능)
     */
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        log.info("POST /v2/roles - roleCode: {}", role.getRoleCode());
        Role createdRole = roleService.createRole(role);
        return ResponseEntity.ok(createdRole);
    }
    
    /**
     * 역할 수정 (ROLE_ADMIN만 접근 가능)
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<Role> updateRole(
            @PathVariable Long roleId,
            @RequestBody Role role) {
        log.info("PUT /v2/roles/{} - roleCode: {}", roleId, role.getRoleCode());
        Role updatedRole = roleService.updateRole(roleId, role.getRoleName(), role.getDescription());
        return ResponseEntity.ok(updatedRole);
    }
    
    /**
     * 역할 삭제 (ROLE_ADMIN만 접근 가능)
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        log.info("DELETE /v2/roles/{}", roleId);
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
}