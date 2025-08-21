package com.wan.framework.history.service;

import com.wan.framework.history.domain.ErrorHistory;
import com.wan.framework.history.dto.ErrorHistoryDTO;
import com.wan.framework.history.mapper.ErrorHistoryMapper;
import com.wan.framework.history.repository.ErrorHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
@RequiredArgsConstructor
public class ErrorHistoryService {

    private final ErrorHistoryRepository errorHistoryRepository;
    private final ErrorHistoryMapper errorHistoryMapper;

    private String getStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        if (stackTrace.length() > 5000) {
            return stackTrace.substring(0, 5000);
        }
        return stackTrace;
    }

    @Transactional
    public ErrorHistoryDTO saveException(Exception exception, HttpServletRequest request) {
        ErrorHistory errorHistory = ErrorHistory.builder()
                .requestURL(request.getRequestURI())
                .requestParam(request.getQueryString())
                .errorMessage(exception.getMessage())
                .stackTrace(getStackTrace(exception))
                .build();

        errorHistory = errorHistoryRepository.save(errorHistory);
        return errorHistoryMapper.toDTO(errorHistory);
    }

    @Transactional(readOnly = true)
    public Page<ErrorHistoryDTO> findAll(Pageable pageable) {
        return errorHistoryRepository
                .findAll(pageable)
                .map(errorHistoryMapper::toDTO);
    }
}
