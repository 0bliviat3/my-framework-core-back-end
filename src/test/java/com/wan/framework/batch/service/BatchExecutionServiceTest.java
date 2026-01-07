package com.wan.framework.batch.service;

import com.wan.framework.batch.constant.BatchExceptionMessage;
import com.wan.framework.batch.constant.BatchStatus;
import com.wan.framework.batch.constant.BatchTriggerType;
import com.wan.framework.batch.domain.BatchExecution;
import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.dto.BatchExecutionDTO;
import com.wan.framework.batch.exception.BatchException;
import com.wan.framework.batch.mapper.BatchExecutionMapper;
import com.wan.framework.batch.repository.BatchExecutionRepository;
import com.wan.framework.proxy.domain.ApiEndpoint;
import com.wan.framework.proxy.dto.ProxyExecutionResponse;
import com.wan.framework.proxy.service.ApiEndpointService;
import com.wan.framework.proxy.service.ApiExecutionService;
import com.wan.framework.redis.service.DistributedLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchExecutionService 테스트")
class BatchExecutionServiceTest {

    @Mock
    private BatchExecutionRepository batchExecutionRepository;

    @Mock
    private BatchExecutionMapper batchExecutionMapper;

    @Mock
    private ApiEndpointService apiEndpointService;

    @Mock
    private ApiExecutionService apiExecutionService;

    @Mock
    private DistributedLockService distributedLockService;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @InjectMocks
    private BatchExecutionService batchExecutionService;

    private BatchJob testBatchJob;
    private BatchExecution testExecution;
    private ProxyExecutionResponse successResponse;

    @BeforeEach
    void setUp() {
        testBatchJob = BatchJob.builder()
                .id(1L)
                .batchId("TEST_BATCH_001")
                .batchName("테스트 배치")
                .proxyApiCode("TEST_API")
                .executionParameters("{\"param\":\"value\"}")
                .enabled(true)
                .allowConcurrent(false)
                .maxRetryCount(3)
                .retryIntervalSeconds(60)
                .timeoutSeconds(300)
                .build();

        testExecution = BatchExecution.builder()
                .id(1L)
                .executionId("exec-001")
                .batchJobId(1L)
                .batchId("TEST_BATCH_001")
                .status(BatchStatus.RUNNING.name())
                .triggerType(BatchTriggerType.MANUAL.name())
                .executedBy("admin")
                .startTime(LocalDateTime.now())
                .retryCount(0)
                .build();

        successResponse = ProxyExecutionResponse.builder()
                .executionHistoryId(1L)
                .apiCode("TEST_API")
                .isSuccess(true)
                .statusCode(200)
                .responseBody("{\"result\":\"success\"}")
                .executionTimeMs(1000L)
                .retryAttempt(0)
                .build();
    }

    @Test
    @DisplayName("배치 실행 - 성공")
    void executeBatch_Success() {
        // given
        ApiEndpoint mockEndpoint = new ApiEndpoint();

        given(batchExecutionRepository.findByBatchIdAndStatus(anyString(), eq(BatchStatus.RUNNING.name())))
                .willReturn(Optional.empty());
        given(distributedLockService.acquireLock(anyString(), anyLong()))
                .willReturn("lock-value");
        given(batchExecutionRepository.save(any(BatchExecution.class)))
                .willReturn(testExecution);
        given(apiEndpointService.getApiEndpointByCode(anyString()))
                .willReturn(mockEndpoint);
        given(apiExecutionService.execute(any(), any()))
                .willReturn(successResponse);
        given(batchExecutionMapper.toDto(any(BatchExecution.class)))
                .willReturn(new BatchExecutionDTO());

        // when
        BatchExecutionDTO result = batchExecutionService.executeBatch(
                testBatchJob,
                BatchTriggerType.MANUAL,
                "admin"
        );

        // then
        assertThat(result).isNotNull();
        verify(distributedLockService).acquireLock(anyString(), anyLong());
        verify(distributedLockService).releaseLock(anyString(), eq("lock-value"));
        verify(apiExecutionService).execute(any(), any());
    }

    @Test
    @DisplayName("배치 실행 - 동시 실행 방지 (이미 실행중)")
    void executeBatch_AlreadyRunning() {
        // given
        testBatchJob.setAllowConcurrent(false);
        given(batchExecutionRepository.findByBatchIdAndStatus(anyString(), eq(BatchStatus.RUNNING.name())))
                .willReturn(Optional.of(testExecution));

        // when & then
        assertThatThrownBy(() -> batchExecutionService.executeBatch(
                testBatchJob,
                BatchTriggerType.MANUAL,
                "admin"
        ))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.BATCH_ALREADY_RUNNING.getMessage());
    }

    @Test
    @DisplayName("배치 실행 - Lock 획득 실패")
    void executeBatch_LockAcquisitionFailed() {
        // given
        given(batchExecutionRepository.findByBatchIdAndStatus(anyString(), eq(BatchStatus.RUNNING.name())))
                .willReturn(Optional.empty());
        given(distributedLockService.acquireLock(anyString(), anyLong()))
                .willThrow(new com.wan.framework.redis.exception.RedisException(
                        com.wan.framework.redis.constant.RedisExceptionMessage.LOCK_ACQUIRE_FAILED));
        given(batchExecutionRepository.findByExecutionId(anyString()))
                .willReturn(Optional.empty());
        given(batchExecutionRepository.save(any(BatchExecution.class)))
                .willReturn(testExecution);

        // when & then
        assertThatThrownBy(() -> batchExecutionService.executeBatch(
                testBatchJob,
                BatchTriggerType.MANUAL,
                "admin"
        ))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.BATCH_EXECUTION_FAILED.getMessage());
    }

    @Test
    @DisplayName("배치 실행 - Proxy API 실행 실패")
    void executeBatch_ProxyApiFailed() {
        // given
        ProxyExecutionResponse failResponse = ProxyExecutionResponse.builder()
                .executionHistoryId(1L)
                .apiCode("TEST_API")
                .isSuccess(false)
                .statusCode(500)
                .errorMessage("API Error")
                .executionTimeMs(500L)
                .retryAttempt(0)
                .build();

        given(batchExecutionRepository.findByBatchIdAndStatus(anyString(), eq(BatchStatus.RUNNING.name())))
                .willReturn(Optional.empty());
        given(distributedLockService.acquireLock(anyString(), anyLong()))
                .willReturn("lock-value");
        given(batchExecutionRepository.save(any(BatchExecution.class)))
                .willReturn(testExecution);
        given(apiEndpointService.getApiEndpointByCode(anyString()))
                .willReturn(new ApiEndpoint());
        given(apiExecutionService.execute(any(), any()))
                .willReturn(failResponse);
        given(batchExecutionMapper.toDto(any(BatchExecution.class)))
                .willReturn(new BatchExecutionDTO());

        // when
        BatchExecutionDTO result = batchExecutionService.executeBatch(
                testBatchJob,
                BatchTriggerType.MANUAL,
                "admin"
        );

        // then
        assertThat(result).isNotNull();
        verify(distributedLockService).releaseLock(anyString(), eq("lock-value"));
    }

    @Test
    @DisplayName("수동 실행 - 성공")
    void executeManual_Success() {
        // given
        com.wan.framework.batch.dto.BatchExecutionRequest request =
                com.wan.framework.batch.dto.BatchExecutionRequest.builder()
                        .batchId("TEST_BATCH_001")
                        .executedBy("admin")
                        .build();

        given(batchExecutionRepository.findByBatchIdAndStatus(anyString(), eq(BatchStatus.RUNNING.name())))
                .willReturn(Optional.empty());
        given(distributedLockService.acquireLock(anyString(), anyLong()))
                .willReturn("lock-value");
        given(batchExecutionRepository.save(any(BatchExecution.class)))
                .willReturn(testExecution);
        given(apiEndpointService.getApiEndpointByCode(anyString()))
                .willReturn(new ApiEndpoint());
        given(apiExecutionService.execute(any(), any()))
                .willReturn(successResponse);
        given(batchExecutionMapper.toDto(any(BatchExecution.class)))
                .willReturn(new BatchExecutionDTO());

        // when
        BatchExecutionDTO result = batchExecutionService.executeManual(testBatchJob, request);

        // then
        assertThat(result).isNotNull();
        verify(apiExecutionService).execute(any(), any());
    }

    @Test
    @DisplayName("배치 재시도 - 성공")
    void retryBatch_Success() {
        // given
        testExecution.setStatus(BatchStatus.FAIL.name());
        testExecution.setRetryCount(1);

        given(batchExecutionRepository.findByExecutionId(anyString()))
                .willReturn(Optional.of(testExecution));
        given(batchExecutionRepository.save(any(BatchExecution.class)))
                .willReturn(testExecution);
        given(batchExecutionMapper.toDto(any(BatchExecution.class)))
                .willReturn(new BatchExecutionDTO());

        // when
        BatchExecutionDTO result = batchExecutionService.retryBatch("exec-001", "admin");

        // then
        assertThat(result).isNotNull();
        // retryBatch는 재시도 이력만 생성하고 실제 실행은 스케줄러에서 처리
        verify(batchExecutionRepository).save(any(BatchExecution.class));
    }

    @Test
    @DisplayName("배치 재시도 - 원본 실행 이력 없음")
    void retryBatch_OriginalNotFound() {
        // given
        given(batchExecutionRepository.findByExecutionId(anyString()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> batchExecutionService.retryBatch("invalid-id", "admin"))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.BATCH_EXECUTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배치 재시도 - 최대 재시도 횟수 초과")
    void retryBatch_MaxRetryExceeded() {
        // given
        testExecution.setStatus(BatchStatus.FAIL.name());
        testExecution.setRetryCount(3);

        given(batchExecutionRepository.findByExecutionId(anyString()))
                .willReturn(Optional.of(testExecution));

        // when & then
        assertThatThrownBy(() -> batchExecutionService.retryBatch("exec-001", "admin"))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.MAX_RETRY_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("배치 재시도 - 성공 상태는 재시도 불가")
    void retryBatch_CannotRetrySuccess() {
        // given
        testExecution.setStatus(BatchStatus.SUCCESS.name());

        given(batchExecutionRepository.findByExecutionId(anyString()))
                .willReturn(Optional.of(testExecution));

        // when & then
        assertThatThrownBy(() -> batchExecutionService.retryBatch("exec-001", "admin"))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.CANNOT_RETRY_SUCCESS.getMessage());
    }

    @Test
    @DisplayName("배치 재시도 - 실행중 상태는 재시도 불가")
    void retryBatch_CannotRetryRunning() {
        // given
        testExecution.setStatus(BatchStatus.RUNNING.name());

        given(batchExecutionRepository.findByExecutionId(anyString()))
                .willReturn(Optional.of(testExecution));

        // when & then
        assertThatThrownBy(() -> batchExecutionService.retryBatch("exec-001", "admin"))
                .isInstanceOf(BatchException.class)
                .hasMessage(BatchExceptionMessage.CANNOT_RETRY_RUNNING.getMessage());
    }

    @Test
    @DisplayName("동시 실행 허용 - 실행중이어도 실행 가능")
    void executeBatch_AllowConcurrent() {
        // given
        testBatchJob.setAllowConcurrent(true);
        given(distributedLockService.acquireLock(anyString(), anyLong()))
                .willReturn("lock-value");
        given(batchExecutionRepository.save(any(BatchExecution.class)))
                .willReturn(testExecution);
        given(apiEndpointService.getApiEndpointByCode(anyString()))
                .willReturn(new ApiEndpoint());
        given(apiExecutionService.execute(any(), any()))
                .willReturn(successResponse);
        given(batchExecutionMapper.toDto(any(BatchExecution.class)))
                .willReturn(new BatchExecutionDTO());

        // when
        BatchExecutionDTO result = batchExecutionService.executeBatch(
                testBatchJob,
                BatchTriggerType.MANUAL,
                "admin"
        );

        // then
        assertThat(result).isNotNull();
        verify(apiExecutionService).execute(any(), any());
    }
}
