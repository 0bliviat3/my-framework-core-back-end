package com.wan.framework.batch.mapper;

import com.wan.framework.batch.domain.BatchJob;
import com.wan.framework.batch.dto.BatchJobDTO;
import org.mapstruct.*;

/**
 * 배치 작업 Mapper
 */
@Mapper(componentModel = "spring")
public interface BatchJobMapper {

    /**
     * Entity -> DTO
     */
    BatchJobDTO toDto(BatchJob entity);

    /**
     * DTO -> Entity
     */
    BatchJob toEntity(BatchJobDTO dto);

    /**
     * DTO로 Entity 업데이트
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(BatchJobDTO dto, @MappingTarget BatchJob entity);
}
