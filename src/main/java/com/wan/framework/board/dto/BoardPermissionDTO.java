package com.wan.framework.board.dto;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPermissionDTO {

    private Long id;
    private Long boardMetaId;
    private String roleOrUserId;
    private Boolean isRole;
    private PermissionType permissionType;
    private Boolean allowed;
    private DataStateCode dataStateCode;
}
