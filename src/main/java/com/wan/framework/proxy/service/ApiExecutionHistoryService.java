package com.wan.framework.proxy.service;

import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import com.wan.framework.proxy.domain.ApiExecutionHistory;
import com.wan.framework.proxy.dto.ApiExecutionHistoryDTO;
import com.wan.framework.proxy.exception.ProxyException;
import com.wan.framework.proxy.mapper.ApiExecutionHistoryMapper;
import com.wan.framework.proxy.repository.ApiExecutionHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API 실행 이력 서비스
 * - 실행 이력 조회
 * - 통계 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiExecutionHistoryService {

    private final ApiExecutionHistoryRepository executionHistoryRepository;
    private final ApiExecutionHistoryMapper executionHistoryMapper;

    /**
     * 실행 이력 조회 (ID)
     */
    public ApiExecutionHistoryDTO getExecutionHistory(Long id) {
        ApiExecutionHistory entity = executionHistoryRepository.findById(id)
                .orElseThrow(() -> new ProxyException(ProxyExceptionMessage.EXECUTION_HISTORY_NOT_FOUND));

        return executionHistoryMapper.toDto(entity);
    }

    /**
     * API 엔드포인트별 실행 이력 조회 (페이징)
     */
    public Page<ApiExecutionHistoryDTO> getHistoryByEndpoint(Long apiEndpointId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApiExecutionHistory> entities = executionHistoryRepository
                .findByApiEndpointIdOrderByExecutedAtDesc(apiEndpointId, pageable);

        return entities.map(executionHistoryMapper::toDto);
    }

    /**
     * API 코드별 실행 이력 조회 (페이징)
     */
    public Page<ApiExecutionHistoryDTO> getHistoryByApiCode(String apiCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApiExecutionHistory> entities = executionHistoryRepository
                .findByApiCodeOrderByExecutedAtDesc(apiCode, pageable);

        return entities.map(executionHistoryMapper::toDto);
    }

    /**
     * 기간별 실행 이력 조회
     */
    public List<ApiExecutionHistoryDTO> getHistoryByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<ApiExecutionHistory> entities = executionHistoryRepository
                .findByExecutedAtBetweenOrderByExecutedAtDesc(startDate, endDate);

        return entities.stream()
                .map(executionHistoryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 성공/실패별 실행 이력 조회
     */
    public Page<ApiExecutionHistoryDTO> getHistoryBySuccess(Boolean isSuccess, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApiExecutionHistory> entities = executionHistoryRepository
                .findByIsSuccessOrderByExecutedAtDesc(isSuccess, pageable);

        return entities.map(executionHistoryMapper::toDto);
    }

    /**
     * 최근 실행 이력 조회 (최대 10건)
     */
    public List<ApiExecutionHistoryDTO> getRecentHistory(String apiCode) {
        List<ApiExecutionHistory> entities = executionHistoryRepository
                .findTop10ByApiCodeOrderByExecutedAtDesc(apiCode);

        return entities.stream()
                .map(executionHistoryMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * API 엔드포인트 실행 통계 조회
     */
    public Object getExecutionStats(Long apiEndpointId) {
        return executionHistoryRepository.getExecutionStats(apiEndpointId);
    }
}
