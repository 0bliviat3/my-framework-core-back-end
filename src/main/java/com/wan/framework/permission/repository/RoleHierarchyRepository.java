package com.wan.framework.permission.repository;

import com.wan.framework.permission.entity.RoleHierarchy;
import com.wan.framework.permission.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RoleHierarchy Repository
 */
@Repository
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, Long> {
    Optional<RoleHierarchy> findByParentRoleAndChildRole(Role parentRole, Role childRole);
    List<RoleHierarchy> findByParentRole(Role parentRole);
    List<RoleHierarchy> findByChildRole(Role childRole);
}