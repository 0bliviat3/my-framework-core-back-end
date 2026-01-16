package com.wan.framework.proxy.mapper;

import com.wan.framework.proxy.domain.ApiEndpoint;
import com.wan.framework.proxy.dto.ApiEndpointDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-16T16:07:41+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class ApiEndpointMapperImpl implements ApiEndpointMapper {

    @Override
    public ApiEndpointDTO toDto(ApiEndpoint entity) {
        if ( entity == null ) {
            return null;
        }

        ApiEndpointDTO.ApiEndpointDTOBuilder apiEndpointDTO = ApiEndpointDTO.builder();

        apiEndpointDTO.id( entity.getId() );
        apiEndpointDTO.apiCode( entity.getApiCode() );
        apiEndpointDTO.apiName( entity.getApiName() );
        apiEndpointDTO.description( entity.getDescription() );
        apiEndpointDTO.targetUrl( entity.getTargetUrl() );
        apiEndpointDTO.httpMethod( entity.getHttpMethod() );
        apiEndpointDTO.requestHeaders( entity.getRequestHeaders() );
        apiEndpointDTO.requestBodyTemplate( entity.getRequestBodyTemplate() );
        apiEndpointDTO.timeoutSeconds( entity.getTimeoutSeconds() );
        apiEndpointDTO.retryCount( entity.getRetryCount() );
        apiEndpointDTO.retryIntervalMs( entity.getRetryIntervalMs() );
        apiEndpointDTO.isInternal( entity.getIsInternal() );
        apiEndpointDTO.isEnabled( entity.getIsEnabled() );
        apiEndpointDTO.dataState( entity.getDataState() );
        apiEndpointDTO.createdBy( entity.getCreatedBy() );
        apiEndpointDTO.createdAt( entity.getCreatedAt() );
        apiEndpointDTO.updatedBy( entity.getUpdatedBy() );
        apiEndpointDTO.updatedAt( entity.getUpdatedAt() );

        return apiEndpointDTO.build();
    }

    @Override
    public ApiEndpoint toEntity(ApiEndpointDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ApiEndpoint.ApiEndpointBuilder apiEndpoint = ApiEndpoint.builder();

        apiEndpoint.id( dto.getId() );
        apiEndpoint.apiCode( dto.getApiCode() );
        apiEndpoint.apiName( dto.getApiName() );
        apiEndpoint.description( dto.getDescription() );
        apiEndpoint.targetUrl( dto.getTargetUrl() );
        apiEndpoint.httpMethod( dto.getHttpMethod() );
        apiEndpoint.requestHeaders( dto.getRequestHeaders() );
        apiEndpoint.requestBodyTemplate( dto.getRequestBodyTemplate() );
        apiEndpoint.timeoutSeconds( dto.getTimeoutSeconds() );
        apiEndpoint.retryCount( dto.getRetryCount() );
        apiEndpoint.retryIntervalMs( dto.getRetryIntervalMs() );
        apiEndpoint.isInternal( dto.getIsInternal() );
        apiEndpoint.isEnabled( dto.getIsEnabled() );
        apiEndpoint.dataState( dto.getDataState() );

        return apiEndpoint.build();
    }

    @Override
    public void updateEntityFromDto(ApiEndpointDTO dto, ApiEndpoint entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getCreatedBy() != null ) {
            entity.setCreatedBy( dto.getCreatedBy() );
        }
        if ( dto.getCreatedAt() != null ) {
            entity.setCreatedAt( dto.getCreatedAt() );
        }
        if ( dto.getUpdatedBy() != null ) {
            entity.setUpdatedBy( dto.getUpdatedBy() );
        }
        if ( dto.getUpdatedAt() != null ) {
            entity.setUpdatedAt( dto.getUpdatedAt() );
        }
        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getApiCode() != null ) {
            entity.setApiCode( dto.getApiCode() );
        }
        if ( dto.getApiName() != null ) {
            entity.setApiName( dto.getApiName() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getTargetUrl() != null ) {
            entity.setTargetUrl( dto.getTargetUrl() );
        }
        if ( dto.getHttpMethod() != null ) {
            entity.setHttpMethod( dto.getHttpMethod() );
        }
        if ( dto.getRequestHeaders() != null ) {
            entity.setRequestHeaders( dto.getRequestHeaders() );
        }
        if ( dto.getRequestBodyTemplate() != null ) {
            entity.setRequestBodyTemplate( dto.getRequestBodyTemplate() );
        }
        if ( dto.getTimeoutSeconds() != null ) {
            entity.setTimeoutSeconds( dto.getTimeoutSeconds() );
        }
        if ( dto.getRetryCount() != null ) {
            entity.setRetryCount( dto.getRetryCount() );
        }
        if ( dto.getRetryIntervalMs() != null ) {
            entity.setRetryIntervalMs( dto.getRetryIntervalMs() );
        }
        if ( dto.getIsInternal() != null ) {
            entity.setIsInternal( dto.getIsInternal() );
        }
        if ( dto.getIsEnabled() != null ) {
            entity.setIsEnabled( dto.getIsEnabled() );
        }
        if ( dto.getDataState() != null ) {
            entity.setDataState( dto.getDataState() );
        }
    }
}
