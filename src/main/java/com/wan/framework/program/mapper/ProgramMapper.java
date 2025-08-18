package com.wan.framework.program.mapper;

import com.wan.framework.program.domain.Program;
import com.wan.framework.program.dto.ProgramDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProgramMapper {

    ProgramDTO toDTO(Program program);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Program toEntity(ProgramDTO programDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProgramDTO programDTO, @MappingTarget Program entity);
}
