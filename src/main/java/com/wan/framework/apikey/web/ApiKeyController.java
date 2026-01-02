package com.wan.framework.apikey.web;

import com.wan.framework.apikey.dto.ApiKeyDTO;
import com.wan.framework.apikey.dto.ApiKeyPermissionDTO;
import com.wan.framework.apikey.service.ApiKeyService;
import com.wan.framework.base.constant.AbleState;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api-keys")
public class ApiKeyController {

    private final ApiKeyService service;

    /**
     * API Key 생성
     */
    @PostMapping
    public ResponseEntity<ApiKeyDTO> createApiKey(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) LocalDateTime expiredAt,
            @RequestParam(required = false) List<String> permissions,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        ApiKeyDTO created = service.createApiKey(description, expiredAt, permissions, userId);
        return ResponseEntity.ok(created);
    }

    /**
     * API Key 목록 조회 (전체)
     */
    @GetMapping
    public ResponseEntity<Page<ApiKeyDTO>> getAllApiKeys(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.findAllApiKeys(pageable));
    }

    /**
     * 내 API Key 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<Page<ApiKeyDTO>> getMyApiKeys(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        return ResponseEntity.ok(service.findMyApiKeys(userId, pageable));
    }

    /**
     * API Key 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyDTO> getApiKey(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * API Key 활성화
     */
    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enableApiKey(@PathVariable Long id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        service.toggleApiKey(id, AbleState.ABLE, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * API Key 비활성화
     */
    @PutMapping("/{id}/disable")
    public ResponseEntity<Void> disableApiKey(@PathVariable Long id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        service.toggleApiKey(id, AbleState.DISABLE, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * API Key 논리 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        service.deleteApiKey(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 권한 추가
     */
    @PostMapping("/{id}/permissions")
    public ResponseEntity<Void> addPermission(
            @PathVariable Long id,
            @RequestParam String permission,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        service.addPermission(id, permission, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 권한 제거
     */
    @DeleteMapping("/{id}/permissions")
    public ResponseEntity<Void> removePermission(
            @PathVariable Long id,
            @RequestParam String permission) {
        service.removePermission(id, permission);
        return ResponseEntity.ok().build();
    }

    /**
     * API Key의 모든 권한 조회
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<ApiKeyPermissionDTO>> getPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(service.findPermissions(id));
    }
}
