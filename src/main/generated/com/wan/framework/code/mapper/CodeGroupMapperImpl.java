package com.wan.framework.code.mapper;

import com.wan.framework.code.domain.CodeGroup;
import com.wan.framework.code.dto.CodeGroupDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T09:53:32+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class CodeGroupMapperImpl implements CodeGroupMapper {

    @Override
    public CodeGroupDTO toDto(CodeGroup codeGroup) {
        if ( codeGroup == null ) {
            return null;
        }

        CodeGroupDTO.CodeGroupDTOBuilder codeGroupDTO = CodeGroupDTO.builder();

        codeGroupDTO.groupCode( codeGroup.getGroupCode() );
        codeGroupDTO.groupName( codeGroup.getGroupName() );
        codeGroupDTO.description( codeGroup.getDescription() );
        codeGroupDTO.enabled( codeGroup.getEnabled() );
        codeGroupDTO.sortOrder( codeGroup.getSortOrder() );
        codeGroupDTO.createTime( codeGroup.getCreateTime() );
        codeGroupDTO.modifiedTime( codeGroup.getModifiedTime() );
        codeGroupDTO.dataState( codeGroup.getDataState() );

        return codeGroupDTO.build();
    }

    @Override
    public CodeGroup toEntity(CodeGroupDTO dto) {
        if ( dto == null ) {
            return null;
        }

        CodeGroup.CodeGroupBuilder codeGroup = CodeGroup.builder();

        codeGroup.groupCode( dto.getGroupCode() );
        codeGroup.groupName( dto.getGroupName() );
        codeGroup.description( dto.getDescription() );
        codeGroup.enabled( dto.getEnabled() );
        codeGroup.sortOrder( dto.getSortOrder() );

        return codeGroup.build();
    }
}
