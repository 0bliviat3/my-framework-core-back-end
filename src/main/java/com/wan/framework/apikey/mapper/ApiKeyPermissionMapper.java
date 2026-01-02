package com.wan.framework.apikey.mapper;

import com.wan.framework.apikey.domain.ApiKeyPermission;
import com.wan.framework.apikey.dto.ApiKeyPermissionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiKeyPermissionMapper {

    @Mapping(source = "apiKey.id", target = "apiKeyId")
    ApiKeyPermissionDTO toDto(ApiKeyPermission entity);

    List<ApiKeyPermissionDTO> toDtoList(List<ApiKeyPermission> entities);
}
