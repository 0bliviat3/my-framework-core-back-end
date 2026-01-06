package com.wan.framework.code.web;

import com.wan.framework.code.dto.CodeGroupDTO;
import com.wan.framework.code.service.CodeGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공통코드 그룹 REST API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/code-groups")
public class CodeGroupController {

    private final CodeGroupService codeGroupService;

    /**
     * 코드 그룹 생성
     */
    @PostMapping
    public ResponseEntity<CodeGroupDTO> createCodeGroup(@RequestBody CodeGroupDTO dto) {
        log.info("POST /code-groups - groupCode: {}", dto.getGroupCode());
        CodeGroupDTO created = codeGroupService.createCodeGroup(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * 코드 그룹 수정
     */
    @PutMapping("/{groupCode}")
    public ResponseEntity<CodeGroupDTO> updateCodeGroup(
            @PathVariable String groupCode,
            @RequestBody CodeGroupDTO dto) {
        log.info("PUT /code-groups/{} - {}", groupCode, dto.getGroupName());
        CodeGroupDTO updated = codeGroupService.updateCodeGroup(groupCode, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 코드 그룹 삭제
     */
    @DeleteMapping("/{groupCode}")
    public ResponseEntity<Void> deleteCodeGroup(@PathVariable String groupCode) {
        log.info("DELETE /code-groups/{}", groupCode);
        codeGroupService.deleteCodeGroup(groupCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * 코드 그룹 조회 (단건)
     */
    @GetMapping("/{groupCode}")
    public ResponseEntity<CodeGroupDTO> getCodeGroup(@PathVariable String groupCode) {
        log.info("GET /code-groups/{}", groupCode);
        CodeGroupDTO group = codeGroupService.getCodeGroup(groupCode);
        return ResponseEntity.ok(group);
    }

    /**
     * 전체 그룹 조회 (페이징)
     */
    @GetMapping
    public Page<CodeGroupDTO> getAllCodeGroups(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        log.info("GET /code-groups - page: {}, size: {}", pageNumber, pageSize);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return codeGroupService.getAllCodeGroups(pageRequest);
    }

    /**
     * 전체 그룹 조회 (목록)
     */
    @GetMapping("/list")
    public ResponseEntity<List<CodeGroupDTO>> getAllCodeGroupsList() {
        log.info("GET /code-groups/list");
        List<CodeGroupDTO> groups = codeGroupService.getAllCodeGroupsList();
        return ResponseEntity.ok(groups);
    }

    /**
     * 활성화된 그룹 조회
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<CodeGroupDTO>> getEnabledCodeGroups() {
        log.info("GET /code-groups/enabled");
        List<CodeGroupDTO> groups = codeGroupService.getEnabledCodeGroups();
        return ResponseEntity.ok(groups);
    }

    /**
     * 그룹명으로 검색
     */
    @GetMapping("/search")
    public Page<CodeGroupDTO> searchCodeGroups(
            @RequestParam String groupName,
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        log.info("GET /code-groups/search - groupName: {}", groupName);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return codeGroupService.searchCodeGroupsByName(groupName, pageRequest);
    }

    /**
     * 그룹 활성화/비활성화 토글
     */
    @PatchMapping("/{groupCode}/toggle")
    public ResponseEntity<CodeGroupDTO> toggleCodeGroup(@PathVariable String groupCode) {
        log.info("PATCH /code-groups/{}/toggle", groupCode);
        CodeGroupDTO toggled = codeGroupService.toggleCodeGroup(groupCode);
        return ResponseEntity.ok(toggled);
    }

    /**
     * 전체 그룹 캐시 갱신
     */
    @PostMapping("/cache/refresh")
    public ResponseEntity<String> refreshAllCache() {
        log.info("POST /code-groups/cache/refresh");
        codeGroupService.refreshAllCache();
        return ResponseEntity.ok("Cache refreshed successfully");
    }
}
