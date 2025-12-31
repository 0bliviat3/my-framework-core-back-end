package com.wan.framework.board.service;

import com.wan.framework.board.constant.BoardDataStatus;
import com.wan.framework.board.domain.BoardData;
import com.wan.framework.board.domain.BoardMeta;
import com.wan.framework.board.dto.BoardDataDTO;
import com.wan.framework.board.exception.BoardException;
import com.wan.framework.board.mapper.BoardDataMapper;
import com.wan.framework.board.repository.BoardDataRepository;
import com.wan.framework.board.repository.BoardMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.base.constant.DataStateCode.U;
import static com.wan.framework.board.constant.BoardDataStatus.PUBLISHED;
import static com.wan.framework.board.constant.BoardExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardDataService {

    private final BoardDataRepository repository;
    private final BoardDataMapper mapper;
    private final BoardMetaRepository boardMetaRepository;

    @Transactional
    public BoardDataDTO createPost(BoardDataDTO dto, String authorId) {
        BoardMeta boardMeta = boardMetaRepository.findByIdAndDataStateCodeNot(dto.getBoardMetaId(), D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_META));

        BoardData entity = mapper.toEntity(dto);
        entity.setBoardMeta(boardMeta);
        entity.setAuthorId(authorId);

        return mapper.toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public BoardDataDTO findById(Long id, String userId) {
        BoardData entity = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_BOARD_DATA));

        BoardDataDTO dto = mapper.toDto(entity);

        // 이전글/다음글 조회
        List<BoardDataStatus> visibleStatuses = Arrays.asList(PUBLISHED, BoardDataStatus.PINNED);
        repository.findPrevious(entity.getBoardMeta().getId(), id, visibleStatuses, D)
                .ifPresent(prev -> {
                    dto.setPrevId(prev.getId());
                    dto.setPrevTitle(prev.getTitle());
                });

        repository.findNext(entity.getBoardMeta().getId(), id, visibleStatuses, D)
                .ifPresent(next -> {
                    dto.setNextId(next.getId());
                    dto.setNextTitle(next.getTitle());
                });

        return dto;
    }

    @Transactional
    public void incrementViewCount(Long id) {
        BoardData entity = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_BOARD_DATA));
        entity.incrementViewCount();
    }

    @Transactional(readOnly = true)
    public Page<BoardDataDTO> findByBoardMeta(Long boardMetaId, Pageable pageable) {
        List<BoardDataStatus> statuses = Arrays.asList(PUBLISHED, BoardDataStatus.PINNED);
        return repository.findByBoardMetaIdAndStatusInAndDataStateCodeNotOrderByStatusDescCreatedAtDesc(
                boardMetaId, statuses, D, pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<BoardDataDTO> searchByTitle(Long boardMetaId, String title, Pageable pageable) {
        List<BoardDataStatus> statuses = Arrays.asList(PUBLISHED, BoardDataStatus.PINNED);
        return repository.findByBoardMetaIdAndTitleContainingAndStatusInAndDataStateCodeNotOrderByStatusDescCreatedAtDesc(
                boardMetaId, title, statuses, D, pageable).map(mapper::toDto);
    }

    @Transactional
    public BoardDataDTO updatePost(Long id, BoardDataDTO dto, String userId) {
        BoardData entity = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_BOARD_DATA));

        if (!entity.getAuthorId().equals(userId)) {
            throw new BoardException(CANNOT_MODIFY_OTHER_POST);
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setDataStateCode(U);
        return mapper.toDto(entity);
    }

    @Transactional
    public void deletePost(Long id, String userId) {
        BoardData entity = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_BOARD_DATA));

        if (!entity.getAuthorId().equals(userId)) {
            throw new BoardException(CANNOT_DELETE_OTHER_POST);
        }

        entity.setDataStateCode(D);
    }
}
