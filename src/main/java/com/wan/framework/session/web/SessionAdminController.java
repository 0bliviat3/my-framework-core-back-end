package com.wan.framework.session.web;

import com.wan.framework.session.dto.SessionDTO;
import com.wan.framework.session.dto.SessionStatsDTO;
import com.wan.framework.session.service.SessionManagementService;
import com.wan.framework.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 세션 관리자 REST API
 * - 세션 통계 조회
 * - 전체 세션 조회
 * - 사용자별 세션 조회
 * - 세션 강제 종료
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/sessions")
public class SessionAdminController {

    private final SessionManagementService sessionManagementService;
    private final SessionService sessionService;

    /**
     * 전체 세션 목록 조회
     */
    @GetMapping
    public Page<SessionDTO> getAllSessions(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        log.info("GET /admin/sessions - page: {}, size: {}", pageNumber, pageSize);

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return sessionManagementService.getAllSessions(pageRequest);
    }

    /**
     * 세션 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<SessionStatsDTO> getSessionStats() {
        log.info("GET /admin/sessions/stats");

        SessionStatsDTO stats = sessionManagementService.getSessionStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * 사용자별 세션 목록 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionDTO>> getUserSessions(@PathVariable String userId) {
        log.info("GET /admin/sessions/user/{}", userId);

        List<SessionDTO> sessions = sessionManagementService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * 특정 세션 강제 종료
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> forceLogout(@PathVariable String sessionId,
                                                           @RequestParam(required = false) String adminId) {
        log.info("DELETE /admin/sessions/{} - adminId: {}", sessionId, adminId);

        String admin = adminId != null ? adminId : "SYSTEM";
        sessionService.forceLogout(sessionId, admin);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Session terminated successfully");
        response.put("sessionId", sessionId);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 전체 세션 종료
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> terminateUserSessions(@PathVariable String userId,
                                                                     @RequestParam(required = false) String adminId) {
        log.info("DELETE /admin/sessions/user/{} - adminId: {}", userId, adminId);

        String admin = adminId != null ? adminId : "SYSTEM";
        int terminatedCount = sessionManagementService.terminateUserSessions(userId, admin);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All sessions terminated for user");
        response.put("userId", userId);
        response.put("terminatedCount", terminatedCount);

        return ResponseEntity.ok(response);
    }
}
