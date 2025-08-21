package com.wan.framework.menu.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.menu.constant.MenuExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.MENU_ERROR;

@Slf4j
public class MenuException extends IllegalArgumentException implements FrameworkException {

    public MenuException() {
    }

    public MenuException(MenuExceptionMessage message) {
        super(message.getMessage());
        log.error("menu exception: {}", message.getMessage());
    }
    public MenuException(MenuExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("menu exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }
    public MenuException(MenuExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("menu exception: {}", String.format(message.getMessage(), args));
    }
    public MenuException(MenuExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("menu exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(MENU_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(MENU_ERROR.getMessage())
                .build();
    }
}
