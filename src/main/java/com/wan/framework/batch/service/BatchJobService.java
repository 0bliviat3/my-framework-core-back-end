package com.wan.framework.batch.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.batch.constant.BatchExceptionMessage;
import com.wan.framework.batch.constant.BatchStatus;
import com.wan.framework.batch.constant.ScheduleType;
import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.dto.BatchJobDTO;
import com.wan.framework.batch.exception.BatchException;
import com.wan.framework.batch.mapper.BatchJobMapper;
import com.wan.framework.batch.repository.BatchJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 배치 작업 서비스
 * - 배치 작업 CRUD
 * - 스케줄 검증
 * - 활성화/비활성화 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchJobService {

    private final BatchJobRepository batchJobRepository;
    private final BatchJobMapper batchJobMapper;
    private final BatchSchedulerService batchSchedulerService;

    /**
     * 배치 작업 생성
     */
    @Transactional
    public BatchJobDTO createBatchJob(BatchJobDTO dto) {
        log.info("Creating batch job: {}", dto.getBatchId());

        // 배치 ID 중복 체크
        if (batchJobRepository.existsByBatchIdAndDataStateNot(dto.getBatchId(), DataStateCode.D)) {
            throw new BatchException(BatchExceptionMessage.BATCH_ID_ALREADY_EXISTS);
        }

        // 스케줄 표현식 검증
        validateScheduleExpression(dto.getScheduleType(), dto.getScheduleExpression());

        // Proxy API 코드 검증은 실제 실행 시 수행
        BatchJob entity = batchJobMapper.toEntity(dto);
        BatchJob saved = batchJobRepository.save(entity);

        // 활성화 상태면 스케줄러에 등록
        if (saved.getEnabled()) {
            try {
                batchSchedulerService.scheduleBatchJob(saved);
                log.info("Batch job scheduled: {}", saved.getBatchId());
            } catch (Exception e) {
                log.error("Failed to schedule batch job: {}", e.getMessage());
                // 스케줄링 실패해도 배치 작업 자체는 저장됨
            }
        }

        log.info("Batch job created: {} (ID: {})", saved.getBatchId(), saved.getId());
        return batchJobMapper.toDto(saved);
    }

    /**
     * 배치 작업 수정
     */
    @Transactional
    public BatchJobDTO updateBatchJob(Long id, BatchJobDTO dto) {
        log.info("Updating batch job: {}", id);

        BatchJob entity = batchJobRepository.findById(id)
                .filter(e -> e.getDataState() != DataStateCode.D)
                .orElseThrow(() -> new BatchException(BatchExceptionMessage.BATCH_JOB_NOT_FOUND));

        // 실행 중인지 확인
        if (isJobRunning(entity.getBatchId())) {
            log.warn("Cannot update running batch job: {}", entity.getBatchId());
            // 실행 중인 배치는 즉시 중단하지 않고, 다음 실행부터 반영
        }

        // 스케줄 표현식 검증 (변경된 경우)
        if (dto.getScheduleExpression() != null) {
            validateScheduleExpression(
                    dto.getScheduleType() != null ? dto.getScheduleType() : entity.getScheduleType(),
                    dto.getScheduleExpression()
            );
        }

        // 기존 스케줄 제거
        try {
            batchSchedulerService.unscheduleBatchJob(entity.getBatchId());
        } catch (Exception e) {
            log.warn("Failed to unschedule batch job: {}", e.getMessage());
        }

        // 엔티티 업데이트
        batchJobMapper.updateEntityFromDto(dto, entity);
        BatchJob updated = batchJobRepository.save(entity);

        // 활성화 상태면 재등록
        if (updated.getEnabled()) {
            try {
                batchSchedulerService.scheduleBatchJob(updated);
                log.info("Batch job rescheduled: {}", updated.getBatchId());
            } catch (Exception e) {
                log.error("Failed to reschedule batch job: {}", e.getMessage());
            }
        }

        log.info("Batch job updated: {}", id);
        return batchJobMapper.toDto(updated);
    }

    /**
     * 배치 작업 삭제 (논리적 삭제)
     */
    @Transactional
    public void deleteBatchJob(Long id) {
        log.info("Deleting batch job: {}", id);

        BatchJob entity = batchJobRepository.findById(id)
                .filter(e -> e.getDataState() != DataStateCode.D)
                .orElseThrow(() -> new BatchException(BatchExceptionMessage.BATCH_JOB_NOT_FOUND));

        // 실행 중인지 확인
        if (isJobRunning(entity.getBatchId())) {
            throw new BatchException(BatchExceptionMessage.BATCH_JOB_RUNNING);
        }

        // 스케줄러에서 제거
        try {
            batchSchedulerService.unscheduleBatchJob(entity.getBatchId());
            log.info("Batch job unscheduled: {}", entity.getBatchId());
        } catch (Exception e) {
            log.warn("Failed to unschedule batch job: {}", e.getMessage());
        }

        // 논리적 삭제
        entity.setDataState(DataStateCode.D);
        entity.setEnabled(false);
        batchJobRepository.save(entity);

        log.info("Batch job deleted: {}", id);
    }

    /**
     * 배치 작업 조회 (ID)
     */
    public BatchJobDTO getBatchJob(Long id) {
        BatchJob entity = batchJobRepository.findById(id)
                .filter(e -> e.getDataState() != DataStateCode.D)
                .orElseThrow(() -> new BatchException(BatchExceptionMessage.BATCH_JOB_NOT_FOUND));

        return batchJobMapper.toDto(entity);
    }

    /**
     * 배치 작업 조회 (배치 ID)
     */
    public BatchJob getBatchJobByBatchId(String batchId) {
        return batchJobRepository.findByBatchIdAndDataStateNot(batchId, DataStateCode.D)
                .orElseThrow(() -> new BatchException(BatchExceptionMessage.BATCH_JOB_NOT_FOUND));
    }

    /**
     * 배치 작업 목록 조회 (전체)
     */
    public Page<BatchJobDTO> getAllBatchJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BatchJob> entities = batchJobRepository.findByDataStateNot(DataStateCode.D, pageable);

        return entities.map(batchJobMapper::toDto);
    }

    /**
     * 배치 작업 목록 조회 (활성화만)
     */
    public Page<BatchJobDTO> getEnabledBatchJobs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BatchJob> entities = batchJobRepository.findByEnabledTrueAndDataStateNot(DataStateCode.D, pageable);

        return entities.map(batchJobMapper::toDto);
    }

    /**
     * 활성화된 배치 목록 조회 (스케줄러용)
     */
    public List<BatchJob> getEnabledBatchJobsList() {
        return batchJobRepository.findByEnabledTrueAndDataStateNot(DataStateCode.D);
    }

    /**
     * 배치 작업 활성화/비활성화 토글
     */
    @Transactional
    public BatchJobDTO toggleBatchJob(Long id) {
        log.info("Toggling batch job: {}", id);

        BatchJob entity = batchJobRepository.findById(id)
                .filter(e -> e.getDataState() != DataStateCode.D)
                .orElseThrow(() -> new BatchException(BatchExceptionMessage.BATCH_JOB_NOT_FOUND));

        boolean newState = !entity.getEnabled();
        entity.setEnabled(newState);

        if (newState) {
            // 활성화 - 스케줄러에 등록
            try {
                batchSchedulerService.scheduleBatchJob(entity);
                log.info("Batch job scheduled: {}", entity.getBatchId());
            } catch (Exception e) {
                log.error("Failed to schedule batch job: {}", e.getMessage());
                throw new BatchException(BatchExceptionMessage.INVALID_SCHEDULE_EXPRESSION, e);
            }
        } else {
            // 비활성화 - 스케줄러에서 제거
            try {
                batchSchedulerService.unscheduleBatchJob(entity.getBatchId());
                log.info("Batch job unscheduled: {}", entity.getBatchId());
            } catch (Exception e) {
                log.warn("Failed to unschedule batch job: {}", e.getMessage());
            }
        }

        BatchJob updated = batchJobRepository.save(entity);
        log.info("Batch job toggled: {} -> {}", id, newState);

        return batchJobMapper.toDto(updated);
    }

    /**
     * 배치 마지막 실행 정보 업데이트
     */
    @Transactional
    public void updateLastExecution(String batchId, String status, java.time.LocalDateTime executedAt) {
        batchJobRepository.findByBatchIdAndDataStateNot(batchId, DataStateCode.D)
                .ifPresent(batchJob -> {
                    batchJob.setLastExecutedAt(executedAt);
                    batchJob.setLastExecutionStatus(status);
                    batchJobRepository.save(batchJob);
                });
    }

    /**
     * 스케줄 표현식 검증
     */
    private void validateScheduleExpression(String scheduleType, String expression) {
        try {
            if (ScheduleType.CRON.name().equals(scheduleType)) {
                // CRON 표현식 검증
                if (!CronExpression.isValidExpression(expression)) {
                    throw new BatchException(BatchExceptionMessage.INVALID_SCHEDULE_EXPRESSION);
                }
            } else if (ScheduleType.INTERVAL.name().equals(scheduleType)) {
                // INTERVAL (밀리초) 검증
                long interval = Long.parseLong(expression);
                if (interval <= 0) {
                    throw new BatchException(BatchExceptionMessage.INVALID_SCHEDULE_EXPRESSION);
                }
            } else {
                throw new BatchException(BatchExceptionMessage.INVALID_SCHEDULE_EXPRESSION);
            }
        } catch (NumberFormatException e) {
            throw new BatchException(BatchExceptionMessage.INVALID_SCHEDULE_EXPRESSION);
        }
    }

    /**
     * 배치 실행 중 여부 확인
     */
    private boolean isJobRunning(String batchId) {
        // BatchExecutionRepository 조회 필요
        // 간단히 구현하기 위해 여기서는 false 반환
        // 실제로는 BatchExecutionRepository를 주입받아 RUNNING 상태 확인
        return false;
    }
}
