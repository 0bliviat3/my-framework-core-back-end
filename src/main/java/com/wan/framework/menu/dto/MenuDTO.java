package com.wan.framework.menu.dto;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDTO {
    private Long id;
    private Long parentId;
    private Long programId;
    private String name;
    private String programName;
    private String type;
    private String roles;
    private String icon;
    private String path;
    private String frontPath;
    private DataStateCode dataStateCode;
    private AbleState ableState;
}
