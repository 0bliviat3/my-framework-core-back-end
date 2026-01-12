package com.wan.framework.permission.repository;

import com.wan.framework.permission.constant.ApiStatus;
import com.wan.framework.permission.domain.ApiRegistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * API Registry Repository
 */
public interface ApiRegistryRepository extends JpaRepository<ApiRegistry, Long> {

    /**
     * API 식별자로 조회 (serviceId + httpMethod + uriPattern)
     */
    Optional<ApiRegistry> findByServiceIdAndHttpMethodAndUriPattern(
        String serviceId, String httpMethod, String uriPattern
    );

    /**
     * 서비스별 활성 API 목록 조회
     */
    List<ApiRegistry> findByServiceIdAndStatus(String serviceId, ApiStatus status);

    /**
     * 모든 활성 API 조회
     */
    List<ApiRegistry> findByStatus(ApiStatus status);

    /**
     * HTTP Method와 URI Pattern으로 활성 API 조회 (정확한 매칭)
     */
    @Query("SELECT a FROM ApiRegistry a WHERE a.httpMethod = :method AND a.uriPattern = :uri AND a.status = 'ACTIVE'")
    Optional<ApiRegistry> findActiveApiByMethodAndUri(
        @Param("method") String method,
        @Param("uri") String uri
    );

    /**
     * HTTP Method로 모든 활성 API 조회 (패턴 매칭용)
     */
    @Query("SELECT a FROM ApiRegistry a WHERE a.httpMethod = :method AND a.status = 'ACTIVE'")
    List<ApiRegistry> findActiveApisByMethod(@Param("method") String method);

    /**
     * 인증 필요 여부로 조회
     */
    List<ApiRegistry> findByAuthRequired(Boolean authRequired);
}
