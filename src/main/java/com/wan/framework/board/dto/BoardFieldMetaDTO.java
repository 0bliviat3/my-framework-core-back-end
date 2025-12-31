package com.wan.framework.board.dto;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardFieldMetaDTO {

    private Long id;
    private Long boardMetaId;
    private String fieldName;
    private String fieldLabel;
    private FieldType fieldType;
    private Integer displayOrder;
    private Boolean required;
    private Boolean showInList;
    private Boolean showInDetail;
    private Boolean showInForm;
    private Boolean searchable;
    private String fieldOptions;
    private String placeholder;
    private String defaultValue;
    private DataStateCode dataStateCode;
}
