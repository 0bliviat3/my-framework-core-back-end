package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardData;
import com.wan.framework.board.dto.BoardDataDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BoardDataMapper {

    @Mapping(source = "boardMeta.id", target = "boardMetaId")
    @Mapping(source = "boardMeta.title", target = "boardMetaTitle")
    BoardDataDTO toDto(BoardData entity);

    @Mapping(target = "boardMeta.id", source = "boardMetaId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "status", defaultValue = "PUBLISHED")
    @Mapping(target = "dataStateCode", defaultValue = "I")
    @Mapping(target = "viewCount", defaultValue = "0L")
    @Mapping(target = "commentCount", defaultValue = "0")
    @BeanMapping(builder = @Builder(disableBuilder = true))
    BoardData toEntity(BoardDataDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boardMeta", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "dataStateCode", ignore = true)
    void updateEntityFromDto(BoardDataDTO dto, @MappingTarget BoardData entity);
}
