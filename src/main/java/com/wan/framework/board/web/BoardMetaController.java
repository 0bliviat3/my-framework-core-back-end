package com.wan.framework.board.web;

import com.wan.framework.board.dto.BoardMetaDTO;
import com.wan.framework.board.service.BoardMetaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/board-metas")
public class BoardMetaController {

    private final BoardMetaService boardMetaService;

    /** 게시판 생성 */
    @PostMapping
    public BoardMetaDTO createBoard(@RequestBody BoardMetaDTO boardMetaDTO) {
        log.debug("boardMetaDTO: {}", boardMetaDTO);
        return boardMetaService.saveBoardMeta(boardMetaDTO);
    }

    /** 게시판 단건 조회 */
    @GetMapping("/{id}")
    public BoardMetaDTO getBoard(@PathVariable Long id) {
        return boardMetaService.findById(id);
    }

    /** 게시판 목록 조회 (페이징) */
    @GetMapping
    public Page<BoardMetaDTO> getBoards(@RequestParam(value = "page", defaultValue = "0") int pageNumber,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return boardMetaService.findAll(pageRequest);
    }

    /** 게시판 수정 */
    @PutMapping("/{id}")
    public BoardMetaDTO updateBoard(@PathVariable Long id, @RequestBody BoardMetaDTO boardMetaDTO) {
        boardMetaDTO.setId(id); // 안전하게 id 강제 주입
        return boardMetaService.modifyBoardMeta(boardMetaDTO);
    }

    /** 게시판 삭제 */
    @DeleteMapping("/{id}")
    public void deleteBoard(@PathVariable Long id) {
        boardMetaService.deleteProgram(id);
    }
}
