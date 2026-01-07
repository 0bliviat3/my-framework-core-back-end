package com.wan.framework.session.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.session.constant.SessionExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.INVALID_ERROR;

/**
 * 세션 모듈 예외
 */
@Slf4j
public class SessionException extends IllegalArgumentException implements FrameworkException {

    public SessionException() {
    }

    public SessionException(SessionExceptionMessage message) {
        super(message.getMessage());
        log.error("session exception: {}", message.getMessage());
    }

    public SessionException(SessionExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("session exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }

    public SessionException(SessionExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("session exception: {}", String.format(message.getMessage(), args));
    }

    public SessionException(SessionExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("session exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(INVALID_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(INVALID_ERROR.getMessage())
                .build();
    }
}
