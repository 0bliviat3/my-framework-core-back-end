package com.wan.framework.base.exception;

import com.wan.framework.history.service.ErrorHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.INVALID_ERROR;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class FrameworkExceptionHandler {

    private final ErrorHistoryService errorHistoryService;

    private ExceptionResponse wrapException(Exception e) {
        if (e instanceof FrameworkException) {
            return ((FrameworkException) e).getResponse();
        }
        return ExceptionResponse.builder()
                .errorCode(INVALID_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(INVALID_ERROR.getMessage())
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("exception handle: {}", e.getMessage());
        errorHistoryService.saveException(e, request);
        return ResponseEntity
                .badRequest()
                .body(wrapException(e));
    }

}
