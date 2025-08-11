package com.wan.framework.program.mapper;

import com.wan.framework.program.domain.Program;
import com.wan.framework.program.dto.ProgramDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProgramMapper {

    ProgramDTO toDTO(Program program);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Program toEntity(ProgramDTO programDTO);

    void updateEntityFromDto(ProgramDTO programDTO, @MappingTarget Program entity);
}
