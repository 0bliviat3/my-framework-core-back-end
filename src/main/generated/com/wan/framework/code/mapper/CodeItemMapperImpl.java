package com.wan.framework.code.mapper;

import com.wan.framework.code.domain.CodeItem;
import com.wan.framework.code.dto.CodeItemDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T09:53:32+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class CodeItemMapperImpl implements CodeItemMapper {

    @Override
    public CodeItemDTO toDto(CodeItem codeItem) {
        if ( codeItem == null ) {
            return null;
        }

        CodeItemDTO.CodeItemDTOBuilder codeItemDTO = CodeItemDTO.builder();

        codeItemDTO.id( codeItem.getId() );
        codeItemDTO.groupCode( codeItem.getGroupCode() );
        codeItemDTO.codeValue( codeItem.getCodeValue() );
        codeItemDTO.codeName( codeItem.getCodeName() );
        codeItemDTO.description( codeItem.getDescription() );
        codeItemDTO.enabled( codeItem.getEnabled() );
        codeItemDTO.sortOrder( codeItem.getSortOrder() );
        codeItemDTO.attribute1( codeItem.getAttribute1() );
        codeItemDTO.attribute2( codeItem.getAttribute2() );
        codeItemDTO.attribute3( codeItem.getAttribute3() );
        codeItemDTO.createTime( codeItem.getCreateTime() );
        codeItemDTO.modifiedTime( codeItem.getModifiedTime() );
        codeItemDTO.dataState( codeItem.getDataState() );

        return codeItemDTO.build();
    }

    @Override
    public CodeItem toEntity(CodeItemDTO dto) {
        if ( dto == null ) {
            return null;
        }

        CodeItem.CodeItemBuilder codeItem = CodeItem.builder();

        codeItem.id( dto.getId() );
        codeItem.groupCode( dto.getGroupCode() );
        codeItem.codeValue( dto.getCodeValue() );
        codeItem.codeName( dto.getCodeName() );
        codeItem.description( dto.getDescription() );
        codeItem.enabled( dto.getEnabled() );
        codeItem.sortOrder( dto.getSortOrder() );
        codeItem.attribute1( dto.getAttribute1() );
        codeItem.attribute2( dto.getAttribute2() );
        codeItem.attribute3( dto.getAttribute3() );

        return codeItem.build();
    }
}
