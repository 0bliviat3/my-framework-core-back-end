package com.wan.framework.permission.service;

import com.wan.framework.permission.entity.DetailedPermission;
import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.repository.DetailedPermissionRepository;
import com.wan.framework.permission.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DetailedPermissionService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
public class DetailedPermissionServiceTest {

    @Mock
    private DetailedPermissionRepository detailedPermissionRepository;
    
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private DetailedPermissionService detailedPermissionService;

    private Role testRole;
    private DetailedPermission permission;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
            .roleId(1L)
            .roleCode("ROLE_TEST")
            .roleName("테스트 역할")
            .build();
            
        permission = DetailedPermission.builder()
            .httpMethod("GET")
            .uriPattern("/api/test/*")
            .allowed(true)
            .build();
    }

    @Test
    void getPermissionsByRole_성공() {
        // Given
        List<DetailedPermission> permissions = Arrays.asList(permission);
        when(detailedPermissionRepository.findByRole(testRole)).thenReturn(permissions);

        // When
        List<DetailedPermission> result = detailedPermissionService.getPermissionsByRole(testRole);

        // Then
        assertEquals(1, result.size());
        verify(detailedPermissionRepository).findByRole(testRole);
    }

    @Test
    void getPermissionByRoleAndMethodAndPattern_존재하는_경우() {
        // Given
        when(detailedPermissionRepository.findByRoleAndHttpMethodAndUriPattern(
            testRole, "GET", "/api/test/*"))
            .thenReturn(Optional.of(permission));

        // When
        Optional<DetailedPermission> result = detailedPermissionService.getPermissionByRoleAndMethodAndPattern(
            testRole, "GET", "/api/test/*");

        // Then
        assertTrue(result.isPresent());
        assertEquals(permission, result.get());
    }

    @Test
    void createPermission_성공() {
        // Given
        when(detailedPermissionRepository.save(any(DetailedPermission.class)))
            .thenReturn(permission);

        // When
        DetailedPermission result = detailedPermissionService.createPermission(permission);

        // Then
        assertNotNull(result);
        verify(detailedPermissionRepository).save(any(DetailedPermission.class));
    }
}