package com.wan.framework.session.dto;

import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 세션 감사 로그 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionAuditDTO {
    private Long id;
    private String sessionId;
    private String userId;
    private String eventType;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime eventTime;
    private String additionalInfo;
    private LocalDateTime createTime;
    private LocalDateTime modifiedTime;
    private DataStateCode dataState;
}
