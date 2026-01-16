package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardAttachment;
import com.wan.framework.board.domain.BoardData;
import com.wan.framework.board.dto.BoardAttachmentDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-15T18:20:50+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class BoardAttachmentMapperImpl implements BoardAttachmentMapper {

    @Override
    public BoardAttachmentDTO toDto(BoardAttachment entity) {
        if ( entity == null ) {
            return null;
        }

        BoardAttachmentDTO.BoardAttachmentDTOBuilder boardAttachmentDTO = BoardAttachmentDTO.builder();

        boardAttachmentDTO.boardDataId( entityBoardDataId( entity ) );
        boardAttachmentDTO.id( entity.getId() );
        boardAttachmentDTO.originalFileName( entity.getOriginalFileName() );
        boardAttachmentDTO.savedFileName( entity.getSavedFileName() );
        boardAttachmentDTO.filePath( entity.getFilePath() );
        boardAttachmentDTO.fileSize( entity.getFileSize() );
        boardAttachmentDTO.contentType( entity.getContentType() );
        boardAttachmentDTO.displayOrder( entity.getDisplayOrder() );
        boardAttachmentDTO.uploadedBy( entity.getUploadedBy() );
        boardAttachmentDTO.downloadCount( entity.getDownloadCount() );
        boardAttachmentDTO.dataStateCode( entity.getDataStateCode() );
        boardAttachmentDTO.createdAt( entity.getCreatedAt() );

        boardAttachmentDTO.formattedFileSize( entity.getFormattedFileSize() );
        boardAttachmentDTO.fileExtension( entity.getFileExtension() );

        return boardAttachmentDTO.build();
    }

    @Override
    public BoardAttachment toEntity(BoardAttachmentDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BoardAttachment.BoardAttachmentBuilder boardAttachment = BoardAttachment.builder();

        boardAttachment.boardData( boardAttachmentDTOToBoardData( dto ) );
        boardAttachment.id( dto.getId() );
        boardAttachment.originalFileName( dto.getOriginalFileName() );
        boardAttachment.savedFileName( dto.getSavedFileName() );
        boardAttachment.filePath( dto.getFilePath() );
        boardAttachment.fileSize( dto.getFileSize() );
        boardAttachment.contentType( dto.getContentType() );
        boardAttachment.displayOrder( dto.getDisplayOrder() );
        boardAttachment.uploadedBy( dto.getUploadedBy() );
        boardAttachment.downloadCount( dto.getDownloadCount() );
        boardAttachment.dataStateCode( dto.getDataStateCode() );

        return boardAttachment.build();
    }

    @Override
    public List<BoardAttachmentDTO> toDtoList(List<BoardAttachment> entities) {
        if ( entities == null ) {
            return null;
        }

        List<BoardAttachmentDTO> list = new ArrayList<BoardAttachmentDTO>( entities.size() );
        for ( BoardAttachment boardAttachment : entities ) {
            list.add( toDto( boardAttachment ) );
        }

        return list;
    }

    @Override
    public void updateEntityFromDto(BoardAttachmentDTO dto, BoardAttachment entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getOriginalFileName() != null ) {
            entity.setOriginalFileName( dto.getOriginalFileName() );
        }
        if ( dto.getSavedFileName() != null ) {
            entity.setSavedFileName( dto.getSavedFileName() );
        }
        if ( dto.getFilePath() != null ) {
            entity.setFilePath( dto.getFilePath() );
        }
        if ( dto.getFileSize() != null ) {
            entity.setFileSize( dto.getFileSize() );
        }
        if ( dto.getContentType() != null ) {
            entity.setContentType( dto.getContentType() );
        }
        if ( dto.getDisplayOrder() != null ) {
            entity.setDisplayOrder( dto.getDisplayOrder() );
        }
        if ( dto.getUploadedBy() != null ) {
            entity.setUploadedBy( dto.getUploadedBy() );
        }
        if ( dto.getDownloadCount() != null ) {
            entity.setDownloadCount( dto.getDownloadCount() );
        }
    }

    private Long entityBoardDataId(BoardAttachment boardAttachment) {
        if ( boardAttachment == null ) {
            return null;
        }
        BoardData boardData = boardAttachment.getBoardData();
        if ( boardData == null ) {
            return null;
        }
        Long id = boardData.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected BoardData boardAttachmentDTOToBoardData(BoardAttachmentDTO boardAttachmentDTO) {
        if ( boardAttachmentDTO == null ) {
            return null;
        }

        BoardData.BoardDataBuilder boardData = BoardData.builder();

        boardData.id( boardAttachmentDTO.getBoardDataId() );

        return boardData.build();
    }
}
