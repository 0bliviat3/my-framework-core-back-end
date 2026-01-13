package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardComment;
import com.wan.framework.board.dto.BoardCommentDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardCommentMapper {

    @Mapping(source = "boardData.id", target = "boardDataId")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "childCount", expression = "java(entity.getChildCount())")
    BoardCommentDTO toDto(BoardComment entity);

    @Mapping(target = "boardData", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "isModified", defaultValue = "false")
    @Mapping(target = "dataStateCode", defaultValue = "I")
    BoardComment toEntity(BoardCommentDTO dto);

    List<BoardCommentDTO> toDtoList(List<BoardComment> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boardData", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "isModified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "dataStateCode", ignore = true)
    void updateEntityFromDto(BoardCommentDTO dto, @MappingTarget BoardComment entity);
}
