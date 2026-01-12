package com.wan.framework.user.mapper;

import com.wan.framework.user.domain.User;
import com.wan.framework.user.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);


    @Mapping(target = "modifiedTime", ignore = true)
    User toEntity(UserDTO dto);
}
