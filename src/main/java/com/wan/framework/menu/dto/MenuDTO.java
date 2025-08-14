package com.wan.framework.menu.dto;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDTO {
    private Long id;
    private Long parentId;
    private Long programId;
    private String name;
    private String type;
    private DataStateCode dataStateCode;
    private AbleState ableState;
}
