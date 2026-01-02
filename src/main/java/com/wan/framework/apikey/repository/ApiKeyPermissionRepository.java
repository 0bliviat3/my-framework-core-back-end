package com.wan.framework.apikey.repository;

import com.wan.framework.apikey.domain.ApiKey;
import com.wan.framework.apikey.domain.ApiKeyPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiKeyPermissionRepository extends JpaRepository<ApiKeyPermission, Long> {

    // ApiKey 엔티티로 조회
    List<ApiKeyPermission> findByApiKey(ApiKey apiKey);

    // ApiKey와 permission으로 존재 여부 확인
    boolean existsByApiKeyAndPermission(ApiKey apiKey, String permission);

    // ApiKey와 permission으로 삭제
    void deleteByApiKeyAndPermission(ApiKey apiKey, String permission);

    // ApiKey ID로 모두 삭제
    void deleteByApiKey(ApiKey apiKey);
}
