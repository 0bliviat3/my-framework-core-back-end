package com.wan.framework.apikey.mapper;

import com.wan.framework.apikey.domain.ApiKey;
import com.wan.framework.apikey.domain.ApiKeyPermission;
import com.wan.framework.apikey.dto.ApiKeyPermissionDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T09:53:32+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class ApiKeyPermissionMapperImpl implements ApiKeyPermissionMapper {

    @Override
    public ApiKeyPermissionDTO toDto(ApiKeyPermission entity) {
        if ( entity == null ) {
            return null;
        }

        ApiKeyPermissionDTO.ApiKeyPermissionDTOBuilder apiKeyPermissionDTO = ApiKeyPermissionDTO.builder();

        apiKeyPermissionDTO.apiKeyId( entityApiKeyId( entity ) );
        apiKeyPermissionDTO.id( entity.getId() );
        apiKeyPermissionDTO.permission( entity.getPermission() );
        apiKeyPermissionDTO.createdBy( entity.getCreatedBy() );
        apiKeyPermissionDTO.createdAt( entity.getCreatedAt() );

        return apiKeyPermissionDTO.build();
    }

    @Override
    public List<ApiKeyPermissionDTO> toDtoList(List<ApiKeyPermission> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ApiKeyPermissionDTO> list = new ArrayList<ApiKeyPermissionDTO>( entities.size() );
        for ( ApiKeyPermission apiKeyPermission : entities ) {
            list.add( toDto( apiKeyPermission ) );
        }

        return list;
    }

    private Long entityApiKeyId(ApiKeyPermission apiKeyPermission) {
        if ( apiKeyPermission == null ) {
            return null;
        }
        ApiKey apiKey = apiKeyPermission.getApiKey();
        if ( apiKey == null ) {
            return null;
        }
        Long id = apiKey.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
