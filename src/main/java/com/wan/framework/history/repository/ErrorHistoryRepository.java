package com.wan.framework.history.repository;

import com.wan.framework.history.domain.ErrorHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorHistoryRepository extends JpaRepository<ErrorHistory, Long> {
}
