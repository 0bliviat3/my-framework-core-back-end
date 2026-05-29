package com.wan.framework.security/controller;

import com.wan.framework.security.entity.SecurityAuditLog;
import com.wan.framework.security.service.SecurityAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Security Audit Log Management REST API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/security/logs")
public class SecurityAuditLogController {
    
    private final SecurityAuditLogService securityAuditLogService;
    
    /**
     * 보안 로그 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<SecurityAuditLog>> getAuditLogs() {
        log.info("GET /v2/security/logs");
        List<SecurityAuditLog> logs = securityAuditLogService.getRecentAuditLogs(100);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 특정 사용자의 보안 로그 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SecurityAuditLog>> getUserAuditLogs(
            @PathVariable String userId) {
        log.info("GET /v2/security/logs/user/{}", userId);
        // 실제 구현에서는 특정 사용자 로그를 가져오는 로직 추가
        return ResponseEntity.ok(List.of());
    }
}