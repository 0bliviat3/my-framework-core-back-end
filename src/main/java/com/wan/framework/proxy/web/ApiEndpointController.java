package com.wan.framework.proxy.web;

import com.wan.framework.proxy.dto.ApiEndpointDTO;
import com.wan.framework.proxy.service.ApiEndpointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API 엔드포인트 관리 컨트롤러
 * - API 메타 정보 CRUD
 */
@Slf4j
@RestController
@RequestMapping("/api-endpoints")
@RequiredArgsConstructor
public class ApiEndpointController {

    private final ApiEndpointService apiEndpointService;

    /**
     * API 엔드포인트 생성
     */
    @PostMapping
    public ResponseEntity<ApiEndpointDTO> createApiEndpoint(@RequestBody ApiEndpointDTO dto) {
        log.info("Creating API endpoint: {}", dto.getApiCode());
        ApiEndpointDTO created = apiEndpointService.createApiEndpoint(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * API 엔드포인트 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiEndpointDTO> updateApiEndpoint(
            @PathVariable Long id,
            @RequestBody ApiEndpointDTO dto) {
        log.info("Updating API endpoint: {}", id);
        ApiEndpointDTO updated = apiEndpointService.updateApiEndpoint(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * API 엔드포인트 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiEndpoint(@PathVariable Long id) {
        log.info("Deleting API endpoint: {}", id);
        apiEndpointService.deleteApiEndpoint(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * API 엔드포인트 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiEndpointDTO> getApiEndpoint(@PathVariable Long id) {
        ApiEndpointDTO dto = apiEndpointService.getApiEndpoint(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * API 엔드포인트 목록 조회 (전체)
     */
    @GetMapping
    public ResponseEntity<Page<ApiEndpointDTO>> getAllApiEndpoints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApiEndpointDTO> endpoints = apiEndpointService.getAllApiEndpoints(page, size);
        return ResponseEntity.ok(endpoints);
    }

    /**
     * API 엔드포인트 목록 조회 (활성화만)
     */
    @GetMapping("/enabled")
    public ResponseEntity<Page<ApiEndpointDTO>> getEnabledApiEndpoints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ApiEndpointDTO> endpoints = apiEndpointService.getEnabledApiEndpoints(page, size);
        return ResponseEntity.ok(endpoints);
    }

    /**
     * API 엔드포인트 활성/비활성 토글
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<ApiEndpointDTO> toggleApiEndpoint(@PathVariable Long id) {
        log.info("Toggling API endpoint: {}", id);
        ApiEndpointDTO toggled = apiEndpointService.toggleApiEndpoint(id);
        return ResponseEntity.ok(toggled);
    }
}
