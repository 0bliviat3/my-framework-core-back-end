package com.wan.framework.batch.mapper;

import com.wan.framework.batch.domain.BatchExecution;
import com.wan.framework.batch.dto.BatchExecutionDTO;
import org.mapstruct.Mapper;

/**
 * 배치 실행 이력 Mapper
 */
@Mapper(componentModel = "spring")
public interface BatchExecutionMapper {

    /**
     * Entity -> DTO
     */
    BatchExecutionDTO toDto(BatchExecution entity);

    /**
     * DTO -> Entity
     */
    BatchExecution toEntity(BatchExecutionDTO dto);
}
