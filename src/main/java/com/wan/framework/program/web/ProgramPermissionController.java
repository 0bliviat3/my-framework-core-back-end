package com.wan.framework.program.web;

import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.program.dto.ProgramApiMappingDTO;
import com.wan.framework.program.service.ProgramPermissionIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Program-Permission 통합 Controller
 * - Program과 API 매핑 관리
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/programs")
public class ProgramPermissionController {

    private final ProgramPermissionIntegrationService integrationService;

    /**
     * Program에 API 추가
     */
    @PostMapping("/{programId}/apis/{apiId}")
    public ResponseEntity<String> addApiToProgram(
            @PathVariable Long programId,
            @PathVariable Long apiId,
            @RequestParam(required = false, defaultValue = "true") Boolean required,
            @RequestParam(required = false) String description) {

        log.info("POST /programs/{}/apis/{} - required: {}", programId, apiId, required);
        integrationService.addApiToProgram(programId, apiId, required, description);
        return ResponseEntity.ok("API added to program successfully");
    }

    /**
     * Program에서 API 제거
     */
    @DeleteMapping("/{programId}/apis/{apiId}")
    public ResponseEntity<String> removeApiFromProgram(
            @PathVariable Long programId,
            @PathVariable Long apiId) {

        log.info("DELETE /programs/{}/apis/{}", programId, apiId);
        integrationService.removeApiFromProgram(programId, apiId);
        return ResponseEntity.ok("API removed from program successfully");
    }

    /**
     * Program별 API 목록 조회
     */
    @GetMapping("/{programId}/apis")
    public ResponseEntity<List<ProgramApiMappingDTO>> getProgramApis(@PathVariable Long programId) {
        log.info("GET /programs/{}/apis", programId);
        List<ProgramApiMappingDTO> mappings = integrationService.getProgramApis(programId);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Program별 필수 API 목록 조회
     */
    @GetMapping("/{programId}/apis/required")
    public ResponseEntity<List<ApiRegistry>> getRequiredApis(@PathVariable Long programId) {
        log.info("GET /programs/{}/apis/required", programId);
        List<ApiRegistry> apis = integrationService.getRequiredApis(programId);
        return ResponseEntity.ok(apis);
    }

    /**
     * Menu에 필요한 API 계산
     */
    @GetMapping("/{programId}/required-apis")
    public ResponseEntity<Set<String>> calculateRequiredApis(@PathVariable Long programId) {
        log.info("GET /programs/{}/required-apis", programId);
        Set<String> apiIdentifiers = integrationService.calculateRequiredApisForMenu(programId);
        return ResponseEntity.ok(apiIdentifiers);
    }

    /**
     * 활성 API 목록 조회 (매핑용)
     */
    @GetMapping("/available-apis")
    public ResponseEntity<List<ApiRegistry>> getAvailableApis() {
        log.info("GET /programs/available-apis");
        List<ApiRegistry> apis = integrationService.getActiveApis();
        return ResponseEntity.ok(apis);
    }
}
