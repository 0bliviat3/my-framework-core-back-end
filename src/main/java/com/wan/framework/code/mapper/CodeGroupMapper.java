package com.wan.framework.code.mapper;

import com.wan.framework.code.domain.CodeGroup;
import com.wan.framework.code.dto.CodeGroupDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 공통코드 그룹 Mapper
 */
@Mapper(componentModel = "spring")
public interface CodeGroupMapper {

    CodeGroupDTO toDto(CodeGroup codeGroup);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "modifiedTime", ignore = true)
    @Mapping(target = "dataState", ignore = true)
    CodeGroup toEntity(CodeGroupDTO dto);
}
