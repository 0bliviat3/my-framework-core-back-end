package com.wan.framework.permission.service;

import com.wan.framework.permission.constant.ApiStatus;
import com.wan.framework.permission.constant.PermissionExceptionMessage;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.domain.Role;
import com.wan.framework.permission.domain.RoleApiPermission;
import com.wan.framework.permission.dto.ApiRegistryDTO;
import com.wan.framework.permission.dto.RoleApiPermissionDTO;
import com.wan.framework.permission.dto.RoleDTO;
import com.wan.framework.permission.exception.PermissionException;
import com.wan.framework.permission.repository.ApiRegistryRepository;
import com.wan.framework.permission.repository.RoleApiPermissionRepository;
import com.wan.framework.permission.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 권한 관리 서비스
 * - Role 생성/수정/삭제
 * - Role-API 권한 매핑 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleRepository roleRepository;
    private final ApiRegistryRepository apiRegistryRepository;
    private final RoleApiPermissionRepository roleApiPermissionRepository;
    private final PermissionCacheService permissionCacheService;

    /**
     * Role 생성
     */
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        // 중복 확인
        if (roleRepository.existsByRoleCode(roleDTO.getRoleCode())) {
            throw new PermissionException(PermissionExceptionMessage.ROLE_ALREADY_EXISTS);
        }

        Role role = Role.builder()
                .roleCode(roleDTO.getRoleCode())
                .roleName(roleDTO.getRoleName())
                .description(roleDTO.getDescription())
                .build();

        role = roleRepository.save(role);

        log.info("Role created: {}", role.getRoleCode());

        return toDTO(role);
    }

    /**
     * Role 수정
     */
    @Transactional
    public RoleDTO updateRole(Long roleId, RoleDTO roleDTO) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new PermissionException(PermissionExceptionMessage.ROLE_NOT_FOUND));

        role.updateInfo(roleDTO.getRoleName(), roleDTO.getDescription());

        log.info("Role updated: {}", role.getRoleCode());

        // 캐시 무효화
        permissionCacheService.invalidateRoleCache(role.getRoleCode());

        return toDTO(role);
    }

    /**
     * Role 삭제
     */
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new PermissionException(PermissionExceptionMessage.ROLE_NOT_FOUND));

        // ADMIN Role은 삭제 불가
        if (role.isAdmin()) {
            throw new PermissionException(PermissionExceptionMessage.CANNOT_DELETE_ADMIN_ROLE);
        }

        // 관련 권한 매핑 삭제
        roleApiPermissionRepository.deleteByRole(role);

        // Role 삭제
        roleRepository.delete(role);

        log.info("Role deleted: {}", role.getRoleCode());

        // 캐시 무효화
        permissionCacheService.invalidateRoleCache(role.getRoleCode());
    }

    /**
     * Role에 API 권한 부여
     */
    @Transactional
    public void grantPermission(Long roleId, Long apiId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new PermissionException(PermissionExceptionMessage.ROLE_NOT_FOUND));

        ApiRegistry api = apiRegistryRepository.findById(apiId)
                .orElseThrow(() -> new PermissionException(PermissionExceptionMessage.API_NOT_FOUND));

        // 기존 권한 확인
        roleApiPermissionRepository.findByRoleAndApiRegistry(role, api)
                .ifPresentOrElse(
                        permission -> permission.setAllowed(true),
                        () -> {
                            RoleApiPermission newPermission = RoleApiPermission.builder()
                                    .role(role)
                                    .apiRegistry(api)
                                    .allowed(true)
                                    .build();
                            roleApiPermissionRepository.save(newPermission);
                        }
                );

        log.info("Permission granted: role={}, api={} {}", role.getRoleCode(), api.getHttpMethod(), api.getUriPattern());

        // 캐시 무효화
        permissionCacheService.invalidateRoleCache(role.getRoleCode());
    }

    /**
     * Role의 API 권한 제거
     */
    @Transactional
    public void revokePermission(Long roleId, Long apiId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new PermissionException(PermissionExceptionMessage.ROLE_NOT_FOUND));

        ApiRegistry api = apiRegistryRepository.findById(apiId)
                .orElseThrow(() -> new PermissionException(PermissionExceptionMessage.API_NOT_FOUND));

        roleApiPermissionRepository.findByRoleAndApiRegistry(role, api)
                .ifPresent(permission -> {
                    permission.setAllowed(false);
                    roleApiPermissionRepository.save(permission);
                });

        log.info("Permission revoked: role={}, api={} {}", role.getRoleCode(), api.getHttpMethod(), api.getUriPattern());

        // 캐시 무효화
        permissionCacheService.invalidateRoleCache(role.getRoleCode());
    }

    /**
     * Role 조회
     */
    public RoleDTO getRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new PermissionException(PermissionExceptionMessage.ROLE_NOT_FOUND));
        return toDTO(role);
    }

    /**
     * 모든 Role 조회
     */
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 활성 API 목록 조회
     */
    public List<ApiRegistryDTO> getActiveApis() {
        return apiRegistryRepository.findByStatus(ApiStatus.ACTIVE).stream()
                .map(this::toApiRegistryDTO)
                .toList();
    }

    /**
     * Role별 권한 목록 조회
     */
    public List<RoleApiPermissionDTO> getRolePermissions(Long roleId) {
        return roleApiPermissionRepository.findAllowedPermissionsByRoleId(roleId).stream()
                .map(this::toRoleApiPermissionDTO)
                .toList();
    }

    // Helper Methods
    private RoleDTO toDTO(Role role) {
        return RoleDTO.builder()
                .roleId(role.getRoleId())
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }

    private ApiRegistryDTO toApiRegistryDTO(ApiRegistry apiRegistry) {
        return ApiRegistryDTO.builder()
                .apiId(apiRegistry.getApiId())
                .serviceId(apiRegistry.getServiceId())
                .httpMethod(apiRegistry.getHttpMethod())
                .uriPattern(apiRegistry.getUriPattern())
                .controllerName(apiRegistry.getControllerName())
                .handlerMethod(apiRegistry.getHandlerMethod())
                .description(apiRegistry.getDescription())
                .authRequired(apiRegistry.getAuthRequired())
                .status(apiRegistry.getStatus())
                .createdAt(apiRegistry.getCreatedAt())
                .updatedAt(apiRegistry.getUpdatedAt())
                .build();
    }

    private RoleApiPermissionDTO toRoleApiPermissionDTO(RoleApiPermission permission) {
        return RoleApiPermissionDTO.builder()
                .permissionId(permission.getPermissionId())
                .roleId(permission.getRole().getRoleId())
                .roleCode(permission.getRole().getRoleCode())
                .apiId(permission.getApiRegistry().getApiId())
                .httpMethod(permission.getApiRegistry().getHttpMethod())
                .uriPattern(permission.getApiRegistry().getUriPattern())
                .allowed(permission.getAllowed())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}
