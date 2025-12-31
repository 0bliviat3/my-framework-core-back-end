package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardFieldMeta;
import com.wan.framework.board.dto.BoardFieldMetaDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BoardFieldMetaMapper {

    @Mapping(source = "boardMeta.id", target = "boardMetaId")
    BoardFieldMetaDTO toDto(BoardFieldMeta entity);

    @Mapping(target = "boardMeta.id", source = "boardMetaId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BoardFieldMeta toEntity(BoardFieldMetaDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boardMeta", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dataStateCode", ignore = true)
    void updateEntityFromDto(BoardFieldMetaDTO dto, @MappingTarget BoardFieldMeta entity);
}
