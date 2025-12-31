package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardPermission;
import com.wan.framework.board.dto.BoardPermissionDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BoardPermissionMapper {

    @Mapping(source = "boardMeta.id", target = "boardMetaId")
    BoardPermissionDTO toDto(BoardPermission entity);

    @Mapping(target = "boardMeta.id", source = "boardMetaId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BoardPermission toEntity(BoardPermissionDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boardMeta", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dataStateCode", ignore = true)
    void updateEntityFromDto(BoardPermissionDTO dto, @MappingTarget BoardPermission entity);
}
