package com.wan.framework.apikey.mapper;

import com.wan.framework.apikey.domain.ApiKey;
import com.wan.framework.apikey.dto.ApiKeyDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ApiKeyMapper {

    @Mapping(target = "rawApiKey", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    ApiKeyDTO toDto(ApiKey entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "dataStateCode", ignore = true)
    ApiKey toEntity(ApiKeyDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "apiKey", ignore = true)
    @Mapping(target = "apiKeyPrefix", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "dataStateCode", ignore = true)
    void updateEntityFromDto(ApiKeyDTO dto, @MappingTarget ApiKey entity);
}
