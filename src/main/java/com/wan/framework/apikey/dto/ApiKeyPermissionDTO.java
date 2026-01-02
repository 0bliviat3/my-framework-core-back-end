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
public class ApiKeyPermissionDTO {

    private Long id;
    private Long apiKeyId;
    private String permission;
    private String createdBy;
    private LocalDateTime createdAt;
}
