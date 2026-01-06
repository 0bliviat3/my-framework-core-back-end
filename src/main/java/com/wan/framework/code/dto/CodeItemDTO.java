package com.wan.framework.code.dto;

import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통코드 항목 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeItemDTO {
    private Long id;
    private String groupCode;
    private String codeValue;
    private String codeName;
    private String description;
    private Boolean enabled;
    private Integer sortOrder;
    private String attribute1;
    private String attribute2;
    private String attribute3;
    private LocalDateTime createTime;
    private LocalDateTime modifiedTime;
    private DataStateCode dataState;
}
