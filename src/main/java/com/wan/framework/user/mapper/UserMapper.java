package com.wan.framework.user.mapper;

import com.wan.framework.user.domain.User;
import com.wan.framework.user.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roleCodes", expression = "java(user.extractRoleCodes())")
    UserDTO toDto(User user);

    @Mapping(target = "modifiedTime", ignore = true)
    @Mapping(target = "roleEntities", ignore = true)  // Service에서 처리
    User toEntity(UserDTO dto);
}
