package com.wan.framework.history.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.history.constant.ErrorHistoryExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.ERROR_HISTORY_ERROR;

@Slf4j
public class ErrorHistoryException extends IllegalArgumentException implements FrameworkException {

    public ErrorHistoryException() {
    }

    public ErrorHistoryException(ErrorHistoryExceptionMessage message) {
        super(message.getMessage());
        log.error("error history exception: {}", message.getMessage());
    }

    public ErrorHistoryException(ErrorHistoryExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("error history exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }

    public ErrorHistoryException(ErrorHistoryExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("error history exception: {}", String.format(message.getMessage(), args));
    }

    public ErrorHistoryException(ErrorHistoryExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("error history exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(ERROR_HISTORY_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(ERROR_HISTORY_ERROR.getMessage())
                .build();
    }
}
