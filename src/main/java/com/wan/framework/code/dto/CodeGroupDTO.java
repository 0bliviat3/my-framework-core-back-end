package com.wan.framework.code.dto;

import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통코드 그룹 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeGroupDTO {
    private String groupCode;
    private String groupName;
    private String description;
    private Boolean enabled;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime modifiedTime;
    private DataStateCode dataState;
}
