package com.wan.framework.board.service;

import com.wan.framework.board.domain.BoardFieldMeta;
import com.wan.framework.board.domain.BoardMeta;
import com.wan.framework.board.dto.BoardFieldMetaDTO;
import com.wan.framework.board.exception.BoardException;
import com.wan.framework.board.mapper.BoardFieldMetaMapper;
import com.wan.framework.board.repository.BoardFieldMetaRepository;
import com.wan.framework.board.repository.BoardMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.base.constant.DataStateCode.U;
import static com.wan.framework.board.constant.BoardExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardFieldMetaService {

    private final BoardFieldMetaRepository repository;
    private final BoardFieldMetaMapper mapper;
    private final BoardMetaRepository boardMetaRepository;

    @Transactional
    public BoardFieldMetaDTO createField(BoardFieldMetaDTO dto) {
        // 게시판 존재 확인
        BoardMeta boardMeta = boardMetaRepository.findByIdAndDataStateCodeNot(dto.getBoardMetaId(), D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_META));

        // 중복 필드명 체크
        if (repository.existsByBoardMetaIdAndFieldNameAndDataStateCodeNot(
                dto.getBoardMetaId(), dto.getFieldName(), D)) {
            throw new BoardException(DUPLICATED_FIELD_NAME);
        }

        BoardFieldMeta entity = mapper.toEntity(dto);
        entity.setBoardMeta(boardMeta);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<BoardFieldMetaDTO> findByBoardMetaId(Long boardMetaId) {
        return repository.findByBoardMetaIdAndDataStateCodeNotOrderByDisplayOrder(boardMetaId, D)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardFieldMetaDTO updateField(Long id, BoardFieldMetaDTO dto) {
        BoardFieldMeta entity = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_FIELD_META));
        mapper.updateEntityFromDto(dto, entity);
        entity.setDataStateCode(U);
        return mapper.toDto(entity);
    }

    @Transactional
    public void deleteField(Long id) {
        BoardFieldMeta entity = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_FIELD_META));
        entity.setDataStateCode(D);
    }
}
