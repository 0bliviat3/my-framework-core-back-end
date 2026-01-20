package com.wan.framework.history.service;

import com.wan.framework.history.constant.ErrorHistoryExceptionMessage;
import com.wan.framework.history.domain.ErrorHistory;
import com.wan.framework.history.dto.ErrorHistoryDTO;
import com.wan.framework.history.exception.ErrorHistoryException;
import com.wan.framework.history.mapper.ErrorHistoryMapper;
import com.wan.framework.history.repository.ErrorHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import static com.wan.framework.history.constant.ErrorHistoryExceptionMessage.*;

@Slf4j
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
        log.debug("전체 에러 로그 조회 - page: {}", pageable.getPageNumber());
        return errorHistoryRepository
                .findAll(pageable)
                .map(errorHistoryMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ErrorHistoryDTO findById(Long errorId) {
        log.debug("에러 로그 단일 조회 - errorId: {}", errorId);
        ErrorHistory errorHistory = errorHistoryRepository.findById(errorId)
                .orElseThrow(() -> new ErrorHistoryException(NOT_FOUND));
        return errorHistoryMapper.toDTO(errorHistory);
    }

    @Transactional(readOnly = true)
    public Page<ErrorHistoryDTO> findByRequestURL(String url, Pageable pageable) {
        log.debug("URL 기준 에러 로그 검색 - url: {}, page: {}", url, pageable.getPageNumber());
        return errorHistoryRepository
                .findByRequestURLContaining(url, pageable)
                .map(errorHistoryMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ErrorHistoryDTO> findByErrorMessage(String message, Pageable pageable) {
        log.debug("에러 메시지 기준 검색 - message: {}, page: {}", message, pageable.getPageNumber());
        return errorHistoryRepository
                .findByErrorMessageContaining(message, pageable)
                .map(errorHistoryMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ErrorHistoryDTO> findByEventTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        log.debug("기간 기준 에러 로그 검색 - start: {}, end: {}, page: {}", start, end, pageable.getPageNumber());
        if (start.isAfter(end)) {
            throw new ErrorHistoryException(INVALID_DATE_RANGE);
        }
        return errorHistoryRepository
                .findByEventTimeBetween(start, end, pageable)
                .map(errorHistoryMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ErrorHistoryDTO> findByEventTimeAfter(LocalDateTime since, Pageable pageable) {
        log.debug("특정 시간 이후 에러 로그 조회 - since: {}, page: {}", since, pageable.getPageNumber());
        return errorHistoryRepository
                .findByEventTimeAfter(since, pageable)
                .map(errorHistoryMapper::toDTO);
    }
}
