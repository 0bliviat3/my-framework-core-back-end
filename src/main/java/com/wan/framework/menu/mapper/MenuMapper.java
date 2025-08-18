package com.wan.framework.menu.mapper;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.dto.MenuDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    @Mapping(target = "frontPath", source = "program.frontPath")
    @Mapping(target = "path", source = "program.path")
    @Mapping(target = "programId", source = "program.id")
    @Mapping(target = "parentId", source = "parent.id")
    MenuDTO toDTO(Menu menu);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "program", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Menu toEntity(MenuDTO menuDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "program", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(MenuDTO menuDTO, @MappingTarget Menu entity);
}
