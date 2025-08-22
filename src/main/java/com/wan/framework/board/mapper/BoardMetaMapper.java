package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardMeta;
import com.wan.framework.board.dto.BoardMetaDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BoardMetaMapper {

    BoardMetaDTO toDTO(BoardMeta boardMeta);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BoardMeta toEntity(BoardMetaDTO boardMetaDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(BoardMetaDTO boardMetaDTO, @MappingTarget BoardMeta entity);

}
