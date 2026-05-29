package com.wan.framework.security.service;

import com.wan.framework.permission.entity.DetailedPermission;
import com.wan.framework.permission.entity.Role;
import com.wan.framework.permission.service.DetailedPermissionService;
import com.wan.framework.permission.service.RoleHierarchyService;
import com.wan.framework.user.domain.User;
import com.wan.framework.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Permission Validation Service
 */
@Service
public class PermissionValidationService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleHierarchyService roleHierarchyService;
    
    @Autowired
    private DetailedPermissionService detailedPermissionService;
    
    /**
     * 사용자 권한 검증 (세부 권한 기반)
     */
    public boolean hasPermission(String userId, String httpMethod, String uriPattern) {
        User user = userService.findByUserId(userId);
        if (user == null) return false;
        
        // 사용자의 모든 역할 가져오기
        Set<Role> roles = user.getRoleEntities();
        
        // 각 역할에 대해 세부 권한 확인
        for (Role role : roles) {
            // 해당 역할의 세부 권한 조회
            List<DetailedPermission> permissions = detailedPermissionService.getPermissionsByRole(role);
            
            for (DetailedPermission permission : permissions) {
                // HTTP 메서드와 URI 패턴 일치 여부 확인
                if (permission.getHttpMethod().equals(httpMethod) && 
                    matchesUriPattern(permission.getUriPattern(), uriPattern)) {
                    return permission.getAllowed();
                }
            }
        }
        
        // 권한이 없거나 일치하는 권한이 없는 경우 기본 거부
        return false;
    }
    
    /**
     * URI 패턴 매칭
     */
    private boolean matchesUriPattern(String pattern, String uri) {
        // 간단한 패턴 매칭 로직 (실제 구현에서는 더 복잡한 로직 필요)
        if (pattern.endsWith("*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return uri.startsWith(prefix);
        }
        return pattern.equals(uri);
    }
}