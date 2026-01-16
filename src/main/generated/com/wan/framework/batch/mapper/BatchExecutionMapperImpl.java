package com.wan.framework.batch.mapper;

import com.wan.framework.batch.domain.BatchExecution;
import com.wan.framework.batch.dto.BatchExecutionDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-15T18:20:50+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class BatchExecutionMapperImpl implements BatchExecutionMapper {

    @Override
    public BatchExecutionDTO toDto(BatchExecution entity) {
        if ( entity == null ) {
            return null;
        }

        BatchExecutionDTO.BatchExecutionDTOBuilder batchExecutionDTO = BatchExecutionDTO.builder();

        batchExecutionDTO.id( entity.getId() );
        batchExecutionDTO.executionId( entity.getExecutionId() );
        batchExecutionDTO.batchJobId( entity.getBatchJobId() );
        batchExecutionDTO.batchId( entity.getBatchId() );
        batchExecutionDTO.batchName( entity.getBatchName() );
        batchExecutionDTO.status( entity.getStatus() );
        batchExecutionDTO.triggerType( entity.getTriggerType() );
        batchExecutionDTO.proxyExecutionHistoryId( entity.getProxyExecutionHistoryId() );
        batchExecutionDTO.executionParameters( entity.getExecutionParameters() );
        batchExecutionDTO.startTime( entity.getStartTime() );
        batchExecutionDTO.endTime( entity.getEndTime() );
        batchExecutionDTO.executionTimeMs( entity.getExecutionTimeMs() );
        batchExecutionDTO.errorMessage( entity.getErrorMessage() );
        batchExecutionDTO.stackTrace( entity.getStackTrace() );
        batchExecutionDTO.retryCount( entity.getRetryCount() );
        batchExecutionDTO.originalExecutionId( entity.getOriginalExecutionId() );
        batchExecutionDTO.serverInfo( entity.getServerInfo() );
        batchExecutionDTO.executedBy( entity.getExecutedBy() );
        batchExecutionDTO.createdAt( entity.getCreatedAt() );

        return batchExecutionDTO.build();
    }

    @Override
    public BatchExecution toEntity(BatchExecutionDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BatchExecution.BatchExecutionBuilder batchExecution = BatchExecution.builder();

        batchExecution.id( dto.getId() );
        batchExecution.executionId( dto.getExecutionId() );
        batchExecution.batchJobId( dto.getBatchJobId() );
        batchExecution.batchId( dto.getBatchId() );
        batchExecution.batchName( dto.getBatchName() );
        batchExecution.status( dto.getStatus() );
        batchExecution.triggerType( dto.getTriggerType() );
        batchExecution.proxyExecutionHistoryId( dto.getProxyExecutionHistoryId() );
        batchExecution.executionParameters( dto.getExecutionParameters() );
        batchExecution.startTime( dto.getStartTime() );
        batchExecution.endTime( dto.getEndTime() );
        batchExecution.executionTimeMs( dto.getExecutionTimeMs() );
        batchExecution.errorMessage( dto.getErrorMessage() );
        batchExecution.stackTrace( dto.getStackTrace() );
        batchExecution.retryCount( dto.getRetryCount() );
        batchExecution.originalExecutionId( dto.getOriginalExecutionId() );
        batchExecution.serverInfo( dto.getServerInfo() );
        batchExecution.executedBy( dto.getExecutedBy() );
        batchExecution.createdAt( dto.getCreatedAt() );

        return batchExecution.build();
    }
}
