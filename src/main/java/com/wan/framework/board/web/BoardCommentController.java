package com.wan.framework.board.web;

import com.wan.framework.board.dto.BoardCommentDTO;
import com.wan.framework.board.service.BoardCommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board-comments")
public class BoardCommentController {

    private final BoardCommentService service;

    @PostMapping
    public ResponseEntity<BoardCommentDTO> createComment(@RequestBody BoardCommentDTO dto, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        return ResponseEntity.ok(service.createComment(dto, userId));
    }

    @GetMapping("/board-data/{boardDataId}")
    public ResponseEntity<List<BoardCommentDTO>> getComments(@PathVariable Long boardDataId) {
        return ResponseEntity.ok(service.findByBoardDataId(boardDataId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardCommentDTO> updateComment(
            @PathVariable Long id,
            @RequestBody String content,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        return ResponseEntity.ok(service.updateComment(id, content, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        service.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
}
