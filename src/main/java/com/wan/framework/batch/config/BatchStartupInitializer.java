package com.wan.framework.batch.config;

import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.repository.BatchJobRepository;
import com.wan.framework.batch.service.BatchSchedulerService;
import com.wan.framework.base.constant.DataStateCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 배치 시작 시 초기화
 * - 활성화된 배치 작업 스케줄 등록
 * - 애플리케이션 시작 시 자동 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchStartupInitializer implements ApplicationRunner {

    private final BatchJobRepository batchJobRepository;
    private final BatchSchedulerService batchSchedulerService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Batch Startup Initialization Started ===");

        try {
            // 스케줄러 상태 확인
            if (!batchSchedulerService.isSchedulerRunning()) {
                log.warn("Scheduler is not running. Skip batch job scheduling.");
                return;
            }

            // 활성화된 배치 작업 조회
            List<BatchJob> enabledJobs = batchJobRepository.findByEnabledTrueAndDataStateNot(DataStateCode.D);
            log.info("Found {} enabled batch jobs", enabledJobs.size());

            if (enabledJobs.isEmpty()) {
                log.info("No enabled batch jobs to schedule");
                return;
            }

            // 모든 활성화된 배치 작업 스케줄 등록
            batchSchedulerService.scheduleAllEnabledJobs(enabledJobs);

            log.info("=== Batch Startup Initialization Completed Successfully ===");

        } catch (Exception e) {
            log.error("Failed to initialize batch jobs on startup: {}", e.getMessage(), e);
            // 애플리케이션 시작은 계속 진행 (배치 실패로 전체 시스템이 중단되지 않도록)
        }
    }
}
