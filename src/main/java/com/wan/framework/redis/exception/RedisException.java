package com.wan.framework.redis.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.redis.constant.RedisExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.REDIS_ERROR;

@Slf4j
public class RedisException extends IllegalArgumentException implements FrameworkException {

    public RedisException() {
    }

    public RedisException(RedisExceptionMessage message) {
        super(message.getMessage());
        log.error("redis exception: {}", message.getMessage());
    }

    public RedisException(RedisExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("redis exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }

    public RedisException(RedisExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("redis exception: {}", String.format(message.getMessage(), args));
    }

    public RedisException(RedisExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("redis exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(REDIS_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(REDIS_ERROR.getMessage())
                .build();
    }
}
