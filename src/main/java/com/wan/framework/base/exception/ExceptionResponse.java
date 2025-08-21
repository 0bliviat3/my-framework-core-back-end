package com.wan.framework.base.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionResponse {

    private String errorCode;
    private String responseMessage;
    private LocalDateTime eventTime;
}
