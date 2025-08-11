package com.wan.framework.program.dto;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramDTO {

    private Long id;
    private String name;
    private String frontPath;
    private String path;
    private String apiKey;
    private String description;
    private DataStateCode dataStateCode;
    private AbleState ableState;

}
