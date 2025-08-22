package com.wan.framework.board.dto;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardMetaDTO {

    private Long id;
    private String title; // 게시판 이름
    private String description; // 게시판 설명
    private String formDefinitionJson; // 게시판 입력폼 정의(JSON)
    private String roles; // 접근권한 (ROLE_USER, ROLE_ADMIN 등)
    private Boolean useComment; // 댓글 사용 여부
    private String createdBy; // 생성자 ID
    private DataStateCode dataStateCode;
    private AbleState ableState;
}
