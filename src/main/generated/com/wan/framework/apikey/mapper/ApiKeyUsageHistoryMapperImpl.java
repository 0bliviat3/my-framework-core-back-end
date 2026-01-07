package com.wan.framework.apikey.mapper;

import com.wan.framework.apikey.domain.ApiKey;
import com.wan.framework.apikey.domain.ApiKeyUsageHistory;
import com.wan.framework.apikey.dto.ApiKeyUsageHistoryDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T13:23:51+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class ApiKeyUsageHistoryMapperImpl implements ApiKeyUsageHistoryMapper {

    @Override
    public ApiKeyUsageHistoryDTO toDto(ApiKeyUsageHistory entity) {
        if ( entity == null ) {
            return null;
        }

        ApiKeyUsageHistoryDTO.ApiKeyUsageHistoryDTOBuilder apiKeyUsageHistoryDTO = ApiKeyUsageHistoryDTO.builder();

        apiKeyUsageHistoryDTO.apiKeyId( entityApiKeyId( entity ) );
        apiKeyUsageHistoryDTO.apiKeyPrefix( entityApiKeyApiKeyPrefix( entity ) );
        apiKeyUsageHistoryDTO.id( entity.getId() );
        apiKeyUsageHistoryDTO.requestUri( entity.getRequestUri() );
        apiKeyUsageHistoryDTO.requestMethod( entity.getRequestMethod() );
        apiKeyUsageHistoryDTO.ipAddress( entity.getIpAddress() );
        apiKeyUsageHistoryDTO.userAgent( entity.getUserAgent() );
        apiKeyUsageHistoryDTO.responseStatus( entity.getResponseStatus() );
        apiKeyUsageHistoryDTO.isSuccess( entity.getIsSuccess() );
        apiKeyUsageHistoryDTO.errorMessage( entity.getErrorMessage() );
        apiKeyUsageHistoryDTO.usedAt( entity.getUsedAt() );

        return apiKeyUsageHistoryDTO.build();
    }

    @Override
    public List<ApiKeyUsageHistoryDTO> toDtoList(List<ApiKeyUsageHistory> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ApiKeyUsageHistoryDTO> list = new ArrayList<ApiKeyUsageHistoryDTO>( entities.size() );
        for ( ApiKeyUsageHistory apiKeyUsageHistory : entities ) {
            list.add( toDto( apiKeyUsageHistory ) );
        }

        return list;
    }

    private Long entityApiKeyId(ApiKeyUsageHistory apiKeyUsageHistory) {
        if ( apiKeyUsageHistory == null ) {
            return null;
        }
        ApiKey apiKey = apiKeyUsageHistory.getApiKey();
        if ( apiKey == null ) {
            return null;
        }
        Long id = apiKey.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entityApiKeyApiKeyPrefix(ApiKeyUsageHistory apiKeyUsageHistory) {
        if ( apiKeyUsageHistory == null ) {
            return null;
        }
        ApiKey apiKey = apiKeyUsageHistory.getApiKey();
        if ( apiKey == null ) {
            return null;
        }
        String apiKeyPrefix = apiKey.getApiKeyPrefix();
        if ( apiKeyPrefix == null ) {
            return null;
        }
        return apiKeyPrefix;
    }
}
