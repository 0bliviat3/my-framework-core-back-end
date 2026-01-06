package com.wan.framework.batch.mapper;

import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.dto.BatchJobDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-06T14:06:42+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class BatchJobMapperImpl implements BatchJobMapper {

    @Override
    public BatchJobDTO toDto(BatchJob entity) {
        if ( entity == null ) {
            return null;
        }

        BatchJobDTO.BatchJobDTOBuilder batchJobDTO = BatchJobDTO.builder();

        batchJobDTO.id( entity.getId() );
        batchJobDTO.batchId( entity.getBatchId() );
        batchJobDTO.batchName( entity.getBatchName() );
        batchJobDTO.description( entity.getDescription() );
        batchJobDTO.scheduleType( entity.getScheduleType() );
        batchJobDTO.scheduleExpression( entity.getScheduleExpression() );
        batchJobDTO.proxyApiCode( entity.getProxyApiCode() );
        batchJobDTO.executionParameters( entity.getExecutionParameters() );
        batchJobDTO.enabled( entity.getEnabled() );
        batchJobDTO.maxRetryCount( entity.getMaxRetryCount() );
        batchJobDTO.retryIntervalSeconds( entity.getRetryIntervalSeconds() );
        batchJobDTO.timeoutSeconds( entity.getTimeoutSeconds() );
        batchJobDTO.allowConcurrent( entity.getAllowConcurrent() );
        batchJobDTO.lastExecutedAt( entity.getLastExecutedAt() );
        batchJobDTO.lastExecutionStatus( entity.getLastExecutionStatus() );
        batchJobDTO.nextExecutionAt( entity.getNextExecutionAt() );
        batchJobDTO.dataState( entity.getDataState() );
        batchJobDTO.createdBy( entity.getCreatedBy() );
        batchJobDTO.createdAt( entity.getCreatedAt() );
        batchJobDTO.updatedBy( entity.getUpdatedBy() );
        batchJobDTO.updatedAt( entity.getUpdatedAt() );

        return batchJobDTO.build();
    }

    @Override
    public BatchJob toEntity(BatchJobDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BatchJob.BatchJobBuilder batchJob = BatchJob.builder();

        batchJob.id( dto.getId() );
        batchJob.batchId( dto.getBatchId() );
        batchJob.batchName( dto.getBatchName() );
        batchJob.description( dto.getDescription() );
        batchJob.scheduleType( dto.getScheduleType() );
        batchJob.scheduleExpression( dto.getScheduleExpression() );
        batchJob.proxyApiCode( dto.getProxyApiCode() );
        batchJob.executionParameters( dto.getExecutionParameters() );
        batchJob.enabled( dto.getEnabled() );
        batchJob.maxRetryCount( dto.getMaxRetryCount() );
        batchJob.retryIntervalSeconds( dto.getRetryIntervalSeconds() );
        batchJob.timeoutSeconds( dto.getTimeoutSeconds() );
        batchJob.allowConcurrent( dto.getAllowConcurrent() );
        batchJob.lastExecutedAt( dto.getLastExecutedAt() );
        batchJob.lastExecutionStatus( dto.getLastExecutionStatus() );
        batchJob.nextExecutionAt( dto.getNextExecutionAt() );
        batchJob.dataState( dto.getDataState() );
        batchJob.createdBy( dto.getCreatedBy() );
        batchJob.createdAt( dto.getCreatedAt() );
        batchJob.updatedBy( dto.getUpdatedBy() );
        batchJob.updatedAt( dto.getUpdatedAt() );

        return batchJob.build();
    }

    @Override
    public void updateEntityFromDto(BatchJobDTO dto, BatchJob entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getId() != null ) {
            entity.setId( dto.getId() );
        }
        if ( dto.getBatchId() != null ) {
            entity.setBatchId( dto.getBatchId() );
        }
        if ( dto.getBatchName() != null ) {
            entity.setBatchName( dto.getBatchName() );
        }
        if ( dto.getDescription() != null ) {
            entity.setDescription( dto.getDescription() );
        }
        if ( dto.getScheduleType() != null ) {
            entity.setScheduleType( dto.getScheduleType() );
        }
        if ( dto.getScheduleExpression() != null ) {
            entity.setScheduleExpression( dto.getScheduleExpression() );
        }
        if ( dto.getProxyApiCode() != null ) {
            entity.setProxyApiCode( dto.getProxyApiCode() );
        }
        if ( dto.getExecutionParameters() != null ) {
            entity.setExecutionParameters( dto.getExecutionParameters() );
        }
        if ( dto.getEnabled() != null ) {
            entity.setEnabled( dto.getEnabled() );
        }
        if ( dto.getMaxRetryCount() != null ) {
            entity.setMaxRetryCount( dto.getMaxRetryCount() );
        }
        if ( dto.getRetryIntervalSeconds() != null ) {
            entity.setRetryIntervalSeconds( dto.getRetryIntervalSeconds() );
        }
        if ( dto.getTimeoutSeconds() != null ) {
            entity.setTimeoutSeconds( dto.getTimeoutSeconds() );
        }
        if ( dto.getAllowConcurrent() != null ) {
            entity.setAllowConcurrent( dto.getAllowConcurrent() );
        }
        if ( dto.getLastExecutedAt() != null ) {
            entity.setLastExecutedAt( dto.getLastExecutedAt() );
        }
        if ( dto.getLastExecutionStatus() != null ) {
            entity.setLastExecutionStatus( dto.getLastExecutionStatus() );
        }
        if ( dto.getNextExecutionAt() != null ) {
            entity.setNextExecutionAt( dto.getNextExecutionAt() );
        }
        if ( dto.getDataState() != null ) {
            entity.setDataState( dto.getDataState() );
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
    }
}
