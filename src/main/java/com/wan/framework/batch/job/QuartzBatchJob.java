package com.wan.framework.batch.job;

import com.wan.framework.batch.constant.BatchTriggerType;
import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.service.BatchExecutionService;
import com.wan.framework.batch.service.BatchJobService;
import com.wan.framework.redis.service.ResilientDistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Quartz 배치 작업 실행자
 * - Quartz 스케줄러에 의해 호출되는 Job
 * - BatchExecutionService를 통해 실제 배치 실행
 * - 분산 락을 통한 다중 서버 환경에서의 중복 실행 방지
 */
@Slf4j
@Component
public class QuartzBatchJob implements Job {

    @Autowired
    private BatchExecutionService batchExecutionService;

    @Autowired
    private BatchJobService batchJobService;

    @Autowired
    private ResilientDistributedLockService resilientDistributedLockService;

    private static final String LOCK_PREFIX = "quartz:batch:";
    private static final long DEFAULT_LOCK_TTL = 300L; // 5분 기본 TTL

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String batchId = dataMap.getString("batchId");

        log.info("Quartz job triggered for batch: {}", batchId);

        // 분산 락 키 생성
        String lockKey = LOCK_PREFIX + batchId;
        String lockValue = null;

        try {
            // 배치 작업 조회
            BatchJob batchJob = batchJobService.getBatchJobByBatchId(batchId);

            // 활성화 상태 재확인
            if (!batchJob.getEnabled()) {
                log.info("Batch job is disabled, skipping execution: {}", batchId);
                return;
            }

            // 동시 실행이 허용되지 않는 경우에만 분산 락 획득
            if (!batchJob.getAllowConcurrent()) {
                try {
                    // TTL = batchJob.timeoutSeconds + 버퍼(30초)
                    long lockTtl = batchJob.getTimeoutSeconds() + 30L;
                    lockValue = resilientDistributedLockService.acquireLock(lockKey, lockTtl);
                    log.debug("Distributed lock acquired for batch: {} (lockValue: {})", batchId, lockValue);
                } catch (Exception e) {
                    log.warn("Failed to acquire distributed lock for batch: {} - Skipping execution", batchId);
                    return; // 락 획득 실패 시 실행하지 않음
                }
            }

            // 배치 실행
            batchExecutionService.executeBatch(
                    batchJob,
                    BatchTriggerType.SCHEDULER,
                    "SCHEDULER"
            );

            log.info("Quartz job completed for batch: {}", batchId);

        } catch (Exception e) {
            log.error("Quartz job failed for batch: {} - {}", batchId, e.getMessage());
            // Quartz에게 실패를 알리고 재시도 정책에 따라 처리
            throw new JobExecutionException(e);
        } finally {
            // 분산 락 해제
            if (lockValue != null) {
                try {
                    resilientDistributedLockService.releaseLock(lockKey, lockValue);
                    log.debug("Distributed lock released for batch: {}", batchId);
                } catch (Exception e) {
                    log.error("Failed to release distributed lock for batch: {} - {}", batchId, e.getMessage());
                }
            }
        }
    }
}
