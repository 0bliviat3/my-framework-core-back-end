package com.wan.framework.history.repository;

import com.wan.framework.history.domain.ErrorHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ErrorHistoryRepository extends JpaRepository<ErrorHistory, Long> {

    // URL로 에러 로그 검색
    Page<ErrorHistory> findByRequestURLContaining(String url, Pageable pageable);

    // 에러 메시지로 검색
    Page<ErrorHistory> findByErrorMessageContaining(String message, Pageable pageable);

    // 기간별 검색
    Page<ErrorHistory> findByEventTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 특정 시간 이후 에러 로그 조회
    Page<ErrorHistory> findByEventTimeAfter(LocalDateTime since, Pageable pageable);
}
