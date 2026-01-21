package com.wan.framework.permission.config;

import com.wan.framework.permission.domain.Role;
import com.wan.framework.permission.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Role 초기화
 * - 애플리케이션 시작 시 기본 Role 생성
 * - ROLE_ADMIN, ROLE_USER, ROLE_MANAGER
 */
@Slf4j
@Component
@Order(1)  // API 스캔보다 먼저 실행
@RequiredArgsConstructor
public class RoleInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("===== Role Initialization Started =====");

        try {
            // ROLE_ADMIN 생성
            createRoleIfNotExists("ROLE_ADMIN", "관리자", "시스템 모든 권한");

            // ROLE_USER 생성
            createRoleIfNotExists("ROLE_USER", "일반 사용자", "기본 사용자 권한");

            // ROLE_MANAGER 생성
            createRoleIfNotExists("ROLE_MANAGER", "매니저", "시스템 관리 권한");

            log.info("===== Role Initialization Completed =====");

        } catch (Exception e) {
            log.error("Failed to initialize roles", e);
            throw e;
        }
    }

    /**
     * Role이 존재하지 않으면 생성
     */
    private void createRoleIfNotExists(String roleCode, String roleName, String description) {
        if (roleRepository.existsByRoleCode(roleCode)) {
            log.debug("Role already exists: {}", roleCode);
            return;
        }

        Role role = Role.builder()
                .roleCode(roleCode)
                .roleName(roleName)
                .description(description)
                .build();

        roleRepository.save(role);
        log.info("Created role: {} ({})", roleCode, roleName);
    }
}
