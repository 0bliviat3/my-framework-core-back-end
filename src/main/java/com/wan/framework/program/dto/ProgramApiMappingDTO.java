package com.wan.framework.program.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Program-API Mapping DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramApiMappingDTO {
    private Long mappingId;
    private Long programId;
    private String programName;
    private Long apiId;
    private String httpMethod;
    private String uriPattern;
    private Boolean required;
    private String description;
    private LocalDateTime createdAt;
}
