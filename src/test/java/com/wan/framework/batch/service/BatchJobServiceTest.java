package com.wan.framework.batch.service;

import com.wan.framework.batch.constant.BatchExceptionMessage;
import com.wan.framework.batch.constant.ScheduleType;
import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.dto.BatchJobDTO;
import com.wan.framework.batch.exception.BatchException;
import com.wan.framework.batch.mapper.BatchJobMapper;
import com.wan.framework.batch.repository.BatchJobRepository;
import com.wan.framework.base.constant.DataStateCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchJobService 테스트")
class BatchJobServiceTest {

    @Mock
    private BatchJobRepository batchJobRepository;

    @Mock
    private BatchJobMapper batchJobMapper;

    @Mock
    private BatchSchedulerService batchSchedulerService;

    @Mock
    private com.wan.framework.batch.repository.BatchExecutionRepository batchExecutionRepository;

    @InjectMocks
    private BatchJobService batchJobService;

    private BatchJobDTO testDTO;
    private BatchJob testEntity;

    @BeforeEach
    void setUp() {
        testDTO = BatchJobDTO.builder()
                .batchId("TEST_BATCH_001")
                .batchName("테스트 배치")
                .description("테스트용 배치 작업")
                .scheduleType(ScheduleType.CRON.name())
                .scheduleExpression("0 0 * * * ?")
                .proxyApiCode("TEST_API")
                .executionParameters("{\"param1\":\"value1\"}")
                .enabled(true)
                .allowConcurrent(false)
                .maxRetryCount(3)
                .retryIntervalSeconds(60)
                .timeoutSeconds(300)
                .build();

        testEntity = BatchJob.builder()
                .id(1L)
                .batchId("TEST_BATCH_001")
                .batchName("테스트 배치")
                .description("테스트용 배치 작업")
                .scheduleType(ScheduleType.CRON.name())
                .scheduleExpression("0 0 * * * ?")
                .proxyApiCode("TEST_API")
                .executionParameters("{\"param1\":\"value1\"}")
                .enabled(true)
                .allowConcurrent(false)
                .maxRetryCount(3)
                .retryIntervalSeconds(60)
                .timeoutSeconds(300)
                .dataState(DataStateCode.I)
                .build();
    }

    @Test
    @DisplayName("배치 작업 생성 - 성공")
    void createBatchJob_Success() throws SchedulerException {
        // given
        given(batchJobRepository.existsByBatchIdAndDataStateNot(anyString(), any(DataStateCode.class)))
                .willReturn(false);
        given(batchJobMapper.toEntity(any(BatchJobDTO.class)))
                .willReturn(testEntity);
        given(batchJobRepository.save(any(BatchJob.class)))
                .willReturn(testEntity);
        given(batchJobMapper.toDto(any(BatchJob.class)))
                .willReturn(testDTO);

        // when
        BatchJobDTO result = batchJobService.createBatchJob(testDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBatchId()).isEqualTo("TEST_BATCH_001");
        verify(batchSchedulerService).scheduleBatchJob(any(BatchJob.class));
    }

    @Test
    @DisplayName("배치 작업 생성 - 중복 batchId")
    void createBatchJob_DuplicateBatchId() {
        // given
        given(batchJobRepository.existsByBatchIdAndDataStateNot(anyString(), any(DataStateCode.class)))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> batchJobService.createBatchJob(testDTO))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.BATCH_ID_ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("배치 작업 생성 - 잘못된 CRON 표현식")
    void createBatchJob_InvalidCronExpression() {
        // given
        testDTO.setScheduleExpression("INVALID_CRON");
        given(batchJobRepository.existsByBatchIdAndDataStateNot(anyString(), any(DataStateCode.class)))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> batchJobService.createBatchJob(testDTO))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.INVALID_SCHEDULE_EXPRESSION.getMessage());
    }

    @Test
    @DisplayName("배치 작업 생성 - 잘못된 INTERVAL 표현식")
    void createBatchJob_InvalidIntervalExpression() {
        // given
        testDTO.setScheduleType(ScheduleType.INTERVAL.name());
        testDTO.setScheduleExpression("-100");
        given(batchJobRepository.existsByBatchIdAndDataStateNot(anyString(), any(DataStateCode.class)))
                .willReturn(false);

        // when & then
        assertThatThrownBy(() -> batchJobService.createBatchJob(testDTO))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.INVALID_SCHEDULE_EXPRESSION.getMessage());
    }

    @Test
    @DisplayName("배치 작업 수정 - 성공")
    void updateBatchJob_Success() throws SchedulerException {
        // given
        given(batchJobRepository.findById(anyLong()))
                .willReturn(Optional.of(testEntity));
        given(batchExecutionRepository.findByBatchIdAndStatus(anyString(), anyString()))
                .willReturn(Optional.empty());
        given(batchJobRepository.save(any(BatchJob.class)))
                .willReturn(testEntity);
        given(batchJobMapper.toDto(any(BatchJob.class)))
                .willReturn(testDTO);

        // when
        BatchJobDTO result = batchJobService.updateBatchJob(1L, testDTO);

        // then
        assertThat(result).isNotNull();
        verify(batchSchedulerService).unscheduleBatchJob(anyString());
        verify(batchSchedulerService).scheduleBatchJob(any(BatchJob.class));
    }

    @Test
    @DisplayName("배치 작업 수정 - 존재하지 않는 ID")
    void updateBatchJob_NotFound() {
        // given
        given(batchJobRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> batchJobService.updateBatchJob(999L, testDTO))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.BATCH_JOB_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배치 작업 삭제 - 성공")
    void deleteBatchJob_Success() throws SchedulerException {
        // given
        given(batchJobRepository.findById(anyLong()))
                .willReturn(Optional.of(testEntity));
        given(batchExecutionRepository.findByBatchIdAndStatus(anyString(), anyString()))
                .willReturn(Optional.empty());
        given(batchJobRepository.save(any(BatchJob.class)))
                .willReturn(testEntity);

        // when
        batchJobService.deleteBatchJob(1L);

        // then
        verify(batchSchedulerService).unscheduleBatchJob(anyString());
        verify(batchJobRepository).save(argThat(batch ->
                batch.getDataState() == DataStateCode.D
        ));
    }

    @Test
    @DisplayName("배치 작업 활성화/비활성화 - 활성화")
    void toggleBatchJob_Enable() throws SchedulerException {
        // given
        testEntity.setEnabled(false);
        given(batchJobRepository.findById(anyLong()))
                .willReturn(Optional.of(testEntity));
        given(batchJobRepository.save(any(BatchJob.class)))
                .willReturn(testEntity);
        given(batchJobMapper.toDto(any(BatchJob.class)))
                .willReturn(testDTO);

        // when
        BatchJobDTO result = batchJobService.toggleBatchJob(1L);

        // then
        assertThat(result).isNotNull();
        verify(batchSchedulerService).scheduleBatchJob(any(BatchJob.class));
    }

    @Test
    @DisplayName("배치 작업 활성화/비활성화 - 비활성화")
    void toggleBatchJob_Disable() throws SchedulerException {
        // given
        testEntity.setEnabled(true);
        given(batchJobRepository.findById(anyLong()))
                .willReturn(Optional.of(testEntity));
        given(batchJobRepository.save(any(BatchJob.class)))
                .willReturn(testEntity);
        given(batchJobMapper.toDto(any(BatchJob.class)))
                .willReturn(testDTO);

        // when
        BatchJobDTO result = batchJobService.toggleBatchJob(1L);

        // then
        assertThat(result).isNotNull();
        verify(batchSchedulerService).unscheduleBatchJob(anyString());
    }

    @Test
    @DisplayName("배치 작업 조회 - batchId로 조회 성공")
    void getBatchJobByBatchId_Success() {
        // given
        given(batchJobRepository.findByBatchIdAndDataStateNot(anyString(), any(DataStateCode.class)))
                .willReturn(Optional.of(testEntity));

        // when
        BatchJob result = batchJobService.getBatchJobByBatchId("TEST_BATCH_001");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBatchId()).isEqualTo("TEST_BATCH_001");
    }

    @Test
    @DisplayName("배치 작업 조회 - batchId로 조회 실패")
    void getBatchJobByBatchId_NotFound() {
        // given
        given(batchJobRepository.findByBatchIdAndDataStateNot(anyString(), any(DataStateCode.class)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> batchJobService.getBatchJobByBatchId("INVALID_ID"))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.BATCH_JOB_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("모든 배치 작업 조회 - 페이징")
    void getAllBatchJobs_WithPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<BatchJob> page = new PageImpl<>(List.of(testEntity), pageable, 1);
        given(batchJobRepository.findByDataStateNot(any(DataStateCode.class), any(Pageable.class)))
                .willReturn(page);
        given(batchJobMapper.toDto(any(BatchJob.class)))
                .willReturn(testDTO);

        // when
        Page<BatchJobDTO> result = batchJobService.getAllBatchJobs(0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("활성화된 배치 작업 조회 - 페이징")
    void getEnabledBatchJobs_WithPaging() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<BatchJob> page = new PageImpl<>(List.of(testEntity), pageable, 1);
        given(batchJobRepository.findByEnabledTrueAndDataStateNot(any(DataStateCode.class), any(Pageable.class)))
                .willReturn(page);
        given(batchJobMapper.toDto(any(BatchJob.class)))
                .willReturn(testDTO);

        // when
        Page<BatchJobDTO> result = batchJobService.getEnabledBatchJobs(0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEnabled()).isTrue();
    }

    @Test
    @DisplayName("ID로 배치 작업 조회 - 성공")
    void getBatchJob_Success() {
        // given
        given(batchJobRepository.findById(anyLong()))
                .willReturn(Optional.of(testEntity));
        given(batchJobMapper.toDto(any(BatchJob.class)))
                .willReturn(testDTO);

        // when
        BatchJobDTO result = batchJobService.getBatchJob(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBatchId()).isEqualTo("TEST_BATCH_001");
    }

    @Test
    @DisplayName("ID로 배치 작업 조회 - 실패")
    void getBatchJob_NotFound() {
        // given
        given(batchJobRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> batchJobService.getBatchJob(999L))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.BATCH_JOB_NOT_FOUND.getMessage());
    }
}
