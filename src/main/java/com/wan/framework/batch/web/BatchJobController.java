package com.wan.framework.batch.web;

import com.wan.framework.batch.dto.BatchJobDTO;
import com.wan.framework.batch.service.BatchJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 배치 작업 관리 컨트롤러
 * - 배치 작업 CRUD
 * - 활성화/비활성화
 */
@Slf4j
@RestController
@RequestMapping("/batch-jobs")
@RequiredArgsConstructor
public class BatchJobController {

    private final BatchJobService batchJobService;

    /**
     * 배치 작업 생성
     */
    @PostMapping
    public ResponseEntity<BatchJobDTO> createBatchJob(@RequestBody BatchJobDTO dto) {
        log.info("Creating batch job: {}", dto.getBatchId());
        BatchJobDTO created = batchJobService.createBatchJob(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 배치 작업 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<BatchJobDTO> updateBatchJob(
            @PathVariable Long id,
            @RequestBody BatchJobDTO dto) {
        log.info("Updating batch job: {}", id);
        BatchJobDTO updated = batchJobService.updateBatchJob(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 배치 작업 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBatchJob(@PathVariable Long id) {
        log.info("Deleting batch job: {}", id);
        batchJobService.deleteBatchJob(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 배치 작업 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<BatchJobDTO> getBatchJob(@PathVariable Long id) {
        BatchJobDTO dto = batchJobService.getBatchJob(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * 배치 작업 목록 조회 (전체)
     */
    @GetMapping
    public ResponseEntity<Page<BatchJobDTO>> getAllBatchJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BatchJobDTO> batchJobs = batchJobService.getAllBatchJobs(page, size);
        return ResponseEntity.ok(batchJobs);
    }

    /**
     * 배치 작업 목록 조회 (활성화만)
     */
    @GetMapping("/enabled")
    public ResponseEntity<Page<BatchJobDTO>> getEnabledBatchJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BatchJobDTO> batchJobs = batchJobService.getEnabledBatchJobs(page, size);
        return ResponseEntity.ok(batchJobs);
    }

    /**
     * 배치 작업 활성화/비활성화 토글
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<BatchJobDTO> toggleBatchJob(@PathVariable Long id) {
        log.info("Toggling batch job: {}", id);
        BatchJobDTO toggled = batchJobService.toggleBatchJob(id);
        return ResponseEntity.ok(toggled);
    }
}
