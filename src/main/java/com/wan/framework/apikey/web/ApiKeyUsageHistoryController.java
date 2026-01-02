package com.wan.framework.apikey.web;

import com.wan.framework.apikey.dto.ApiKeyUsageHistoryDTO;
import com.wan.framework.apikey.service.ApiKeyUsageHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api-key-usage-history")
public class ApiKeyUsageHistoryController {

    private final ApiKeyUsageHistoryService service;

    /**
     * API Key별 사용 이력 조회 (페이징)
     */
    @GetMapping("/api-key/{apiKeyId}")
    public ResponseEntity<Page<ApiKeyUsageHistoryDTO>> getUsageHistory(
            @PathVariable Long apiKeyId,
            @PageableDefault(size = 50, sort = "usedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.findByApiKeyId(apiKeyId, pageable));
    }

    /**
     * API Key별 사용 이력 조회 (기간 지정)
     */
    @GetMapping("/api-key/{apiKeyId}/range")
    public ResponseEntity<List<ApiKeyUsageHistoryDTO>> getUsageHistoryByDateRange(
            @PathVariable Long apiKeyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(service.findByApiKeyIdAndDateRange(apiKeyId, startDate, endDate));
    }

    /**
     * 성공/실패 여부로 필터링
     */
    @GetMapping("/api-key/{apiKeyId}/filter")
    public ResponseEntity<Page<ApiKeyUsageHistoryDTO>> getUsageHistoryBySuccess(
            @PathVariable Long apiKeyId,
            @RequestParam Boolean isSuccess,
            @PageableDefault(size = 50, sort = "usedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.findByApiKeyIdAndSuccess(apiKeyId, isSuccess, pageable));
    }

    /**
     * 전체 사용 이력 조회 (관리자용)
     */
    @GetMapping("/all")
    public ResponseEntity<Page<ApiKeyUsageHistoryDTO>> getAllUsageHistory(
            @PageableDefault(size = 50, sort = "usedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    /**
     * 특정 API Key의 총 사용 횟수
     */
    @GetMapping("/api-key/{apiKeyId}/count")
    public ResponseEntity<Long> getTotalUsageCount(@PathVariable Long apiKeyId) {
        return ResponseEntity.ok(service.countByApiKeyId(apiKeyId));
    }

    /**
     * 특정 API Key의 성공/실패 횟수
     */
    @GetMapping("/api-key/{apiKeyId}/count/success")
    public ResponseEntity<Long> getSuccessCount(
            @PathVariable Long apiKeyId,
            @RequestParam Boolean isSuccess) {
        return ResponseEntity.ok(service.countByApiKeyIdAndSuccess(apiKeyId, isSuccess));
    }
}
