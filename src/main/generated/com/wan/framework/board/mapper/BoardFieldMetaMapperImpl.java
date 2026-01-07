package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardFieldMeta;
import com.wan.framework.board.domain.BoardMeta;
import com.wan.framework.board.dto.BoardFieldMetaDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T09:53:32+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class BoardFieldMetaMapperImpl implements BoardFieldMetaMapper {

    @Override
    public BoardFieldMetaDTO toDto(BoardFieldMeta entity) {
        if ( entity == null ) {
            return null;
        }

        BoardFieldMetaDTO.BoardFieldMetaDTOBuilder boardFieldMetaDTO = BoardFieldMetaDTO.builder();

        boardFieldMetaDTO.boardMetaId( entityBoardMetaId( entity ) );
        boardFieldMetaDTO.id( entity.getId() );
        boardFieldMetaDTO.fieldName( entity.getFieldName() );
        boardFieldMetaDTO.fieldLabel( entity.getFieldLabel() );
        boardFieldMetaDTO.fieldType( entity.getFieldType() );
        boardFieldMetaDTO.displayOrder( entity.getDisplayOrder() );
        boardFieldMetaDTO.required( entity.getRequired() );
        boardFieldMetaDTO.showInList( entity.getShowInList() );
        boardFieldMetaDTO.showInDetail( entity.getShowInDetail() );
        boardFieldMetaDTO.showInForm( entity.getShowInForm() );
        boardFieldMetaDTO.searchable( entity.getSearchable() );
        boardFieldMetaDTO.fieldOptions( entity.getFieldOptions() );
        boardFieldMetaDTO.placeholder( entity.getPlaceholder() );
        boardFieldMetaDTO.defaultValue( entity.getDefaultValue() );
        boardFieldMetaDTO.dataStateCode( entity.getDataStateCode() );

        return boardFieldMetaDTO.build();
    }

    @Override
    public BoardFieldMeta toEntity(BoardFieldMetaDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BoardFieldMeta.BoardFieldMetaBuilder boardFieldMeta = BoardFieldMeta.builder();

        boardFieldMeta.boardMeta( boardFieldMetaDTOToBoardMeta( dto ) );
        boardFieldMeta.id( dto.getId() );
        boardFieldMeta.fieldName( dto.getFieldName() );
        boardFieldMeta.fieldLabel( dto.getFieldLabel() );
        boardFieldMeta.fieldType( dto.getFieldType() );
        boardFieldMeta.displayOrder( dto.getDisplayOrder() );
        boardFieldMeta.required( dto.getRequired() );
        boardFieldMeta.showInList( dto.getShowInList() );
        boardFieldMeta.showInDetail( dto.getShowInDetail() );
        boardFieldMeta.showInForm( dto.getShowInForm() );
        boardFieldMeta.searchable( dto.getSearchable() );
        boardFieldMeta.fieldOptions( dto.getFieldOptions() );
        boardFieldMeta.placeholder( dto.getPlaceholder() );
        boardFieldMeta.defaultValue( dto.getDefaultValue() );
        boardFieldMeta.dataStateCode( dto.getDataStateCode() );

        return boardFieldMeta.build();
    }

    @Override
    public void updateEntityFromDto(BoardFieldMetaDTO dto, BoardFieldMeta entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getFieldName() != null ) {
            entity.setFieldName( dto.getFieldName() );
        }
        if ( dto.getFieldLabel() != null ) {
            entity.setFieldLabel( dto.getFieldLabel() );
        }
        if ( dto.getFieldType() != null ) {
            entity.setFieldType( dto.getFieldType() );
        }
        if ( dto.getDisplayOrder() != null ) {
            entity.setDisplayOrder( dto.getDisplayOrder() );
        }
        if ( dto.getRequired() != null ) {
            entity.setRequired( dto.getRequired() );
        }
        if ( dto.getShowInList() != null ) {
            entity.setShowInList( dto.getShowInList() );
        }
        if ( dto.getShowInDetail() != null ) {
            entity.setShowInDetail( dto.getShowInDetail() );
        }
        if ( dto.getShowInForm() != null ) {
            entity.setShowInForm( dto.getShowInForm() );
        }
        if ( dto.getSearchable() != null ) {
            entity.setSearchable( dto.getSearchable() );
        }
        if ( dto.getFieldOptions() != null ) {
            entity.setFieldOptions( dto.getFieldOptions() );
        }
        if ( dto.getPlaceholder() != null ) {
            entity.setPlaceholder( dto.getPlaceholder() );
        }
        if ( dto.getDefaultValue() != null ) {
            entity.setDefaultValue( dto.getDefaultValue() );
        }
    }

    private Long entityBoardMetaId(BoardFieldMeta boardFieldMeta) {
        if ( boardFieldMeta == null ) {
            return null;
        }
        BoardMeta boardMeta = boardFieldMeta.getBoardMeta();
        if ( boardMeta == null ) {
            return null;
        }
        Long id = boardMeta.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected BoardMeta boardFieldMetaDTOToBoardMeta(BoardFieldMetaDTO boardFieldMetaDTO) {
        if ( boardFieldMetaDTO == null ) {
            return null;
        }

        BoardMeta.BoardMetaBuilder boardMeta = BoardMeta.builder();

        boardMeta.id( boardFieldMetaDTO.getBoardMetaId() );

        return boardMeta.build();
    }
}
