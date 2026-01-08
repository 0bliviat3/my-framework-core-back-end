package com.wan.framework.permission.repository;

import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.domain.Role;
import com.wan.framework.permission.domain.RoleApiPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Role-API Permission Repository
 */
public interface RoleApiPermissionRepository extends JpaRepository<RoleApiPermission, Long> {

    /**
     * Role별 권한 목록 조회
     */
    List<RoleApiPermission> findByRole(Role role);

    /**
     * Role ID로 권한 목록 조회
     */
    @Query("SELECT p FROM RoleApiPermission p JOIN FETCH p.apiRegistry WHERE p.role.roleId = :roleId AND p.allowed = true")
    List<RoleApiPermission> findAllowedPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * Role과 API로 권한 조회
     */
    Optional<RoleApiPermission> findByRoleAndApiRegistry(Role role, ApiRegistry apiRegistry);

    /**
     * Role ID와 API ID로 권한 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM RoleApiPermission p " +
           "WHERE p.role.roleId = :roleId AND p.apiRegistry.apiId = :apiId AND p.allowed = true")
    boolean existsAllowedPermission(@Param("roleId") Long roleId, @Param("apiId") Long apiId);

    /**
     * Role에 속한 모든 권한 삭제
     */
    @Modifying
    @Query("DELETE FROM RoleApiPermission p WHERE p.role = :role")
    void deleteByRole(@Param("role") Role role);

    /**
     * API Registry에 속한 모든 권한 삭제
     */
    @Modifying
    @Query("DELETE FROM RoleApiPermission p WHERE p.apiRegistry = :apiRegistry")
    void deleteByApiRegistry(@Param("apiRegistry") ApiRegistry apiRegistry);
}
