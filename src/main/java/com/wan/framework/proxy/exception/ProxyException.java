package com.wan.framework.proxy.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.proxy.constant.ProxyExceptionMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.PROXY_ERROR;

/**
 * Proxy API 관련 예외
 */
@Slf4j
@Getter
public class ProxyException extends IllegalArgumentException implements FrameworkException {

    private final ProxyExceptionMessage proxyExceptionMessage;

    public ProxyException(ProxyExceptionMessage message) {
        super(message.getMessage());
        this.proxyExceptionMessage = message;
        log.error("proxy exception: {} - {}", message.getCode(), message.getMessage());
    }

    public ProxyException(ProxyExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        this.proxyExceptionMessage = message;
        log.error("proxy exception: {} - {} / cause: {}",
                message.getCode(),
                message.getMessage(),
                cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(PROXY_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(PROXY_ERROR.getMessage())
                .build();
    }
}
