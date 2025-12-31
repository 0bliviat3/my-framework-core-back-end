package com.wan.framework.board.service;

import com.wan.framework.board.domain.BoardComment;
import com.wan.framework.board.domain.BoardData;
import com.wan.framework.board.dto.BoardCommentDTO;
import com.wan.framework.board.exception.BoardException;
import com.wan.framework.board.mapper.BoardCommentMapper;
import com.wan.framework.board.repository.BoardCommentRepository;
import com.wan.framework.board.repository.BoardDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.board.constant.BoardExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardCommentService {

    private final BoardCommentRepository repository;
    private final BoardCommentMapper mapper;
    private final BoardDataRepository boardDataRepository;

    @Transactional
    public BoardCommentDTO createComment(BoardCommentDTO dto, String authorId) {
        BoardData boardData = boardDataRepository.findByIdAndDataStateCodeNot(dto.getBoardDataId(), D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_BOARD_DATA));

        if (!boardData.getBoardMeta().getUseComment()) {
            throw new BoardException(COMMENT_NOT_ALLOWED);
        }

        BoardComment entity = mapper.toEntity(dto);
        entity.setBoardData(boardData);
        entity.setAuthorId(authorId);

        // 부모 댓글 설정 (대댓글인 경우)
        if (dto.getParentId() != null) {
            BoardComment parent = repository.findByIdAndDataStateCodeNot(dto.getParentId(), D)
                    .orElseThrow(() -> new BoardException(NOT_FOUND_COMMENT));
            entity.setParent(parent);
        }

        BoardComment saved = repository.save(entity);
        boardData.incrementCommentCount();

        return mapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<BoardCommentDTO> findByBoardDataId(Long boardDataId) {
        List<BoardComment> comments = repository.findAllWithChildrenByBoardDataId(boardDataId, D);

        return comments.stream()
                .map(comment -> {
                    BoardCommentDTO dto = mapper.toDto(comment);
                    List<BoardCommentDTO> children = comment.getChildren().stream()
                            .filter(child -> child.getDataStateCode() != D)
                            .map(mapper::toDto)
                            .collect(Collectors.toList());
                    dto.setChildren(children);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardCommentDTO updateComment(Long id, String content, String userId) {
        BoardComment entity = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_COMMENT));

        if (!entity.getAuthorId().equals(userId)) {
            throw new BoardException(CANNOT_MODIFY_OTHER_COMMENT);
        }

        entity.updateContent(content);
        return mapper.toDto(entity);
    }

    @Transactional
    public void deleteComment(Long id, String userId) {
        BoardComment entity = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_COMMENT));

        if (!entity.getAuthorId().equals(userId)) {
            throw new BoardException(CANNOT_DELETE_OTHER_COMMENT);
        }

        entity.setDataStateCode(D);
        entity.getBoardData().decrementCommentCount();
    }
}
