package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardComment;
import com.wan.framework.board.domain.BoardData;
import com.wan.framework.board.dto.BoardCommentDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T09:53:31+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class BoardCommentMapperImpl implements BoardCommentMapper {

    @Override
    public BoardCommentDTO toDto(BoardComment entity) {
        if ( entity == null ) {
            return null;
        }

        BoardCommentDTO.BoardCommentDTOBuilder boardCommentDTO = BoardCommentDTO.builder();

        boardCommentDTO.boardDataId( entityBoardDataId( entity ) );
        boardCommentDTO.parentId( entityParentId( entity ) );
        boardCommentDTO.id( entity.getId() );
        boardCommentDTO.authorId( entity.getAuthorId() );
        boardCommentDTO.content( entity.getContent() );
        boardCommentDTO.isModified( entity.getIsModified() );
        boardCommentDTO.dataStateCode( entity.getDataStateCode() );
        boardCommentDTO.createdAt( entity.getCreatedAt() );
        boardCommentDTO.updatedAt( entity.getUpdatedAt() );
        boardCommentDTO.deletedAt( entity.getDeletedAt() );

        boardCommentDTO.childCount( entity.getChildCount() );

        return boardCommentDTO.build();
    }

    @Override
    public BoardComment toEntity(BoardCommentDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BoardComment.BoardCommentBuilder boardComment = BoardComment.builder();

        boardComment.boardData( boardCommentDTOToBoardData( dto ) );
        boardComment.parent( boardCommentDTOToBoardComment( dto ) );
        boardComment.id( dto.getId() );
        boardComment.authorId( dto.getAuthorId() );
        boardComment.content( dto.getContent() );
        boardComment.isModified( dto.getIsModified() );
        boardComment.dataStateCode( dto.getDataStateCode() );

        return boardComment.build();
    }

    @Override
    public List<BoardCommentDTO> toDtoList(List<BoardComment> entities) {
        if ( entities == null ) {
            return null;
        }

        List<BoardCommentDTO> list = new ArrayList<BoardCommentDTO>( entities.size() );
        for ( BoardComment boardComment : entities ) {
            list.add( toDto( boardComment ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(BoardCommentDTO dto, BoardComment entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getContent() != null ) {
            entity.setContent( dto.getContent() );
        }
    }

    private Long entityBoardDataId(BoardComment boardComment) {
        if ( boardComment == null ) {
            return null;
        }
        BoardData boardData = boardComment.getBoardData();
        if ( boardData == null ) {
            return null;
        }
        Long id = boardData.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long entityParentId(BoardComment boardComment) {
        if ( boardComment == null ) {
            return null;
        }
        BoardComment parent = boardComment.getParent();
        if ( parent == null ) {
            return null;
        }
        Long id = parent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected BoardData boardCommentDTOToBoardData(BoardCommentDTO boardCommentDTO) {
        if ( boardCommentDTO == null ) {
            return null;
        }

        BoardData.BoardDataBuilder boardData = BoardData.builder();

        boardData.id( boardCommentDTO.getBoardDataId() );

        return boardData.build();
    }

    protected BoardComment boardCommentDTOToBoardComment(BoardCommentDTO boardCommentDTO) {
        if ( boardCommentDTO == null ) {
            return null;
        }

        BoardComment.BoardCommentBuilder boardComment = BoardComment.builder();

        boardComment.id( boardCommentDTO.getParentId() );

        return boardComment.build();
    }
}
