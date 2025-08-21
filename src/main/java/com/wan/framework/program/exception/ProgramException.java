package com.wan.framework.program.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.program.constant.ProgramExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.PROGRAM_ERROR;

@Slf4j
public class ProgramException extends IllegalArgumentException implements FrameworkException {

    public ProgramException() {
    }

    public ProgramException(ProgramExceptionMessage message) {
        super(message.getMessage());
        log.error("program exception: {}", message.getMessage());
    }
    public ProgramException(ProgramExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("program exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }
    public ProgramException(ProgramExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("program exception: {}", String.format(message.getMessage(), args));
    }
    public ProgramException(ProgramExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("program exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(PROGRAM_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(PROGRAM_ERROR.getMessage())
                .build();
    }
}
