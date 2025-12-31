package com.wan.framework.board.web;

import com.wan.framework.board.dto.BoardDataDTO;
import com.wan.framework.board.service.BoardDataService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board-data")
public class BoardDataController {

    private final BoardDataService service;

    @PostMapping
    public ResponseEntity<BoardDataDTO> createPost(@RequestBody BoardDataDTO dto, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        return ResponseEntity.ok(service.createPost(dto, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDataDTO> getPost(@PathVariable Long id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        service.incrementViewCount(id); // 조회수 증가
        return ResponseEntity.ok(service.findById(id, userId));
    }

    @GetMapping("/board-meta/{boardMetaId}")
    public Page<BoardDataDTO> getPostList(
            @PathVariable Long boardMetaId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return service.findByBoardMeta(boardMetaId, PageRequest.of(page, size));
    }

    @GetMapping("/board-meta/{boardMetaId}/search")
    public Page<BoardDataDTO> searchPosts(
            @PathVariable Long boardMetaId,
            @RequestParam String title,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return service.searchByTitle(boardMetaId, title, PageRequest.of(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardDataDTO> updatePost(
            @PathVariable Long id,
            @RequestBody BoardDataDTO dto,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        return ResponseEntity.ok(service.updatePost(id, dto, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        service.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }
}
