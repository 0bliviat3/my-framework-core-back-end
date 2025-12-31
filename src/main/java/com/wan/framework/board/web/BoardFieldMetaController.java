package com.wan.framework.board.web;

import com.wan.framework.board.dto.BoardFieldMetaDTO;
import com.wan.framework.board.service.BoardFieldMetaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board-fields")
public class BoardFieldMetaController {

    private final BoardFieldMetaService service;

    @PostMapping
    public ResponseEntity<BoardFieldMetaDTO> createField(@RequestBody BoardFieldMetaDTO dto) {
        return ResponseEntity.ok(service.createField(dto));
    }

    @GetMapping("/board-meta/{boardMetaId}")
    public ResponseEntity<List<BoardFieldMetaDTO>> getFieldsByBoardMeta(@PathVariable Long boardMetaId) {
        return ResponseEntity.ok(service.findByBoardMetaId(boardMetaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoardFieldMetaDTO> updateField(@PathVariable Long id, @RequestBody BoardFieldMetaDTO dto) {
        return ResponseEntity.ok(service.updateField(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id) {
        service.deleteField(id);
        return ResponseEntity.noContent().build();
    }
}
