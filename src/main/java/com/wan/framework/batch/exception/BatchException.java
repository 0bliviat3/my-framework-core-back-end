package com.wan.framework.batch.exception;

import com.wan.framework.base.exception.ExceptionResponse;
import com.wan.framework.base.exception.FrameworkException;
import com.wan.framework.batch.constant.BatchExceptionMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.wan.framework.base.exception.ExceptionConst.BATCH_ERROR;

/**
 * 배치 관리 관련 예외
 */
@Slf4j
@Getter
public class BatchException extends IllegalArgumentException implements FrameworkException {

    private final BatchExceptionMessage batchExceptionMessage;

    public BatchException(BatchExceptionMessage message) {
        super(message.getMessage());
        this.batchExceptionMessage = message;
        log.error("batch exception: {} - {}", message.getCode(), message.getMessage());
    }

    public BatchException(BatchExceptionMessage message, Throwable cause) {
        super(message.getMessage(), cause);
        this.batchExceptionMessage = message;
        log.error("batch exception: {} - {} / cause: {}",
                message.getCode(),
                message.getMessage(),
                cause.getMessage());
    }

    @Override
    public ExceptionResponse getResponse() {
        return ExceptionResponse.builder()
                .errorCode(BATCH_ERROR.getCode())
                .eventTime(LocalDateTime.now())
                .responseMessage(BATCH_ERROR.getMessage())
                .build();
    }
}
