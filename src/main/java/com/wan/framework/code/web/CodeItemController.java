package com.wan.framework.code.web;

import com.wan.framework.code.dto.CodeItemDTO;
import com.wan.framework.code.service.CodeItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 공통코드 항목 REST API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/code-items")
public class CodeItemController {

    private final CodeItemService codeItemService;

    /**
     * 코드 항목 생성
     */
    @PostMapping
    public ResponseEntity<CodeItemDTO> createCodeItem(@RequestBody CodeItemDTO dto) {
        log.info("POST /code-items - {}:{}", dto.getGroupCode(), dto.getCodeValue());
        CodeItemDTO created = codeItemService.createCodeItem(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * 코드 항목 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<CodeItemDTO> updateCodeItem(
            @PathVariable Long id,
            @RequestBody CodeItemDTO dto) {
        log.info("PUT /code-items/{} - {}", id, dto.getCodeName());
        CodeItemDTO updated = codeItemService.updateCodeItem(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 코드 항목 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCodeItem(@PathVariable Long id) {
        log.info("DELETE /code-items/{}", id);
        codeItemService.deleteCodeItem(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 코드 항목 조회 (단건)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CodeItemDTO> getCodeItem(@PathVariable Long id) {
        log.info("GET /code-items/{}", id);
        CodeItemDTO item = codeItemService.getCodeItem(id);
        return ResponseEntity.ok(item);
    }

    /**
     * 그룹별 코드 조회
     */
    @GetMapping("/group/{groupCode}")
    public ResponseEntity<List<CodeItemDTO>> getCodeItemsByGroup(@PathVariable String groupCode) {
        log.info("GET /code-items/group/{}", groupCode);
        List<CodeItemDTO> items = codeItemService.getCodeItemsByGroup(groupCode);
        return ResponseEntity.ok(items);
    }

    /**
     * 그룹별 활성화된 코드 조회
     */
    @GetMapping("/group/{groupCode}/enabled")
    public ResponseEntity<List<CodeItemDTO>> getEnabledCodeItemsByGroup(@PathVariable String groupCode) {
        log.info("GET /code-items/group/{}/enabled", groupCode);
        List<CodeItemDTO> items = codeItemService.getEnabledCodeItemsByGroup(groupCode);
        return ResponseEntity.ok(items);
    }

    /**
     * 전체 코드 조회 (페이징)
     */
    @GetMapping
    public Page<CodeItemDTO> getAllCodeItems(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        log.info("GET /code-items - page: {}, size: {}", pageNumber, pageSize);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return codeItemService.getAllCodeItems(pageRequest);
    }

    /**
     * 그룹별 코드 조회 (페이징)
     */
    @GetMapping("/group/{groupCode}/paged")
    public Page<CodeItemDTO> getCodeItemsByGroupPaged(
            @PathVariable String groupCode,
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        log.info("GET /code-items/group/{}/paged - page: {}, size: {}", groupCode, pageNumber, pageSize);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return codeItemService.getCodeItemsByGroup(groupCode, pageRequest);
    }

    /**
     * 코드명으로 검색
     */
    @GetMapping("/search")
    public Page<CodeItemDTO> searchCodeItems(
            @RequestParam String codeName,
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        log.info("GET /code-items/search - codeName: {}", codeName);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return codeItemService.searchCodeItemsByName(codeName, pageRequest);
    }

    /**
     * 그룹 코드와 코드 값으로 조회
     */
    @GetMapping("/value")
    public ResponseEntity<CodeItemDTO> getCodeItemByValue(
            @RequestParam String groupCode,
            @RequestParam String codeValue) {
        log.info("GET /code-items/value - {}:{}", groupCode, codeValue);
        CodeItemDTO item = codeItemService.getCodeItemByValue(groupCode, codeValue);
        return ResponseEntity.ok(item);
    }

    /**
     * 그룹별 코드 Map 조회 (codeValue -> CodeItemDTO)
     */
    @GetMapping("/group/{groupCode}/map")
    public ResponseEntity<Map<String, CodeItemDTO>> getCodeItemsAsMap(@PathVariable String groupCode) {
        log.info("GET /code-items/group/{}/map", groupCode);
        Map<String, CodeItemDTO> itemMap = codeItemService.getCodeItemsAsMap(groupCode);
        return ResponseEntity.ok(itemMap);
    }

    /**
     * 코드 항목 활성화/비활성화 토글
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CodeItemDTO> toggleCodeItem(@PathVariable Long id) {
        log.info("PATCH /code-items/{}/toggle", id);
        CodeItemDTO toggled = codeItemService.toggleCodeItem(id);
        return ResponseEntity.ok(toggled);
    }

    /**
     * 그룹별 캐시 갱신
     */
    @PostMapping("/cache/refresh/{groupCode}")
    public ResponseEntity<String> refreshGroupCache(@PathVariable String groupCode) {
        log.info("POST /code-items/cache/refresh/{}", groupCode);
        codeItemService.refreshGroupCache(groupCode);
        return ResponseEntity.ok("Cache refreshed for group: " + groupCode);
    }

    /**
     * 전체 캐시 갱신
     */
    @PostMapping("/cache/refresh")
    public ResponseEntity<String> refreshAllCache() {
        log.info("POST /code-items/cache/refresh");
        codeItemService.refreshAllCache();
        return ResponseEntity.ok("All cache refreshed successfully");
    }
}
