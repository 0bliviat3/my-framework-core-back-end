package com.wan.framework.proxy.mapper;

import com.wan.framework.proxy.domain.ApiExecutionHistory;
import com.wan.framework.proxy.dto.ApiExecutionHistoryDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T13:23:51+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class ApiExecutionHistoryMapperImpl implements ApiExecutionHistoryMapper {

    @Override
    public ApiExecutionHistoryDTO toDto(ApiExecutionHistory entity) {
        if ( entity == null ) {
            return null;
        }

        ApiExecutionHistoryDTO.ApiExecutionHistoryDTOBuilder apiExecutionHistoryDTO = ApiExecutionHistoryDTO.builder();

        apiExecutionHistoryDTO.id( entity.getId() );
        apiExecutionHistoryDTO.apiEndpointId( entity.getApiEndpointId() );
        apiExecutionHistoryDTO.apiCode( entity.getApiCode() );
        apiExecutionHistoryDTO.executedUrl( entity.getExecutedUrl() );
        apiExecutionHistoryDTO.httpMethod( entity.getHttpMethod() );
        apiExecutionHistoryDTO.requestHeaders( entity.getRequestHeaders() );
        apiExecutionHistoryDTO.requestBody( entity.getRequestBody() );
        apiExecutionHistoryDTO.responseStatusCode( entity.getResponseStatusCode() );
        apiExecutionHistoryDTO.responseHeaders( entity.getResponseHeaders() );
        apiExecutionHistoryDTO.responseBody( entity.getResponseBody() );
        apiExecutionHistoryDTO.executionTimeMs( entity.getExecutionTimeMs() );
        apiExecutionHistoryDTO.isSuccess( entity.getIsSuccess() );
        apiExecutionHistoryDTO.errorMessage( entity.getErrorMessage() );
        apiExecutionHistoryDTO.retryAttempt( entity.getRetryAttempt() );
        apiExecutionHistoryDTO.executionTrigger( entity.getExecutionTrigger() );
        apiExecutionHistoryDTO.executedBy( entity.getExecutedBy() );
        apiExecutionHistoryDTO.executedAt( entity.getExecutedAt() );

        return apiExecutionHistoryDTO.build();
    }

    @Override
    public ApiExecutionHistory toEntity(ApiExecutionHistoryDTO dto) {
        if ( dto == null ) {
            return null;
        }

        ApiExecutionHistory.ApiExecutionHistoryBuilder apiExecutionHistory = ApiExecutionHistory.builder();

        apiExecutionHistory.id( dto.getId() );
        apiExecutionHistory.apiEndpointId( dto.getApiEndpointId() );
        apiExecutionHistory.apiCode( dto.getApiCode() );
        apiExecutionHistory.executedUrl( dto.getExecutedUrl() );
        apiExecutionHistory.httpMethod( dto.getHttpMethod() );
        apiExecutionHistory.requestHeaders( dto.getRequestHeaders() );
        apiExecutionHistory.requestBody( dto.getRequestBody() );
        apiExecutionHistory.responseStatusCode( dto.getResponseStatusCode() );
        apiExecutionHistory.responseHeaders( dto.getResponseHeaders() );
        apiExecutionHistory.responseBody( dto.getResponseBody() );
        apiExecutionHistory.executionTimeMs( dto.getExecutionTimeMs() );
        apiExecutionHistory.isSuccess( dto.getIsSuccess() );
        apiExecutionHistory.errorMessage( dto.getErrorMessage() );
        apiExecutionHistory.retryAttempt( dto.getRetryAttempt() );
        apiExecutionHistory.executionTrigger( dto.getExecutionTrigger() );
        apiExecutionHistory.executedBy( dto.getExecutedBy() );
        apiExecutionHistory.executedAt( dto.getExecutedAt() );

        return apiExecutionHistory.build();
    }
}
