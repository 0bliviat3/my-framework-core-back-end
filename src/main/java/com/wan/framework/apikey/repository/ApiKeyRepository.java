package com.wan.framework.apikey.repository;

import com.wan.framework.apikey.domain.ApiKey;
import com.wan.framework.base.constant.DataStateCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);

    Optional<ApiKey> findByApiKeyAndDataStateCodeNot(String apiKey, DataStateCode dataStateCode);

    Page<ApiKey> findByCreatedByAndDataStateCodeNotOrderByCreatedAtDesc(
            String createdBy, DataStateCode dataStateCode, Pageable pageable);

    Page<ApiKey> findByDataStateCodeNotOrderByCreatedAtDesc(
            DataStateCode dataStateCode, Pageable pageable);

    long countByCreatedByAndDataStateCodeNot(String createdBy, DataStateCode dataStateCode);
}
