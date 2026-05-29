package com.wan.framework.permission.service;

import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Role Service
 */
@Service
@Transactional
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    public Optional<Role> getRoleById(Long roleId) {
        return roleRepository.findById(roleId);
    }
    
    public Optional<Role> getRoleByCode(String roleCode) {
        return roleRepository.findByRoleCode(roleCode);
    }
    
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }
    
    public Role updateRole(Long roleId, String roleName, String description) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));
        role.updateInfo(roleName, description);
        return roleRepository.save(role);
    }
    
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }
    
    public boolean existsByCode(String roleCode) {
        return roleRepository.existsByRoleCode(roleCode);
    }
}