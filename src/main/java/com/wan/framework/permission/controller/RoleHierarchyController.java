package com.wan.framework.permission.controller;

import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.entity.RoleHierarchy;
import com.wan.framework.permission.service.RoleHierarchyService;
import com.wan.framework.permission.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role Hierarchy Management REST API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/roles/{roleId}/hierarchy")
public class RoleHierarchyController {
    
    private final RoleHierarchyService roleHierarchyService;
    private final RoleService roleService;
    
    /**
     * 역할 계층 구조 조회
     */
    @GetMapping
    public ResponseEntity<List<RoleHierarchy>> getRoleHierarchies(
            @PathVariable Long roleId) {
        log.info("GET /v2/roles/{}/hierarchy", roleId);
        Role role = roleService.getRoleById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        
        List<RoleHierarchy> hierarchies = roleHierarchyService.getRoleHierarchiesByParent(role);
        return ResponseEntity.ok(hierarchies);
    }
    
    /**
     * 상위 역할 설정
     */
    @PostMapping("/parent/{parentId}")
    public ResponseEntity<RoleHierarchy> setParentRole(
            @PathVariable Long roleId,
            @PathVariable Long parentId) {
        log.info("POST /v2/roles/{}/hierarchy/parent/{}", roleId, parentId);
        
        Role childRole = roleService.getRoleById(roleId)
            .orElseThrow(() -> new RuntimeException("Child role not found: " + roleId));
            
        Role parentRole = roleService.getRoleById(parentId)
            .orElseThrow(() -> new RuntimeException("Parent role not found: " + parentId));
        
        RoleHierarchy hierarchy = roleHierarchyService.createRoleHierarchy(parentRole, childRole);
        return ResponseEntity.ok(hierarchy);
    }
}