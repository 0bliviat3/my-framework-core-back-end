package com.wan.framework.apikey.mapper;

import com.wan.framework.apikey.domain.ApiKey;
import com.wan.framework.apikey.dto.ApiKeyDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T13:23:52+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class ApiKeyMapperImpl implements ApiKeyMapper {

    @Override
    public ApiKeyDTO toDto(ApiKey entity) {
        if ( entity == null ) {
            return null;
        }

        ApiKeyDTO.ApiKeyDTOBuilder apiKeyDTO = ApiKeyDTO.builder();

        apiKeyDTO.id( entity.getId() );
        apiKeyDTO.apiKeyPrefix( entity.getApiKeyPrefix() );
        apiKeyDTO.description( entity.getDescription() );
        apiKeyDTO.createdBy( entity.getCreatedBy() );
        apiKeyDTO.updatedBy( entity.getUpdatedBy() );
        apiKeyDTO.ableState( entity.getAbleState() );
        apiKeyDTO.expiredAt( entity.getExpiredAt() );
        apiKeyDTO.lastUsedAt( entity.getLastUsedAt() );
        apiKeyDTO.usageCount( entity.getUsageCount() );
        apiKeyDTO.dataStateCode( entity.getDataStateCode() );
        apiKeyDTO.createdAt( entity.getCreatedAt() );
        apiKeyDTO.updatedAt( entity.getUpdatedAt() );
        apiKeyDTO.deletedAt( entity.getDeletedAt() );

        return apiKeyDTO.build();
    }

    @Override
    public ApiKey toEntity(ApiKeyDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ApiKey.ApiKeyBuilder apiKey = ApiKey.builder();

        apiKey.id( dto.getId() );
        apiKey.apiKeyPrefix( dto.getApiKeyPrefix() );
        apiKey.description( dto.getDescription() );
        apiKey.createdBy( dto.getCreatedBy() );
        apiKey.updatedBy( dto.getUpdatedBy() );
        apiKey.ableState( dto.getAbleState() );
        apiKey.expiredAt( dto.getExpiredAt() );

        return apiKey.build();
    }

    @Override
    public void updateEntityFromDto(ApiKeyDTO dto, ApiKey entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getUpdatedBy() != null ) {
            entity.setUpdatedBy( dto.getUpdatedBy() );
        }
        if ( dto.getAbleState() != null ) {
            entity.setAbleState( dto.getAbleState() );
        }
        if ( dto.getExpiredAt() != null ) {
            entity.setExpiredAt( dto.getExpiredAt() );
        }
    }
}
