package com.wan.framework.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorHistoryDTO {
    private Long errorId;
    private String requestURL;
    private String requestParam;
    private String errorMessage;
    private LocalDateTime eventTime;
    private String stackTrace;
}
