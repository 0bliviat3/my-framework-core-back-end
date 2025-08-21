package com.wan.framework.history.mapper;

import com.wan.framework.history.domain.ErrorHistory;
import com.wan.framework.history.dto.ErrorHistoryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ErrorHistoryMapper {

    ErrorHistoryDTO toDTO(ErrorHistory errorHistory);

    ErrorHistory toEntity(ErrorHistoryDTO errorHistoryDTO);

}
