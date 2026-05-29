package com.wan.framework.permission.service;

import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.entity.RoleHierarchy;
import com.wan.framework.permission.repository.RoleHierarchyRepository;
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
 * RoleHierarchyService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
public class RoleHierarchyServiceTest {

    @Mock
    private RoleHierarchyRepository roleHierarchyRepository;
    
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleHierarchyService roleHierarchyService;

    private Role parentRole;
    private Role childRole;
    private RoleHierarchy roleHierarchy;

    @BeforeEach
    void setUp() {
        parentRole = Role.builder()
            .roleId(1L)
            .roleCode("ROLE_PARENT")
            .roleName("부모 역할")
            .build();
            
        childRole = Role.builder()
            .roleId(2L)
            .roleCode("ROLE_CHILD")
            .roleName("자식 역할")
            .build();
            
        roleHierarchy = RoleHierarchy.builder()
            .parentRole(parentRole)
            .childRole(childRole)
            .inherited(true)
            .build();
    }

    @Test
    void getRoleHierarchiesByParent_성공() {
        // Given
        List<RoleHierarchy> hierarchies = Arrays.asList(roleHierarchy);
        when(roleHierarchyRepository.findByParentRole(parentRole)).thenReturn(hierarchies);

        // When
        List<RoleHierarchy> result = roleHierarchyService.getRoleHierarchiesByParent(parentRole);

        // Then
        assertEquals(1, result.size());
        verify(roleHierarchyRepository).findByParentRole(parentRole);
    }

    @Test
    void getRoleHierarchy_존재하는_경우() {
        // Given
        when(roleHierarchyRepository.findByParentRoleAndChildRole(parentRole, childRole))
            .thenReturn(Optional.of(roleHierarchy));

        // When
        Optional<RoleHierarchy> result = roleHierarchyService.getRoleHierarchy(parentRole, childRole);

        // Then
        assertTrue(result.isPresent());
        assertEquals(roleHierarchy, result.get());
    }

    @Test
    void createRoleHierarchy_성공() {
        // Given
        when(roleHierarchyRepository.save(any(RoleHierarchy.class))).thenReturn(roleHierarchy);

        // When
        RoleHierarchy result = roleHierarchyService.createRoleHierarchy(parentRole, childRole);

        // Then
        assertNotNull(result);
        verify(roleHierarchyRepository).save(any(RoleHierarchy.class));
    }
}