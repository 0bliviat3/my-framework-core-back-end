package com.wan.framework.menu.mapper;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.dto.MenuDTO;
import com.wan.framework.program.domain.Program;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-02T14:17:43+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class MenuMapperImpl implements MenuMapper {

    @Override
    public MenuDTO toDTO(Menu menu) {
        if ( menu == null ) {
            return null;
        }

        MenuDTO.MenuDTOBuilder menuDTO = MenuDTO.builder();

        menuDTO.frontPath( menuProgramFrontPath( menu ) );
        menuDTO.path( menuProgramPath( menu ) );
        menuDTO.programId( menuProgramId( menu ) );
        menuDTO.programName( menuProgramName( menu ) );
        menuDTO.parentId( menuParentId( menu ) );
        menuDTO.id( menu.getId() );
        menuDTO.name( menu.getName() );
        menuDTO.type( menu.getType() );
        menuDTO.roles( menu.getRoles() );
        menuDTO.icon( menu.getIcon() );
        menuDTO.dataStateCode( menu.getDataStateCode() );
        menuDTO.ableState( menu.getAbleState() );

        return menuDTO.build();
    }

    @Override
    public Menu toEntity(MenuDTO menuDTO) {
        if ( menuDTO == null ) {
            return null;
        }

        Menu.MenuBuilder menu = Menu.builder();

        menu.id( menuDTO.getId() );
        menu.name( menuDTO.getName() );
        menu.type( menuDTO.getType() );
        menu.roles( menuDTO.getRoles() );
        menu.icon( menuDTO.getIcon() );
        menu.dataStateCode( menuDTO.getDataStateCode() );
        menu.ableState( menuDTO.getAbleState() );

        return menu.build();
    }

    @Override
    public void updateEntityFromDto(MenuDTO menuDTO, Menu entity) {
        if ( menuDTO == null ) {
            return;
        }

        if ( menuDTO.getId() != null ) {
            entity.setId( menuDTO.getId() );
        }
        if ( menuDTO.getName() != null ) {
            entity.setName( menuDTO.getName() );
        }
        if ( menuDTO.getType() != null ) {
            entity.setType( menuDTO.getType() );
        }
        if ( menuDTO.getRoles() != null ) {
            entity.setRoles( menuDTO.getRoles() );
        }
        if ( menuDTO.getIcon() != null ) {
            entity.setIcon( menuDTO.getIcon() );
        }
        if ( menuDTO.getDataStateCode() != null ) {
            entity.setDataStateCode( menuDTO.getDataStateCode() );
        }
        if ( menuDTO.getAbleState() != null ) {
            entity.setAbleState( menuDTO.getAbleState() );
        }
    }

    private String menuProgramFrontPath(Menu menu) {
        if ( menu == null ) {
            return null;
        }
        Program program = menu.getProgram();
        if ( program == null ) {
            return null;
        }
        String frontPath = program.getFrontPath();
        if ( frontPath == null ) {
            return null;
        }
        return frontPath;
    }

    private String menuProgramPath(Menu menu) {
        if ( menu == null ) {
            return null;
        }
        Program program = menu.getProgram();
        if ( program == null ) {
            return null;
        }
        String path = program.getPath();
        if ( path == null ) {
            return null;
        }
        return path;
    }

    private Long menuProgramId(Menu menu) {
        if ( menu == null ) {
            return null;
        }
        Program program = menu.getProgram();
        if ( program == null ) {
            return null;
        }
        Long id = program.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String menuProgramName(Menu menu) {
        if ( menu == null ) {
            return null;
        }
        Program program = menu.getProgram();
        if ( program == null ) {
            return null;
        }
        String name = program.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long menuParentId(Menu menu) {
        if ( menu == null ) {
            return null;
        }
        Menu parent = menu.getParent();
        if ( parent == null ) {
            return null;
        }
        Long id = parent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
