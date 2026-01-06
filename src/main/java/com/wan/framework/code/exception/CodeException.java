package com.wan.framework.code.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.code.constant.CodeExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.INVALID_ERROR;

/**
 * 공통코드 모듈 예외
 */
@Slf4j
public class CodeException extends IllegalArgumentException implements FrameworkException {

    public CodeException() {
    }

    public CodeException(CodeExceptionMessage message) {
        super(message.getMessage());
        log.error("code exception: {}", message.getMessage());
    }

    public CodeException(CodeExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("code exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }

    public CodeException(CodeExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("code exception: {}", String.format(message.getMessage(), args));
    }

    public CodeException(CodeExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("code exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
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
