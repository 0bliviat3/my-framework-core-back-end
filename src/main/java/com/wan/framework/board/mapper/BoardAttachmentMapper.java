package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardAttachment;
import com.wan.framework.board.dto.BoardAttachmentDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardAttachmentMapper {

    @Mapping(source = "boardData.id", target = "boardDataId")
    @Mapping(target = "formattedFileSize", expression = "java(entity.getFormattedFileSize())")
    @Mapping(target = "fileExtension", expression = "java(entity.getFileExtension())")
    BoardAttachmentDTO toDto(BoardAttachment entity);

    @Mapping(target = "boardData.id", source = "boardDataId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BoardAttachment toEntity(BoardAttachmentDTO dto);

    List<BoardAttachmentDTO> toDtoList(List<BoardAttachment> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boardData", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "dataStateCode", ignore = true)
    void updateEntityFromDto(BoardAttachmentDTO dto, @MappingTarget BoardAttachment entity);
}
