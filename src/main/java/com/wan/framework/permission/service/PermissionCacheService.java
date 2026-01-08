package com.wan.framework.permission.service;

import com.wan.framework.permission.constant.PermissionConstants;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.domain.RoleApiPermission;
import com.wan.framework.permission.repository.RoleApiPermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 권한 캐싱 서비스
 * - Role별 접근 가능한 API 목록을 Redis에 캐싱
 * - O(1) 성능으로 권한 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleApiPermissionRepository roleApiPermissionRepository;

    /**
     * Role별 접근 가능한 API 목록 캐싱
     * Key: ROLE_API_PERMISSION::{roleCode}
     * Value: Set<apiIdentifier> (serviceId::httpMethod::uriPattern)
     */
    public void cacheRolePermissions(Long roleId, String roleCode) {
        try {
            String cacheKey = PermissionConstants.CACHE_ROLE_API_PERMISSION + roleCode;

            // 권한 조회
            List<RoleApiPermission> permissions = roleApiPermissionRepository.findAllowedPermissionsByRoleId(roleId);

            // API 식별자 Set 생성
            Set<String> apiIdentifiers = permissions.stream()
                    .map(RoleApiPermission::getApiRegistry)
                    .map(ApiRegistry::getApiIdentifier)
                    .collect(Collectors.toSet());

            // Redis에 저장
            redisTemplate.opsForSet().add(cacheKey, apiIdentifiers.toArray());
            redisTemplate.expire(cacheKey, PermissionConstants.CACHE_TTL_HOURS, TimeUnit.HOURS);

            log.info("Cached permissions for role: {} ({} APIs)", roleCode, apiIdentifiers.size());

        } catch (Exception e) {
            log.error("Failed to cache role permissions: {}", roleCode, e);
        }
    }

    /**
     * 권한 확인 (Redis 캐시 조회)
     * @return true if permission exists, false otherwise
     */
    public boolean hasPermission(String roleCode, String apiIdentifier) {
        try {
            String cacheKey = PermissionConstants.CACHE_ROLE_API_PERMISSION + roleCode;

            // Redis Set에 API 식별자가 있는지 확인 - O(1)
            Boolean isMember = redisTemplate.opsForSet().isMember(cacheKey, apiIdentifier);

            return Boolean.TRUE.equals(isMember);

        } catch (Exception e) {
            log.error("Failed to check permission from cache: role={}, api={}", roleCode, apiIdentifier, e);
            return false;  // Fail-Closed: 캐시 조회 실패 시 권한 없음으로 처리
        }
    }

    /**
     * Role 권한 캐시 무효화
     */
    public void invalidateRoleCache(String roleCode) {
        try {
            String cacheKey = PermissionConstants.CACHE_ROLE_API_PERMISSION + roleCode;
            redisTemplate.delete(cacheKey);

            log.info("Invalidated permission cache for role: {}", roleCode);

        } catch (Exception e) {
            log.error("Failed to invalidate role cache: {}", roleCode, e);
        }
    }

    /**
     * 모든 Role 권한 캐시 무효화
     */
    public void invalidateAllRoleCache() {
        try {
            String pattern = PermissionConstants.CACHE_ROLE_API_PERMISSION + "*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Invalidated all role permission caches: {} keys", keys.size());
            }

        } catch (Exception e) {
            log.error("Failed to invalidate all role caches", e);
        }
    }

    /**
     * 캐시 워밍업 (애플리케이션 시작 시 또는 필요 시)
     */
    public void warmUpCache(Long roleId, String roleCode) {
        cacheRolePermissions(roleId, roleCode);
    }
}
