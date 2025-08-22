package com.wan.framework.board.service;

import com.wan.framework.board.domain.BoardMeta;
import com.wan.framework.board.dto.BoardMetaDTO;
import com.wan.framework.board.exception.BoardException;
import com.wan.framework.board.mapper.BoardMetaMapper;
import com.wan.framework.board.repository.BoardMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.base.constant.DataStateCode.U;
import static com.wan.framework.board.constant.BoardExceptionMessage.DUPLICATED_TITLE;
import static com.wan.framework.board.constant.BoardExceptionMessage.NOT_FOUND_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardMetaService {

    private final BoardMetaMapper mapper;
    private final BoardMetaRepository repository;

    @Transactional
    public BoardMetaDTO saveBoardMeta(BoardMetaDTO metaDTO) {
        if (repository.existsByTitleAndDataStateCodeNot(metaDTO.getTitle(), D)) {
            throw new BoardException(DUPLICATED_TITLE);
        }
        BoardMeta meta = mapper.toEntity(metaDTO);
        meta = repository.save(meta);
        return mapper.toDTO(meta);
    }

    @Transactional(readOnly = true)
    public BoardMetaDTO findById(Long id) {
        BoardMeta meta = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_ID));
        return mapper.toDTO(meta);
    }

    @Transactional(readOnly = true)
    public Page<BoardMetaDTO> findAll(Pageable pageable) {
        return repository.findAllByDataStateCodeNot(pageable, D)
                .map(mapper::toDTO);
    }

    @Transactional
    public BoardMetaDTO modifyBoardMeta(BoardMetaDTO boardMetaDTO) {
        BoardMeta meta = repository.findByIdAndDataStateCodeNot(boardMetaDTO.getId(), D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_ID));
        mapper.updateEntityFromDto(boardMetaDTO, meta);
        meta.setDataStateCode(U);
        return mapper.toDTO(meta);
    }

    @Transactional
    public void deleteProgram(Long id) {
        BoardMeta meta = repository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new BoardException(NOT_FOUND_ID));
        meta.setDataStateCode(D);
    }

}
