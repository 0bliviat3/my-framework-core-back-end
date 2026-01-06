package com.wan.framework.batch.web;

import com.wan.framework.batch.dto.BatchExecutionDTO;
import com.wan.framework.batch.dto.BatchExecutionRequest;
import com.wan.framework.batch.service.BatchExecutionService;
import com.wan.framework.batch.service.BatchHistoryService;
import com.wan.framework.batch.service.BatchJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 배치 실행 컨트롤러
 * - 수동 실행
 * - 재시도
 * - 실행 이력 조회
 */
@Slf4j
@RestController
@RequestMapping("/batch-executions")
@RequiredArgsConstructor
public class BatchExecutionController {

    private final BatchExecutionService batchExecutionService;
    private final BatchHistoryService batchHistoryService;
    private final BatchJobService batchJobService;

    /**
     * 배치 수동 실행 (Run Now)
     */
    @PostMapping("/execute")
    public ResponseEntity<BatchExecutionDTO> executeBatch(@RequestBody BatchExecutionRequest request) {
        log.info("Manual batch execution requested: {}", request.getBatchId());

        var batchJob = batchJobService.getBatchJobByBatchId(request.getBatchId());
        BatchExecutionDTO execution = batchExecutionService.executeManual(batchJob, request);

        return ResponseEntity.ok(execution);
    }

    /**
     * 배치 재시도
     */
    @PostMapping("/retry/{executionId}")
    public ResponseEntity<BatchExecutionDTO> retryBatch(
            @PathVariable String executionId,
            @RequestParam String executedBy) {
        log.info("Batch retry requested: {}", executionId);

        BatchExecutionDTO execution = batchExecutionService.retryBatch(executionId, executedBy);
        return ResponseEntity.ok(execution);
    }

    /**
     * 실행 이력 조회 (ID)
     */
    @GetMapping("/{id}")
    public ResponseEntity<BatchExecutionDTO> getExecution(@PathVariable Long id) {
        BatchExecutionDTO dto = batchHistoryService.getExecution(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * 실행 이력 조회 (실행 ID)
     */
    @GetMapping("/execution-id/{executionId}")
    public ResponseEntity<BatchExecutionDTO> getExecutionByExecutionId(@PathVariable String executionId) {
        BatchExecutionDTO dto = batchHistoryService.getExecutionByExecutionId(executionId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 배치 작업별 실행 이력 조회
     */
    @GetMapping("/batch-job/{batchJobId}")
    public ResponseEntity<Page<BatchExecutionDTO>> getHistoryByBatchJob(
            @PathVariable Long batchJobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BatchExecutionDTO> history = batchHistoryService.getHistoryByBatchJob(batchJobId, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * 배치 ID별 실행 이력 조회
     */
    @GetMapping("/batch-id/{batchId}")
    public ResponseEntity<Page<BatchExecutionDTO>> getHistoryByBatchId(
            @PathVariable String batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BatchExecutionDTO> history = batchHistoryService.getHistoryByBatchId(batchId, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * 상태별 실행 이력 조회
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BatchExecutionDTO>> getHistoryByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BatchExecutionDTO> history = batchHistoryService.getHistoryByStatus(status, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * 트리거 타입별 실행 이력 조회
     */
    @GetMapping("/trigger-type/{triggerType}")
    public ResponseEntity<Page<BatchExecutionDTO>> getHistoryByTriggerType(
            @PathVariable String triggerType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BatchExecutionDTO> history = batchHistoryService.getHistoryByTriggerType(triggerType, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * 기간별 실행 이력 조회
     */
    @GetMapping("/period")
    public ResponseEntity<List<BatchExecutionDTO>> getHistoryByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<BatchExecutionDTO> history = batchHistoryService.getHistoryByPeriod(startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * 최근 실행 이력 조회
     */
    @GetMapping("/recent/{batchId}")
    public ResponseEntity<List<BatchExecutionDTO>> getRecentHistory(@PathVariable String batchId) {
        List<BatchExecutionDTO> history = batchHistoryService.getRecentHistory(batchId);
        return ResponseEntity.ok(history);
    }

    /**
     * 배치 실행 통계 조회
     */
    @GetMapping("/stats/{batchJobId}")
    public ResponseEntity<Object> getExecutionStats(@PathVariable Long batchJobId) {
        Object stats = batchHistoryService.getExecutionStats(batchJobId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 재시도 대상 조회
     */
    @GetMapping("/retry-targets")
    public ResponseEntity<List<BatchExecutionDTO>> getRetryTargets() {
        List<BatchExecutionDTO> targets = batchHistoryService.getRetryTargets();
        return ResponseEntity.ok(targets);
    }
}
