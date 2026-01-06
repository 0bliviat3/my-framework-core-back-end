package com.wan.framework.code.mapper;

import com.wan.framework.code.domain.CodeItem;
import com.wan.framework.code.dto.CodeItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 공통코드 항목 Mapper
 */
@Mapper(componentModel = "spring")
public interface CodeItemMapper {

    CodeItemDTO toDto(CodeItem codeItem);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "modifiedTime", ignore = true)
    @Mapping(target = "dataState", ignore = true)
    CodeItem toEntity(CodeItemDTO dto);
}
