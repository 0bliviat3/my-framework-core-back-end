package com.wan.framework.batch.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.batch.domain.BatchJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 배치 작업 Repository
 */
@Repository
public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {

    /**
     * 배치 ID로 조회
     */
    Optional<BatchJob> findByBatchIdAndDataStateNot(String batchId, DataStateCode dataState);

    /**
     * 배치 ID 존재 여부 확인
     */
    boolean existsByBatchIdAndDataStateNot(String batchId, DataStateCode dataState);

    /**
     * 활성화된 배치 목록 조회
     */
    List<BatchJob> findByEnabledTrueAndDataStateNot(DataStateCode dataState);

    /**
     * 전체 배치 목록 조회 (페이징)
     */
    Page<BatchJob> findByDataStateNot(DataStateCode dataState, Pageable pageable);

    /**
     * 활성화된 배치 목록 조회 (페이징)
     */
    Page<BatchJob> findByEnabledTrueAndDataStateNot(DataStateCode dataState, Pageable pageable);
}
