package com.wan.framework.code.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.code.domain.CodeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 항목 Repository
 */
public interface CodeItemRepository extends JpaRepository<CodeItem, Long> {

    /**
     * 삭제되지 않은 코드 조회 (ID)
     */
    Optional<CodeItem> findByIdAndDataStateNot(Long id, DataStateCode dataState);

    /**
     * 그룹별 코드 조회 (삭제 제외, 정렬순서)
     */
    List<CodeItem> findAllByGroupCodeAndDataStateNotOrderBySortOrder(String groupCode, DataStateCode dataState);

    /**
     * 그룹별 활성화된 코드 조회
     */
    List<CodeItem> findAllByGroupCodeAndEnabledTrueAndDataStateNotOrderBySortOrder(
            String groupCode, DataStateCode dataState);

    /**
     * 그룹별 코드 조회 (페이징)
     */
    Page<CodeItem> findAllByGroupCodeAndDataStateNot(String groupCode, DataStateCode dataState, Pageable pageable);

    /**
     * 전체 코드 조회 (페이징)
     */
    Page<CodeItem> findAllByDataStateNot(DataStateCode dataState, Pageable pageable);

    /**
     * 그룹 내 코드 값 중복 체크
     */
    boolean existsByGroupCodeAndCodeValueAndDataStateNot(String groupCode, String codeValue, DataStateCode dataState);

    /**
     * 그룹별 코드 개수 조회
     */
    long countByGroupCodeAndDataStateNot(String groupCode, DataStateCode dataState);

    /**
     * 코드명으로 검색 (페이징)
     */
    Page<CodeItem> findAllByCodeNameContainingAndDataStateNot(String codeName, DataStateCode dataState, Pageable pageable);

    /**
     * 그룹 코드와 코드 값으로 조회
     */
    Optional<CodeItem> findByGroupCodeAndCodeValueAndDataStateNot(String groupCode, String codeValue, DataStateCode dataState);
}
