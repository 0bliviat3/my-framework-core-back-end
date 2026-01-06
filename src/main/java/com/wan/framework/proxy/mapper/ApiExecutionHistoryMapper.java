package com.wan.framework.proxy.mapper;

import com.wan.framework.proxy.domain.ApiExecutionHistory;
import com.wan.framework.proxy.dto.ApiExecutionHistoryDTO;
import org.mapstruct.Mapper;

/**
 * API 실행 이력 Mapper
 */
@Mapper(componentModel = "spring")
public interface ApiExecutionHistoryMapper {

    /**
     * Entity -> DTO
     */
    ApiExecutionHistoryDTO toDto(ApiExecutionHistory entity);

    /**
     * DTO -> Entity
     */
    ApiExecutionHistory toEntity(ApiExecutionHistoryDTO dto);
}
