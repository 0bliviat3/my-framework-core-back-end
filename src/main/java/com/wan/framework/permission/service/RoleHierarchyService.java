package com.wan.framework.permission.service;

import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.entity.RoleHierarchy;
import com.wan.framework.permission.repository.RoleHierarchyRepository;
import com.wan.framework.permission.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * RoleHierarchy Service
 */
@Service
@Transactional
public class RoleHierarchyService {
    
    @Autowired
    private RoleHierarchyRepository roleHierarchyRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    public List<RoleHierarchy> getRoleHierarchiesByParent(Role parentRole) {
        return roleHierarchyRepository.findByParentRole(parentRole);
    }
    
    public List<RoleHierarchy> getRoleHierarchiesByChild(Role childRole) {
        return roleHierarchyRepository.findByChildRole(childRole);
    }
    
    public Optional<RoleHierarchy> getRoleHierarchy(Role parentRole, Role childRole) {
        return roleHierarchyRepository.findByParentRoleAndChildRole(parentRole, childRole);
    }
    
    public RoleHierarchy createRoleHierarchy(Role parentRole, Role childRole) {
        RoleHierarchy roleHierarchy = RoleHierarchy.builder()
            .parentRole(parentRole)
            .childRole(childRole)
            .inherited(true)
            .build();
        return roleHierarchyRepository.save(roleHierarchy);
    }
    
    public void deleteRoleHierarchy(Long id) {
        roleHierarchyRepository.deleteById(id);
    }
}