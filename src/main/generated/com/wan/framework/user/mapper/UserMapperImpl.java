package com.wan.framework.user.mapper;

import com.wan.framework.user.constant.RoleType;
import com.wan.framework.user.domain.User;
import com.wan.framework.user.dto.UserDTO;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-16T09:40:27+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.userId( user.getUserId() );
        userDTO.password( user.getPassword() );
        userDTO.name( user.getName() );
        userDTO.dataCode( user.getDataCode() );
        userDTO.passwordSalt( user.getPasswordSalt() );
        Set<RoleType> set = user.getRoles();
        if ( set != null ) {
            userDTO.roles( new LinkedHashSet<RoleType>( set ) );
        }
        userDTO.createTime( user.getCreateTime() );
        userDTO.modifiedTime( user.getModifiedTime() );

        userDTO.roleCodes( user.extractRoleCodes() );

        return userDTO.build();
    }

    @Override
    public User toEntity(UserDTO dto) {
        if ( dto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.userId( dto.getUserId() );
        user.password( dto.getPassword() );
        user.name( dto.getName() );
        user.createTime( dto.getCreateTime() );
        user.dataCode( dto.getDataCode() );
        user.passwordSalt( dto.getPasswordSalt() );
        Set<RoleType> set = dto.getRoles();
        if ( set != null ) {
            user.roles( new LinkedHashSet<RoleType>( set ) );
        }

        return user.build();
    }
}
