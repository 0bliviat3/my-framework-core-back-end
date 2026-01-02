package com.wan.framework.apikey.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.apikey.constant.ApiKeyExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.APIKEY_ERROR;

@Slf4j
public class ApiKeyException extends IllegalArgumentException implements FrameworkException {

    public ApiKeyException() {
    }

    public ApiKeyException(ApiKeyExceptionMessage message) {
        super(message.getMessage());
        log.error("api key exception: {}", message.getMessage());
    }

    public ApiKeyException(ApiKeyExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("api key exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }

    public ApiKeyException(ApiKeyExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("api key exception: {}", String.format(message.getMessage(), args));
    }

    public ApiKeyException(ApiKeyExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("api key exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(APIKEY_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(APIKEY_ERROR.getMessage())
                .build();
    }
}
