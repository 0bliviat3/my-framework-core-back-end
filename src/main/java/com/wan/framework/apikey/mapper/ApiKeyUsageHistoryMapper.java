package com.wan.framework.apikey.mapper;

import com.wan.framework.apikey.domain.ApiKeyUsageHistory;
import com.wan.framework.apikey.dto.ApiKeyUsageHistoryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiKeyUsageHistoryMapper {

    @Mapping(source = "apiKey.id", target = "apiKeyId")
    @Mapping(source = "apiKey.apiKeyPrefix", target = "apiKeyPrefix")
    ApiKeyUsageHistoryDTO toDto(ApiKeyUsageHistory entity);

    List<ApiKeyUsageHistoryDTO> toDtoList(List<ApiKeyUsageHistory> entities);
}
