package com.wan.framework.permission.repository;

import com.wan.framework.permission.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Role Repository
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Role 코드로 조회
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * Role 코드 존재 여부 확인
     */
    boolean existsByRoleCode(String roleCode);
}
