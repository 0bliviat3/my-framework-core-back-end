package com.wan.framework.batch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wan.framework.batch.constant.BatchExceptionMessage;
import com.wan.framework.batch.constant.BatchStatus;
import com.wan.framework.batch.constant.BatchTriggerType;
import com.wan.framework.batch.domain.BatchExecution;
import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.dto.BatchExecutionDTO;
import com.wan.framework.batch.dto.BatchExecutionRequest;
import com.wan.framework.batch.exception.BatchException;
import com.wan.framework.batch.mapper.BatchExecutionMapper;
import com.wan.framework.batch.repository.BatchExecutionRepository;
import com.wan.framework.proxy.dto.ProxyExecutionRequest;
import com.wan.framework.proxy.dto.ProxyExecutionResponse;
import com.wan.framework.proxy.service.ApiExecutionService;
import com.wan.framework.proxy.service.ApiEndpointService;
import com.wan.framework.redis.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 배치 실행 서비스
 * - Proxy API 연계를 통한 배치 실행
 * - Redis 분산 락을 통한 중복 실행 방지
 * - 실행 이력 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchExecutionService {

    private final BatchExecutionRepository batchExecutionRepository;
    private final BatchExecutionMapper batchExecutionMapper;
    private final DistributedLockService distributedLockService;
    private final ApiEndpointService apiEndpointService;
    private final ApiExecutionService apiExecutionService;
    private final ObjectMapper objectMapper;

    private static final String LOCK_KEY_PREFIX = "batch:";
    private static final String LOCK_KEY_SUFFIX = ":lock";

    /**
     * 배치 실행 (스케줄러용)
     */
    @Transactional
    public BatchExecutionDTO executeBatch(BatchJob batchJob, BatchTriggerType triggerType, String executedBy) {
        log.info("Starting batch execution: {} ({})", batchJob.getBatchId(), triggerType);

        // 실행 ID 생성
        String executionId = UUID.randomUUID().toString();

        // 동시 실행 체크
        if (!batchJob.getAllowConcurrent()) {
            // 실행 중인 작업 확인
            batchExecutionRepository.findByBatchIdAndStatus(
                    batchJob.getBatchId(),
                    BatchStatus.RUNNING.name()
            ).ifPresent(running -> {
                log.warn("Batch already running: {}", batchJob.getBatchId());
                throw new BatchException(BatchExceptionMessage.BATCH_ALREADY_RUNNING);
            });
        }

        // Redis 분산 락 획득
        String lockKey = LOCK_KEY_PREFIX + batchJob.getBatchId() + LOCK_KEY_SUFFIX;
        String lockValue = null;

        try {
            // TTL = timeout + 버퍼(10초)
            long lockTtl = batchJob.getTimeoutSeconds() + 10;
            lockValue = distributedLockService.acquireLock(lockKey, lockTtl);

            if (lockValue == null) {
                log.warn("Failed to acquire lock for batch: {}", batchJob.getBatchId());
                throw new BatchException(BatchExceptionMessage.BATCH_LOCK_ACQUISITION_FAILED);
            }

            // 실행 이력 생성
            BatchExecution execution = createExecution(batchJob, executionId, triggerType, executedBy, 0, null);
            execution.setStatus(BatchStatus.RUNNING.name());
            BatchExecution savedExecution = batchExecutionRepository.save(execution);

            // Proxy API 실행 (타임아웃 체크 포함)
            ProxyExecutionResponse proxyResponse = executeProxyApiWithTimeout(
                    batchJob,
                    executionId,
                    executedBy,
                    savedExecution
            );

            // 실행 완료 처리
            completeExecution(savedExecution, proxyResponse);

            return batchExecutionMapper.toDto(savedExecution);

        } catch (Exception e) {
            log.error("Batch execution failed: {} - {}", batchJob.getBatchId(), e.getMessage());

            // 실패 이력 저장
            BatchExecution failedExecution = batchExecutionRepository.findByExecutionId(executionId)
                    .orElse(createExecution(batchJob, executionId, triggerType, executedBy, 0, null));

            failedExecution.setStatus(BatchStatus.FAIL.name());
            failedExecution.setEndTime(LocalDateTime.now());
            failedExecution.setExecutionTimeMs(
                    Duration.between(failedExecution.getStartTime(), LocalDateTime.now()).toMillis()
            );
            failedExecution.setErrorMessage(e.getMessage());
            failedExecution.setStackTrace(getStackTrace(e));

            batchExecutionRepository.save(failedExecution);

            throw new BatchException(BatchExceptionMessage.BATCH_EXECUTION_FAILED, e);

        } finally {
            // Redis 락 해제
            if (lockValue != null) {
                try {
                    distributedLockService.releaseLock(lockKey, lockValue);
                    log.info("Released lock for batch: {}", batchJob.getBatchId());
                } catch (Exception e) {
                    log.error("Failed to release lock: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 배치 수동 실행
     */
    @Transactional
    public BatchExecutionDTO executeManual(BatchJob batchJob, BatchExecutionRequest request) {
        log.info("Manual batch execution: {}", batchJob.getBatchId());

        // 비활성화 체크
        if (!batchJob.getEnabled()) {
            throw new BatchException(BatchExceptionMessage.BATCH_JOB_DISABLED);
        }

        return executeBatch(batchJob, BatchTriggerType.MANUAL, request.getExecutedBy());
    }

    /**
     * 배치 재시도
     */
    @Transactional
    public BatchExecutionDTO retryBatch(String originalExecutionId, String executedBy) {
        log.info("Retrying batch execution: {}", originalExecutionId);

        // 원본 실행 이력 조회
        BatchExecution originalExecution = batchExecutionRepository.findByExecutionId(originalExecutionId)
                .orElseThrow(() -> new BatchException(BatchExceptionMessage.BATCH_EXECUTION_NOT_FOUND));

        // 재시도 가능 상태 체크
        if (BatchStatus.SUCCESS.name().equals(originalExecution.getStatus())) {
            throw new BatchException(BatchExceptionMessage.CANNOT_RETRY_SUCCESS);
        }
        if (BatchStatus.RUNNING.name().equals(originalExecution.getStatus())) {
            throw new BatchException(BatchExceptionMessage.CANNOT_RETRY_RUNNING);
        }
        if (!BatchStatus.FAIL.name().equals(originalExecution.getStatus()) &&
            !BatchStatus.TIMEOUT.name().equals(originalExecution.getStatus())) {
            throw new BatchException(BatchExceptionMessage.RETRY_NOT_ALLOWED);
        }

        // 최대 재시도 횟수 초과 체크
        // BatchJob을 조회하여 maxRetryCount 확인 필요
        // 여기서는 간단히 retryCount만 체크 (실제로는 BatchJob 조회 필요)
        if (originalExecution.getRetryCount() >= 3) {
            throw new BatchException(BatchExceptionMessage.MAX_RETRY_EXCEEDED);
        }

        // 배치 작업 조회 (실제로는 BatchJobService에서 조회해야 하지만 여기서는 간단히 처리)
        // 실제 구현 시 BatchJobService 주입 필요

        // 새 실행 ID 생성
        String newExecutionId = UUID.randomUUID().toString();

        // 재시도 실행 이력 생성
        BatchExecution retryExecution = BatchExecution.builder()
                .executionId(newExecutionId)
                .batchJobId(originalExecution.getBatchJobId())
                .batchId(originalExecution.getBatchId())
                .batchName(originalExecution.getBatchName())
                .status(BatchStatus.RETRY.name())
                .triggerType(BatchTriggerType.RETRY.name())
                .executionParameters(originalExecution.getExecutionParameters())
                .retryCount(originalExecution.getRetryCount() + 1)
                .originalExecutionId(originalExecutionId)
                .serverInfo(getServerInfo())
                .executedBy(executedBy)
                .startTime(LocalDateTime.now())
                .build();

        batchExecutionRepository.save(retryExecution);

        // 재시도 로직은 스케줄러에서 처리
        log.info("Retry execution created: {}", newExecutionId);

        return batchExecutionMapper.toDto(retryExecution);
    }

    /**
     * Proxy API 실행 (타임아웃 체크 포함)
     */
    private ProxyExecutionResponse executeProxyApiWithTimeout(
            BatchJob batchJob,
            String executionId,
            String executedBy,
            BatchExecution execution) {

        LocalDateTime startTime = LocalDateTime.now();
        long timeoutMillis = batchJob.getTimeoutSeconds() * 1000L;

        try {
            // 실행 파라미터 준비
            Map<String, Object> parameters = prepareExecutionParameters(batchJob, executionId);

            // Proxy API 요청 생성
            ProxyExecutionRequest proxyRequest = ProxyExecutionRequest.builder()
                    .apiCode(batchJob.getProxyApiCode())
                    .parameters(parameters)
                    .executionTrigger("BATCH")
                    .executedBy(executedBy)
                    .build();

            // API 엔드포인트 조회
            var endpoint = apiEndpointService.getApiEndpointByCode(batchJob.getProxyApiCode());

            // API 실행
            ProxyExecutionResponse response = apiExecutionService.execute(endpoint, proxyRequest);

            // 실행 시간 체크
            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();
            if (executionTime > timeoutMillis) {
                log.warn("Batch execution timeout: {} ({}ms > {}ms)",
                        batchJob.getBatchId(), executionTime, timeoutMillis);

                // 타임아웃 상태로 업데이트
                execution.setStatus(BatchStatus.TIMEOUT.name());
                execution.setEndTime(LocalDateTime.now());
                execution.setExecutionTimeMs(executionTime);
                execution.setErrorMessage(String.format("Execution timeout: %dms exceeded limit of %dms",
                        executionTime, timeoutMillis));
                batchExecutionRepository.save(execution);

                throw new BatchException(BatchExceptionMessage.BATCH_TIMEOUT);
            }

            return response;

        } catch (Exception e) {
            // 실행 시간 체크
            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();
            if (executionTime > timeoutMillis) {
                log.error("Batch execution timeout during error: {} ({}ms > {}ms)",
                        batchJob.getBatchId(), executionTime, timeoutMillis);

                // 타임아웃 상태로 업데이트
                execution.setStatus(BatchStatus.TIMEOUT.name());
                execution.setEndTime(LocalDateTime.now());
                execution.setExecutionTimeMs(executionTime);
                execution.setErrorMessage(String.format("Execution timeout with error: %dms exceeded limit of %dms - %s",
                        executionTime, timeoutMillis, e.getMessage()));
                execution.setStackTrace(getStackTrace(e));
                batchExecutionRepository.save(execution);

                throw new BatchException(BatchExceptionMessage.BATCH_TIMEOUT, e);
            }

            log.error("Proxy API execution failed: {}", e.getMessage());
            throw new BatchException(BatchExceptionMessage.PROXY_API_EXECUTION_FAILED, e);
        }
    }

    /**
     * Proxy API 실행
     */
    private ProxyExecutionResponse executeProxyApi(BatchJob batchJob, String executionId, String executedBy) {
        try {
            // 실행 파라미터 준비
            Map<String, Object> parameters = prepareExecutionParameters(batchJob, executionId);

            // Proxy API 요청 생성
            ProxyExecutionRequest proxyRequest = ProxyExecutionRequest.builder()
                    .apiCode(batchJob.getProxyApiCode())
                    .parameters(parameters)
                    .executionTrigger("BATCH")
                    .executedBy(executedBy)
                    .build();

            // API 엔드포인트 조회
            var endpoint = apiEndpointService.getApiEndpointByCode(batchJob.getProxyApiCode());

            // API 실행
            return apiExecutionService.execute(endpoint, proxyRequest);

        } catch (Exception e) {
            log.error("Proxy API execution failed: {}", e.getMessage());
            throw new BatchException(BatchExceptionMessage.PROXY_API_EXECUTION_FAILED, e);
        }
    }

    /**
     * 실행 파라미터 준비
     * - JSON 파라미터 파싱 및 템플릿 변수 치환
     */
    private Map<String, Object> prepareExecutionParameters(BatchJob batchJob, String executionId) {
        Map<String, Object> parameters = new HashMap<>();

        // 기본 파라미터
        parameters.put("executionId", executionId);
        parameters.put("batchId", batchJob.getBatchId());

        // 배치 작업에 정의된 파라미터 병합
        if (batchJob.getExecutionParameters() != null && !batchJob.getExecutionParameters().isEmpty()) {
            try {
                Map<String, Object> customParams = objectMapper.readValue(
                        batchJob.getExecutionParameters(),
                        Map.class
                );
                parameters.putAll(customParams);
            } catch (Exception e) {
                log.warn("Failed to parse execution parameters: {}", e.getMessage());
            }
        }

        return parameters;
    }

    /**
     * 실행 완료 처리
     */
    private void completeExecution(BatchExecution execution, ProxyExecutionResponse proxyResponse) {
        execution.setProxyExecutionHistoryId(proxyResponse.getExecutionHistoryId());
        execution.setEndTime(LocalDateTime.now());
        execution.setExecutionTimeMs(proxyResponse.getExecutionTimeMs());

        if (proxyResponse.getIsSuccess()) {
            execution.setStatus(BatchStatus.SUCCESS.name());
        } else {
            execution.setStatus(BatchStatus.FAIL.name());
            execution.setErrorMessage(proxyResponse.getErrorMessage());
        }

        batchExecutionRepository.save(execution);
        log.info("Batch execution completed: {} - {}", execution.getExecutionId(), execution.getStatus());
    }

    /**
     * 실행 이력 생성
     */
    private BatchExecution createExecution(
            BatchJob batchJob,
            String executionId,
            BatchTriggerType triggerType,
            String executedBy,
            int retryCount,
            String originalExecutionId) {

        return BatchExecution.builder()
                .executionId(executionId)
                .batchJobId(batchJob.getId())
                .batchId(batchJob.getBatchId())
                .batchName(batchJob.getBatchName())
                .status(BatchStatus.WAIT.name())
                .triggerType(triggerType.name())
                .executionParameters(batchJob.getExecutionParameters())
                .retryCount(retryCount)
                .originalExecutionId(originalExecutionId)
                .serverInfo(getServerInfo())
                .executedBy(executedBy)
                .startTime(LocalDateTime.now())
                .build();
    }

    /**
     * 서버 정보 조회
     */
    private String getServerInfo() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 스택 트레이스 추출
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
            if (sb.length() > 2000) break;
        }
        return sb.toString();
    }
}
