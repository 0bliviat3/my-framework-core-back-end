package com.wan.framework.history.mapper;

import com.wan.framework.history.domain.ErrorHistory;
import com.wan.framework.history.dto.ErrorHistoryDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-15T18:20:49+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class ErrorHistoryMapperImpl implements ErrorHistoryMapper {

    @Override
    public ErrorHistoryDTO toDTO(ErrorHistory errorHistory) {
        if ( errorHistory == null ) {
            return null;
        }

        ErrorHistoryDTO.ErrorHistoryDTOBuilder errorHistoryDTO = ErrorHistoryDTO.builder();

        errorHistoryDTO.errorId( errorHistory.getErrorId() );
        errorHistoryDTO.requestURL( errorHistory.getRequestURL() );
        errorHistoryDTO.requestParam( errorHistory.getRequestParam() );
        errorHistoryDTO.errorMessage( errorHistory.getErrorMessage() );
        errorHistoryDTO.eventTime( errorHistory.getEventTime() );
        errorHistoryDTO.stackTrace( errorHistory.getStackTrace() );

        return errorHistoryDTO.build();
    }

    @Override
    public ErrorHistory toEntity(ErrorHistoryDTO errorHistoryDTO) {
        if ( errorHistoryDTO == null ) {
            return null;
        }

        ErrorHistory.ErrorHistoryBuilder errorHistory = ErrorHistory.builder();

        errorHistory.errorId( errorHistoryDTO.getErrorId() );
        errorHistory.requestURL( errorHistoryDTO.getRequestURL() );
        errorHistory.requestParam( errorHistoryDTO.getRequestParam() );
        errorHistory.errorMessage( errorHistoryDTO.getErrorMessage() );
        errorHistory.eventTime( errorHistoryDTO.getEventTime() );
        errorHistory.stackTrace( errorHistoryDTO.getStackTrace() );

        return errorHistory.build();
    }
}
