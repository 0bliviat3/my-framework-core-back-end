package com.wan.framework.apikey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyUsageHistoryDTO {

    private Long id;
    private Long apiKeyId;
    private String apiKeyPrefix; // 조회용
    private String requestUri;
    private String requestMethod;
    private String ipAddress;
    private String userAgent;
    private Integer responseStatus;
    private Boolean isSuccess;
    private String errorMessage;
    private LocalDateTime usedAt;
}
