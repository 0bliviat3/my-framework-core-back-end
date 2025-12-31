package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardMeta;
import com.wan.framework.board.dto.BoardMetaDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-31T11:17:04+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class BoardMetaMapperImpl implements BoardMetaMapper {

    @Override
    public BoardMetaDTO toDTO(BoardMeta boardMeta) {
        if ( boardMeta == null ) {
            return null;
        }

        BoardMetaDTO.BoardMetaDTOBuilder boardMetaDTO = BoardMetaDTO.builder();

        boardMetaDTO.id( boardMeta.getId() );
        boardMetaDTO.title( boardMeta.getTitle() );
        boardMetaDTO.description( boardMeta.getDescription() );
        boardMetaDTO.formDefinitionJson( boardMeta.getFormDefinitionJson() );
        boardMetaDTO.roles( boardMeta.getRoles() );
        boardMetaDTO.useComment( boardMeta.getUseComment() );
        boardMetaDTO.createdBy( boardMeta.getCreatedBy() );
        boardMetaDTO.dataStateCode( boardMeta.getDataStateCode() );
        boardMetaDTO.ableState( boardMeta.getAbleState() );

        return boardMetaDTO.build();
    }

    @Override
    public BoardMeta toEntity(BoardMetaDTO boardMetaDTO) {
        if ( boardMetaDTO == null ) {
            return null;
        }

        BoardMeta.BoardMetaBuilder boardMeta = BoardMeta.builder();

        boardMeta.id( boardMetaDTO.getId() );
        boardMeta.title( boardMetaDTO.getTitle() );
        boardMeta.description( boardMetaDTO.getDescription() );
        boardMeta.formDefinitionJson( boardMetaDTO.getFormDefinitionJson() );
        boardMeta.roles( boardMetaDTO.getRoles() );
        boardMeta.useComment( boardMetaDTO.getUseComment() );
        boardMeta.createdBy( boardMetaDTO.getCreatedBy() );
        boardMeta.dataStateCode( boardMetaDTO.getDataStateCode() );
        boardMeta.ableState( boardMetaDTO.getAbleState() );

        return boardMeta.build();
    }

    @Override
    public void updateEntityFromDto(BoardMetaDTO boardMetaDTO, BoardMeta entity) {
        if ( boardMetaDTO == null ) {
            return;
        }

        if ( boardMetaDTO.getId() != null ) {
            entity.setId( boardMetaDTO.getId() );
        }
        if ( boardMetaDTO.getTitle() != null ) {
            entity.setTitle( boardMetaDTO.getTitle() );
        }
        if ( boardMetaDTO.getDescription() != null ) {
            entity.setDescription( boardMetaDTO.getDescription() );
        }
        if ( boardMetaDTO.getFormDefinitionJson() != null ) {
            entity.setFormDefinitionJson( boardMetaDTO.getFormDefinitionJson() );
        }
        if ( boardMetaDTO.getRoles() != null ) {
            entity.setRoles( boardMetaDTO.getRoles() );
        }
        if ( boardMetaDTO.getUseComment() != null ) {
            entity.setUseComment( boardMetaDTO.getUseComment() );
        }
        if ( boardMetaDTO.getCreatedBy() != null ) {
            entity.setCreatedBy( boardMetaDTO.getCreatedBy() );
        }
        if ( boardMetaDTO.getDataStateCode() != null ) {
            entity.setDataStateCode( boardMetaDTO.getDataStateCode() );
        }
        if ( boardMetaDTO.getAbleState() != null ) {
            entity.setAbleState( boardMetaDTO.getAbleState() );
        }
    }
}
