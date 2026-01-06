package com.wan.framework.batch.service;

import com.wan.framework.batch.constant.BatchExceptionMessage;
import com.wan.framework.batch.domain.BatchExecution;
import com.wan.framework.batch.dto.BatchExecutionDTO;
import com.wan.framework.batch.exception.BatchException;
import com.wan.framework.batch.mapper.BatchExecutionMapper;
import com.wan.framework.batch.repository.BatchExecutionRepository;
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
 * 배치 실행 이력 서비스
 * - 실행 이력 조회
 * - 통계 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchHistoryService {

    private final BatchExecutionRepository batchExecutionRepository;
    private final BatchExecutionMapper batchExecutionMapper;

    /**
     * 실행 이력 조회 (ID)
     */
    public BatchExecutionDTO getExecution(Long id) {
        BatchExecution entity = batchExecutionRepository.findById(id)
                .orElseThrow(() -> new BatchException(BatchExceptionMessage.BATCH_EXECUTION_NOT_FOUND));

        return batchExecutionMapper.toDto(entity);
    }

    /**
     * 실행 이력 조회 (실행 ID)
     */
    public BatchExecutionDTO getExecutionByExecutionId(String executionId) {
        BatchExecution entity = batchExecutionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new BatchException(BatchExceptionMessage.BATCH_EXECUTION_NOT_FOUND));

        return batchExecutionMapper.toDto(entity);
    }

    /**
     * 배치 작업별 실행 이력 조회 (페이징)
     */
    public Page<BatchExecutionDTO> getHistoryByBatchJob(Long batchJobId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BatchExecution> entities = batchExecutionRepository
                .findByBatchJobIdOrderByStartTimeDesc(batchJobId, pageable);

        return entities.map(batchExecutionMapper::toDto);
    }

    /**
     * 배치 ID별 실행 이력 조회 (페이징)
     */
    public Page<BatchExecutionDTO> getHistoryByBatchId(String batchId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BatchExecution> entities = batchExecutionRepository
                .findByBatchIdOrderByStartTimeDesc(batchId, pageable);

        return entities.map(batchExecutionMapper::toDto);
    }

    /**
     * 상태별 실행 이력 조회 (페이징)
     */
    public Page<BatchExecutionDTO> getHistoryByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BatchExecution> entities = batchExecutionRepository
                .findByStatusOrderByStartTimeDesc(status, pageable);

        return entities.map(batchExecutionMapper::toDto);
    }

    /**
     * 트리거 타입별 실행 이력 조회 (페이징)
     */
    public Page<BatchExecutionDTO> getHistoryByTriggerType(String triggerType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BatchExecution> entities = batchExecutionRepository
                .findByTriggerTypeOrderByStartTimeDesc(triggerType, pageable);

        return entities.map(batchExecutionMapper::toDto);
    }

    /**
     * 기간별 실행 이력 조회
     */
    public List<BatchExecutionDTO> getHistoryByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<BatchExecution> entities = batchExecutionRepository
                .findByStartTimeBetweenOrderByStartTimeDesc(startDate, endDate);

        return entities.stream()
                .map(batchExecutionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 최근 실행 이력 조회 (최대 10건)
     */
    public List<BatchExecutionDTO> getRecentHistory(String batchId) {
        List<BatchExecution> entities = batchExecutionRepository
                .findTop10ByBatchIdOrderByStartTimeDesc(batchId);

        return entities.stream()
                .map(batchExecutionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 배치 실행 통계 조회
     */
    public Object getExecutionStats(Long batchJobId) {
        return batchExecutionRepository.getExecutionStats(batchJobId);
    }

    /**
     * 재시도 대상 조회
     */
    public List<BatchExecutionDTO> getRetryTargets() {
        List<BatchExecution> entities = batchExecutionRepository.findRetryTargets("FAIL");

        return entities.stream()
                .map(batchExecutionMapper::toDto)
                .collect(Collectors.toList());
    }
}
