package com.wan.framework.code.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.code.domain.CodeGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 그룹 Repository
 */
public interface CodeGroupRepository extends JpaRepository<CodeGroup, String> {

    /**
     * 삭제되지 않은 그룹 조회 (groupCode)
     */
    Optional<CodeGroup> findByGroupCodeAndDataStateNot(String groupCode, DataStateCode dataState);

    /**
     * 삭제되지 않은 전체 그룹 조회 (페이징)
     */
    Page<CodeGroup> findAllByDataStateNot(DataStateCode dataState, Pageable pageable);

    /**
     * 삭제되지 않은 전체 그룹 조회 (목록)
     */
    List<CodeGroup> findAllByDataStateNot(DataStateCode dataState);

    /**
     * 활성화된 그룹 조회
     */
    List<CodeGroup> findAllByEnabledTrueAndDataStateNot(DataStateCode dataState);

    /**
     * 그룹 코드 중복 체크
     */
    boolean existsByGroupCodeAndDataStateNot(String groupCode, DataStateCode dataState);

    /**
     * 그룹명으로 검색 (페이징)
     */
    Page<CodeGroup> findAllByGroupNameContainingAndDataStateNot(String groupName, DataStateCode dataState, Pageable pageable);
}
