package com.wan.framework.permission.service;

import com.wan.framework.permission.entity.DetailedPermission;
import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.repository.DetailedPermissionRepository;
import com.wan.framework.permission.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * DetailedPermission Service
 */
@Service
@Transactional
public class DetailedPermissionService {
    
    @Autowired
    private DetailedPermissionRepository detailedPermissionRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    public List<DetailedPermission> getPermissionsByRole(Role role) {
        return detailedPermissionRepository.findByRole(role);
    }
    
    public Optional<DetailedPermission> getPermissionByRoleAndMethodAndPattern(
            Role role, String httpMethod, String uriPattern) {
        return detailedPermissionRepository.findByRoleAndHttpMethodAndUriPattern(role, httpMethod, uriPattern);
    }
    
    public DetailedPermission createPermission(DetailedPermission permission) {
        return detailedPermissionRepository.save(permission);
    }
    
    public DetailedPermission updatePermission(Long permissionId, 
                                               String httpMethod, 
                                               String uriPattern, 
                                               Boolean allowed) {
        DetailedPermission permission = detailedPermissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));
        permission.updateInfo(httpMethod, uriPattern, allowed);
        return detailedPermissionRepository.save(permission);
    }
    
    public void deletePermission(Long permissionId) {
        detailedPermissionRepository.deleteById(permissionId);
    }
}