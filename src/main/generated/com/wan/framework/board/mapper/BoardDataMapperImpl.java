package com.wan.framework.board.mapper;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.BoardDataStatus;
import com.wan.framework.board.domain.BoardData;
import com.wan.framework.board.domain.BoardMeta;
import com.wan.framework.board.dto.BoardDataDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-15T18:20:50+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class BoardDataMapperImpl implements BoardDataMapper {

    @Override
    public BoardDataDTO toDto(BoardData entity) {
        if ( entity == null ) {
            return null;
        }

        BoardDataDTO.BoardDataDTOBuilder boardDataDTO = BoardDataDTO.builder();

        boardDataDTO.boardMetaId( entityBoardMetaId( entity ) );
        boardDataDTO.boardMetaTitle( entityBoardMetaTitle( entity ) );
        boardDataDTO.id( entity.getId() );
        boardDataDTO.authorId( entity.getAuthorId() );
        boardDataDTO.title( entity.getTitle() );
        boardDataDTO.content( entity.getContent() );
        boardDataDTO.fieldDataJson( entity.getFieldDataJson() );
        boardDataDTO.status( entity.getStatus() );
        boardDataDTO.viewCount( entity.getViewCount() );
        boardDataDTO.commentCount( entity.getCommentCount() );
        boardDataDTO.dataStateCode( entity.getDataStateCode() );
        boardDataDTO.createdAt( entity.getCreatedAt() );
        boardDataDTO.updatedAt( entity.getUpdatedAt() );
        boardDataDTO.publishedAt( entity.getPublishedAt() );

        return boardDataDTO.build();
    }

    @Override
    public BoardData toEntity(BoardDataDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BoardData boardData = new BoardData();

        boardData.setBoardMeta( boardDataDTOToBoardMeta( dto ) );
        if ( dto.getStatus() != null ) {
            boardData.setStatus( dto.getStatus() );
        }
        else {
            boardData.setStatus( BoardDataStatus.PUBLISHED );
        }
        if ( dto.getDataStateCode() != null ) {
            boardData.setDataStateCode( dto.getDataStateCode() );
        }
        else {
            boardData.setDataStateCode( DataStateCode.I );
        }
        if ( dto.getViewCount() != null ) {
            boardData.setViewCount( dto.getViewCount() );
        }
        else {
            boardData.setViewCount( (long) 0L );
        }
        if ( dto.getCommentCount() != null ) {
            boardData.setCommentCount( dto.getCommentCount() );
        }
        else {
            boardData.setCommentCount( 0 );
        }
        boardData.setId( dto.getId() );
        boardData.setAuthorId( dto.getAuthorId() );
        boardData.setTitle( dto.getTitle() );
        boardData.setContent( dto.getContent() );
        boardData.setFieldDataJson( dto.getFieldDataJson() );

        return boardData;
    }

    @Override
    public void updateEntityFromDto(BoardDataDTO dto, BoardData entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getTitle() != null ) {
            entity.setTitle( dto.getTitle() );
        }
        if ( dto.getContent() != null ) {
            entity.setContent( dto.getContent() );
        }
        if ( dto.getFieldDataJson() != null ) {
            entity.setFieldDataJson( dto.getFieldDataJson() );
        }
        if ( dto.getStatus() != null ) {
            entity.setStatus( dto.getStatus() );
        }
    }

    private Long entityBoardMetaId(BoardData boardData) {
        if ( boardData == null ) {
            return null;
        }
        BoardMeta boardMeta = boardData.getBoardMeta();
        if ( boardMeta == null ) {
            return null;
        }
        Long id = boardMeta.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityBoardMetaTitle(BoardData boardData) {
        if ( boardData == null ) {
            return null;
        }
        BoardMeta boardMeta = boardData.getBoardMeta();
        if ( boardMeta == null ) {
            return null;
        }
        String title = boardMeta.getTitle();
        if ( title == null ) {
            return null;
        }
        return title;
    }

    protected BoardMeta boardDataDTOToBoardMeta(BoardDataDTO boardDataDTO) {
        if ( boardDataDTO == null ) {
            return null;
        }

        BoardMeta boardMeta = new BoardMeta();

        boardMeta.setId( boardDataDTO.getBoardMetaId() );

        return boardMeta;
    }
}
