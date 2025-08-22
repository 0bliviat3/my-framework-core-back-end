package com.wan.framework.board.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.board.constant.BoardExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.BOARD_ERROR;

@Slf4j
public class BoardException extends IllegalArgumentException implements FrameworkException {

    public BoardException() {
    }

    public BoardException(BoardExceptionMessage message) {
        super(message.getMessage());
        log.error("board exception: {}", message.getMessage());
    }
    public BoardException(BoardExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        log.error("board exception: {}, cause: {}", message.getMessage(), cause.getMessage());
    }
    public BoardException(BoardExceptionMessage message, Object... args) {
        super(String.format(message.getMessage(), args));
        log.error("board exception: {}", String.format(message.getMessage(), args));
    }
    public BoardException(BoardExceptionMessage message, Throwable cause, Object... args) {
        super(String.format(message.getMessage(), args), cause);
        log.error("board exception: {}, cause: {}", String.format(message.getMessage(), args), cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(BOARD_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(BOARD_ERROR.getMessage())
                .build();
    }
}
