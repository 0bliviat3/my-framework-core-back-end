package com.wan.framework.proxy.mapper;

import com.wan.framework.proxy.domain.ApiEndpoint;
import com.wan.framework.proxy.dto.ApiEndpointDTO;
import org.mapstruct.*;

/**
 * API 엔드포인트 Mapper
 */
@Mapper(componentModel = "spring")
public interface ApiEndpointMapper {

    /**
     * Entity -> DTO
     */
    ApiEndpointDTO toDto(ApiEndpoint entity);

    /**
     * DTO -> Entity
     */
    ApiEndpoint toEntity(ApiEndpointDTO dto);

    /**
     * DTO로 Entity 업데이트
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ApiEndpointDTO dto, @MappingTarget ApiEndpoint entity);
}
