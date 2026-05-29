package com.wan.framework.permission.repository;

import com.wan.framework.permission.entity.DetailedPermission;
import com.wan.framework.permission.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DetailedPermission Repository
 */
@Repository
public interface DetailedPermissionRepository extends JpaRepository<DetailedPermission, Long> {
    List<DetailedPermission> findByRole(Role role);
    Optional<DetailedPermission> findByRoleAndHttpMethodAndUriPattern(
        Role role, String httpMethod, String uriPattern);
}