package com.wan.framework.session.web;

import com.wan.framework.session.dto.LoginRequest;
import com.wan.framework.session.dto.SessionDTO;
import com.wan.framework.session.service.SessionService;
import com.wan.framework.user.service.SignService;
import com.wan.framework.user.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 세션 REST API
 * - 로그인/로그아웃
 * - 세션 조회/갱신/검증
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final SignService signService;

    /**
     * 로그인 (세션 생성)
     */
    @PostMapping("/login")
    public ResponseEntity<SessionDTO> login(@RequestBody LoginRequest request,
                                           HttpServletRequest httpRequest,
                                           HttpServletResponse httpResponse) {
        log.info("POST /sessions/login - userId: {}", request.getUserId());

        // 사용자 인증
        UserDTO loginUser = UserDTO.builder()
                .userId(request.getUserId())
                .password(request.getPassword())
                .build();
        UserDTO user = signService.signIn(loginUser);

        // 세션 생성 (실제 사용자 역할 사용)
        SessionDTO session = sessionService.createSession(
                httpRequest,
                httpResponse,
                user.getUserId(),
                user.getName(),
                user.getRoleNames()  // 사용자의 실제 역할 사용
        );

        return ResponseEntity.ok(session);
    }

    /**
     * 로그아웃 (세션 삭제)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                      HttpServletResponse response) {
        log.info("POST /sessions/logout");

        sessionService.deleteSession(request, response);
        return ResponseEntity.noContent().build();
    }

    /**
     * 현재 세션 정보 조회
     */
    @GetMapping("/current")
    public ResponseEntity<SessionDTO> getCurrentSession(HttpServletRequest request) {
        log.info("GET /sessions/current");

        SessionDTO session = sessionService.getCurrentSession(request);
        return ResponseEntity.ok(session);
    }

    /**
     * 세션 갱신 (TTL 연장)
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshSession(HttpServletRequest request) {
        log.info("POST /sessions/refresh");

        sessionService.refreshSession(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Session refreshed successfully");
        response.put("expiresIn", 1800);

        return ResponseEntity.ok(response);
    }

    /**
     * 세션 유효성 검증
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateSession(HttpServletRequest request) {
        log.info("GET /sessions/validate");

        boolean isValid = sessionService.validateSession(request);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        if (isValid) {
            SessionDTO session = sessionService.getCurrentSession(request);
            response.put("remainingTime", session.getRemainingTime());
        } else {
            response.put("reason", "Session not found or expired");
        }

        return ResponseEntity.ok(response);
    }
}
