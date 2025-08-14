package com.wan.framework.menu.mapper;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.dto.MenuDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    MenuDTO toDTO(Menu menu);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Menu toEntity(MenuDTO menuDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(MenuDTO menuDTO, @MappingTarget Menu entity);
}
