package com.wan.framework.program.mapper;

import com.wan.framework.program.domain.Program;
import com.wan.framework.program.dto.ProgramDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-15T18:20:50+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class ProgramMapperImpl implements ProgramMapper {

    @Override
    public ProgramDTO toDTO(Program program) {
        if ( program == null ) {
            return null;
        }

        ProgramDTO.ProgramDTOBuilder programDTO = ProgramDTO.builder();

        programDTO.id( program.getId() );
        programDTO.name( program.getName() );
        programDTO.frontPath( program.getFrontPath() );
        programDTO.path( program.getPath() );
        programDTO.apiKey( program.getApiKey() );
        programDTO.description( program.getDescription() );
        programDTO.dataStateCode( program.getDataStateCode() );
        programDTO.ableState( program.getAbleState() );

        return programDTO.build();
    }

    @Override
    public Program toEntity(ProgramDTO programDTO) {
        if ( programDTO == null ) {
            return null;
        }

        Program.ProgramBuilder program = Program.builder();

        program.id( programDTO.getId() );
        program.name( programDTO.getName() );
        program.frontPath( programDTO.getFrontPath() );
        program.path( programDTO.getPath() );
        program.apiKey( programDTO.getApiKey() );
        program.description( programDTO.getDescription() );
        program.dataStateCode( programDTO.getDataStateCode() );
        program.ableState( programDTO.getAbleState() );

        return program.build();
    }

    @Override
    public void updateEntityFromDto(ProgramDTO programDTO, Program entity) {
        if ( programDTO == null ) {
            return;
        }

        if ( programDTO.getId() != null ) {
            entity.setId( programDTO.getId() );
        }
        if ( programDTO.getName() != null ) {
            entity.setName( programDTO.getName() );
        }
        if ( programDTO.getFrontPath() != null ) {
            entity.setFrontPath( programDTO.getFrontPath() );
        }
        if ( programDTO.getPath() != null ) {
            entity.setPath( programDTO.getPath() );
        }
        if ( programDTO.getApiKey() != null ) {
            entity.setApiKey( programDTO.getApiKey() );
        }
        if ( programDTO.getDescription() != null ) {
            entity.setDescription( programDTO.getDescription() );
        }
        if ( programDTO.getDataStateCode() != null ) {
            entity.setDataStateCode( programDTO.getDataStateCode() );
        }
        if ( programDTO.getAbleState() != null ) {
            entity.setAbleState( programDTO.getAbleState() );
        }
    }
}
