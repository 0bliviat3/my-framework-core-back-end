package com.wan.framework.proxy.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.proxy.domain.ApiEndpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * API 엔드포인트 Repository
 */
@Repository
public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, Long> {

    /**
     * API 코드로 조회
     */
    Optional<ApiEndpoint> findByApiCodeAndDataStateNot(String apiCode, DataStateCode dataState);

    /**
     * 활성화된 API 목록 조회 (페이징)
     */
    Page<ApiEndpoint> findByDataStateNotAndIsEnabledTrue(DataStateCode dataState, Pageable pageable);

    /**
     * 전체 API 목록 조회 (페이징)
     */
    Page<ApiEndpoint> findByDataStateNot(DataStateCode dataState, Pageable pageable);

    /**
     * API 코드 존재 여부 확인
     */
    boolean existsByApiCodeAndDataStateNot(String apiCode, DataStateCode dataState);
}
