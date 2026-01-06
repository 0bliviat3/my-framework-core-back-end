package com.wan.framework.proxy.web;

import com.wan.framework.proxy.dto.ApiExecutionHistoryDTO;
import com.wan.framework.proxy.service.ApiExecutionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 실행 이력 컨트롤러
 * - 실행 이력 조회
 * - 통계 조회
 */
@Slf4j
@RestController
@RequestMapping("/api-execution-history")
@RequiredArgsConstructor
public class ApiExecutionHistoryController {

    private final ApiExecutionHistoryService executionHistoryService;

    /**
     * 실행 이력 조회 (ID)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiExecutionHistoryDTO> getExecutionHistory(@PathVariable Long id) {
        ApiExecutionHistoryDTO dto = executionHistoryService.getExecutionHistory(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * API 엔드포인트별 실행 이력 조회
     */
    @GetMapping("/endpoint/{apiEndpointId}")
    public ResponseEntity<Page<ApiExecutionHistoryDTO>> getHistoryByEndpoint(
            @PathVariable Long apiEndpointId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApiExecutionHistoryDTO> history = executionHistoryService.getHistoryByEndpoint(apiEndpointId, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * API 코드별 실행 이력 조회
     */
    @GetMapping("/api-code/{apiCode}")
    public ResponseEntity<Page<ApiExecutionHistoryDTO>> getHistoryByApiCode(
            @PathVariable String apiCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApiExecutionHistoryDTO> history = executionHistoryService.getHistoryByApiCode(apiCode, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * 기간별 실행 이력 조회
     */
    @GetMapping("/period")
    public ResponseEntity<List<ApiExecutionHistoryDTO>> getHistoryByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<ApiExecutionHistoryDTO> history = executionHistoryService.getHistoryByPeriod(startDate, endDate);
        return ResponseEntity.ok(history);
    }

    /**
     * 성공/실패별 실행 이력 조회
     */
    @GetMapping("/success/{isSuccess}")
    public ResponseEntity<Page<ApiExecutionHistoryDTO>> getHistoryBySuccess(
            @PathVariable Boolean isSuccess,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApiExecutionHistoryDTO> history = executionHistoryService.getHistoryBySuccess(isSuccess, page, size);
        return ResponseEntity.ok(history);
    }

    /**
     * 최근 실행 이력 조회
     */
    @GetMapping("/recent/{apiCode}")
    public ResponseEntity<List<ApiExecutionHistoryDTO>> getRecentHistory(@PathVariable String apiCode) {
        List<ApiExecutionHistoryDTO> history = executionHistoryService.getRecentHistory(apiCode);
        return ResponseEntity.ok(history);
    }

    /**
     * API 엔드포인트 실행 통계 조회
     */
    @GetMapping("/stats/{apiEndpointId}")
    public ResponseEntity<Object> getExecutionStats(@PathVariable Long apiEndpointId) {
        Object stats = executionHistoryService.getExecutionStats(apiEndpointId);
        return ResponseEntity.ok(stats);
    }
}
