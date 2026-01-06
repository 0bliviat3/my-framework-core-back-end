package com.wan.framework.batch.service;

import com.wan.framework.batch.constant.ScheduleType;
import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.job.QuartzBatchJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

/**
 * 배치 스케줄러 서비스
 * - Quartz 스케줄러 관리
 * - 배치 작업 등록/해제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchSchedulerService {

    private final Scheduler scheduler;

    /**
     * 배치 작업 스케줄 등록
     */
    public void scheduleBatchJob(BatchJob batchJob) throws SchedulerException {
        log.info("Scheduling batch job: {}", batchJob.getBatchId());

        // Job Detail 생성
        JobDetail jobDetail = JobBuilder.newJob(QuartzBatchJob.class)
                .withIdentity(batchJob.getBatchId(), "BATCH_GROUP")
                .withDescription(batchJob.getBatchName())
                .usingJobData("batchId", batchJob.getBatchId())
                .storeDurably(false)
                .build();

        // Trigger 생성
        Trigger trigger = createTrigger(batchJob);

        // 스케줄러에 등록
        if (scheduler.checkExists(jobDetail.getKey())) {
            // 이미 존재하면 삭제 후 재등록
            scheduler.deleteJob(jobDetail.getKey());
        }

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("Batch job scheduled: {} with trigger: {}", batchJob.getBatchId(), trigger.getKey());
    }

    /**
     * 배치 작업 스케줄 해제
     */
    public void unscheduleBatchJob(String batchId) throws SchedulerException {
        log.info("Unscheduling batch job: {}", batchId);

        JobKey jobKey = JobKey.jobKey(batchId, "BATCH_GROUP");

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            log.info("Batch job unscheduled: {}", batchId);
        } else {
            log.warn("Batch job not found in scheduler: {}", batchId);
        }
    }

    /**
     * Trigger 생성
     */
    private Trigger createTrigger(BatchJob batchJob) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(batchJob.getBatchId() + "_trigger", "BATCH_GROUP")
                .withDescription(batchJob.getBatchName());

        if (ScheduleType.CRON.name().equals(batchJob.getScheduleType())) {
            // CRON 스케줄
            triggerBuilder.withSchedule(
                    CronScheduleBuilder.cronSchedule(batchJob.getScheduleExpression())
                            .withMisfireHandlingInstructionDoNothing()
            );
        } else if (ScheduleType.INTERVAL.name().equals(batchJob.getScheduleType())) {
            // INTERVAL 스케줄
            long intervalMs = Long.parseLong(batchJob.getScheduleExpression());
            triggerBuilder.withSchedule(
                    SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMilliseconds(intervalMs)
                            .repeatForever()
                            .withMisfireHandlingInstructionNextWithRemainingCount()
            );
        }

        return triggerBuilder.startNow().build();
    }

    /**
     * 모든 활성화된 배치 작업 스케줄 등록
     */
    public void scheduleAllEnabledJobs(java.util.List<BatchJob> batchJobs) {
        log.info("Scheduling all enabled batch jobs: {} jobs", batchJobs.size());

        for (BatchJob batchJob : batchJobs) {
            try {
                scheduleBatchJob(batchJob);
            } catch (Exception e) {
                log.error("Failed to schedule batch job: {} - {}", batchJob.getBatchId(), e.getMessage());
            }
        }

        log.info("All enabled batch jobs scheduled");
    }

    /**
     * 스케줄러 상태 조회
     */
    public boolean isSchedulerRunning() {
        try {
            return scheduler.isStarted() && !scheduler.isInStandbyMode();
        } catch (SchedulerException e) {
            log.error("Failed to check scheduler status: {}", e.getMessage());
            return false;
        }
    }
}
