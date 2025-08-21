package com.wan.framework.user.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.user.constant.UserExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.INVALID_ERROR;

@Slf4j
public class UserException extends IllegalArgumentException implements FrameworkException {

    public UserException() {
    }

    public UserException(UserExceptionMessage message) {
        super(message.getMessage());
        log.error("user exception: {}", message.getMessage());
    }
    public UserException(UserExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("user exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }
    public UserException(UserExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("user exception: {}", String.format(message.getMessage(), args));
    }
    public UserException(UserExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("user exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
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
