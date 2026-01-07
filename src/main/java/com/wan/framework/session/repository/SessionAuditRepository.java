package com.wan.framework.session.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.session.domain.SessionAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 세션 감사 로그 Repository
 */
public interface SessionAuditRepository extends JpaRepository<SessionAudit, Long> {

    /**
     * 사용자별 감사 로그 조회
     */
    List<SessionAudit> findByUserIdAndDataStateNotOrderByEventTimeDesc(
            String userId, DataStateCode dataState);

    /**
     * 세션 ID로 감사 로그 조회
     */
    List<SessionAudit> findBySessionIdAndDataStateNotOrderByEventTimeDesc(
            String sessionId, DataStateCode dataState);

    /**
     * 이벤트 타입별 감사 로그 조회
     */
    Page<SessionAudit> findByEventTypeAndDataStateNot(
            String eventType, DataStateCode dataState, Pageable pageable);

    /**
     * 기간별 감사 로그 조회
     */
    Page<SessionAudit> findByEventTimeBetweenAndDataStateNot(
            LocalDateTime startTime, LocalDateTime endTime,
            DataStateCode dataState, Pageable pageable);

    /**
     * 오늘 만료된 세션 수
     */
    @Query("SELECT COUNT(sa) FROM SessionAudit sa WHERE sa.eventType = :eventType " +
           "AND sa.eventTime >= :startTime AND sa.dataState != :dataState")
    long countExpiredToday(@Param("eventType") String eventType,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("dataState") DataStateCode dataState);
}
