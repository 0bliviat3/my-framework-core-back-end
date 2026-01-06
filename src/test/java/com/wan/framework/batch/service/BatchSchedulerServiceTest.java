package com.wan.framework.batch.service;

import com.wan.framework.batch.constant.ScheduleType;
import com.wan.framework.batch.domain.BatchJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchSchedulerService 테스트")
class BatchSchedulerServiceTest {

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private BatchSchedulerService batchSchedulerService;

    private BatchJob testBatchJob;

    @BeforeEach
    void setUp() {
        testBatchJob = BatchJob.builder()
                .id(1L)
                .batchId("TEST_BATCH_001")
                .batchName("테스트 배치")
                .scheduleType(ScheduleType.CRON.name())
                .scheduleExpression("0 0 * * * ?")
                .proxyApiCode("TEST_API")
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("CRON 배치 작업 스케줄 등록 - 성공")
    void scheduleBatchJob_Cron_Success() throws SchedulerException {
        // given
        given(scheduler.checkExists(any(JobKey.class)))
                .willReturn(false);

        // when
        batchSchedulerService.scheduleBatchJob(testBatchJob);

        // then
        verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("INTERVAL 배치 작업 스케줄 등록 - 성공")
    void scheduleBatchJob_Interval_Success() throws SchedulerException {
        // given
        testBatchJob.setScheduleType(ScheduleType.INTERVAL.name());
        testBatchJob.setScheduleExpression("60000"); // 60초
        given(scheduler.checkExists(any(JobKey.class)))
                .willReturn(false);

        // when
        batchSchedulerService.scheduleBatchJob(testBatchJob);

        // then
        verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("배치 작업 스케줄 등록 - 이미 존재하는 경우 재등록")
    void scheduleBatchJob_AlreadyExists() throws SchedulerException {
        // given
        given(scheduler.checkExists(any(JobKey.class)))
                .willReturn(true);

        // when
        batchSchedulerService.scheduleBatchJob(testBatchJob);

        // then
        verify(scheduler).deleteJob(any(JobKey.class));
        verify(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("배치 작업 스케줄 해제 - 성공")
    void unscheduleBatchJob_Success() throws SchedulerException {
        // given
        given(scheduler.checkExists(any(JobKey.class)))
                .willReturn(true);

        // when
        batchSchedulerService.unscheduleBatchJob("TEST_BATCH_001");

        // then
        verify(scheduler).deleteJob(any(JobKey.class));
    }

    @Test
    @DisplayName("배치 작업 스케줄 해제 - 존재하지 않는 경우")
    void unscheduleBatchJob_NotExists() throws SchedulerException {
        // given
        given(scheduler.checkExists(any(JobKey.class)))
                .willReturn(false);

        // when
        batchSchedulerService.unscheduleBatchJob("TEST_BATCH_001");

        // then
        verify(scheduler, never()).deleteJob(any(JobKey.class));
    }

    @Test
    @DisplayName("모든 활성화된 배치 작업 스케줄 등록")
    void scheduleAllEnabledJobs_Success() throws SchedulerException {
        // given
        BatchJob batch1 = BatchJob.builder()
                .batchId("BATCH_001")
                .batchName("배치 1")
                .scheduleType(ScheduleType.CRON.name())
                .scheduleExpression("0 0 * * * ?")
                .build();

        BatchJob batch2 = BatchJob.builder()
                .batchId("BATCH_002")
                .batchName("배치 2")
                .scheduleType(ScheduleType.INTERVAL.name())
                .scheduleExpression("30000")
                .build();

        List<BatchJob> batchJobs = List.of(batch1, batch2);

        given(scheduler.checkExists(any(JobKey.class)))
                .willReturn(false);

        // when
        batchSchedulerService.scheduleAllEnabledJobs(batchJobs);

        // then
        verify(scheduler, times(2)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("모든 활성화된 배치 작업 스케줄 등록 - 일부 실패")
    void scheduleAllEnabledJobs_PartialFailure() throws SchedulerException {
        // given
        BatchJob batch1 = BatchJob.builder()
                .batchId("BATCH_001")
                .batchName("배치 1")
                .scheduleType(ScheduleType.CRON.name())
                .scheduleExpression("0 0 * * * ?")
                .build();

        BatchJob batch2 = BatchJob.builder()
                .batchId("BATCH_002")
                .batchName("배치 2")
                .scheduleType(ScheduleType.CRON.name())
                .scheduleExpression("0 0 * * * ?")
                .build();

        List<BatchJob> batchJobs = List.of(batch1, batch2);

        given(scheduler.checkExists(any(JobKey.class)))
                .willReturn(false);
        willThrow(new SchedulerException("Scheduler error"))
                .given(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));

        // when
        batchSchedulerService.scheduleAllEnabledJobs(batchJobs);

        // then
        // 에러가 발생해도 다음 배치 작업 등록 시도
        verify(scheduler, times(2)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    @DisplayName("스케줄러 상태 조회 - 실행중")
    void isSchedulerRunning_Running() throws SchedulerException {
        // given
        given(scheduler.isStarted()).willReturn(true);
        given(scheduler.isInStandbyMode()).willReturn(false);

        // when
        boolean result = batchSchedulerService.isSchedulerRunning();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("스케줄러 상태 조회 - 중지됨")
    void isSchedulerRunning_Stopped() throws SchedulerException {
        // given
        given(scheduler.isStarted()).willReturn(false);

        // when
        boolean result = batchSchedulerService.isSchedulerRunning();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("스케줄러 상태 조회 - 대기모드")
    void isSchedulerRunning_InStandby() throws SchedulerException {
        // given
        given(scheduler.isStarted()).willReturn(true);
        given(scheduler.isInStandbyMode()).willReturn(true);

        // when
        boolean result = batchSchedulerService.isSchedulerRunning();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("스케줄러 상태 조회 - 예외 발생")
    void isSchedulerRunning_Exception() throws SchedulerException {
        // given
        given(scheduler.isStarted())
                .willThrow(new SchedulerException("Scheduler error"));

        // when
        boolean result = batchSchedulerService.isSchedulerRunning();

        // then
        assertThat(result).isFalse();
    }
}
