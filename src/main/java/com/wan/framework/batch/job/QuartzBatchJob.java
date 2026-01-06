package com.wan.framework.batch.job;

import com.wan.framework.batch.constant.BatchTriggerType;
import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.service.BatchExecutionService;
import com.wan.framework.batch.service.BatchJobService;
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
 */
@Slf4j
@Component
public class QuartzBatchJob implements Job {

    @Autowired
    private BatchExecutionService batchExecutionService;

    @Autowired
    private BatchJobService batchJobService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String batchId = dataMap.getString("batchId");

        log.info("Quartz job triggered for batch: {}", batchId);

        try {
            // 배치 작업 조회
            BatchJob batchJob = batchJobService.getBatchJobByBatchId(batchId);

            // 활성화 상태 재확인
            if (!batchJob.getEnabled()) {
                log.info("Batch job is disabled, skipping execution: {}", batchId);
                return;
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
        }
    }
}
