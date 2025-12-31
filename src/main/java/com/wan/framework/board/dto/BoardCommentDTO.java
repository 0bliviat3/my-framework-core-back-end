package com.wan.framework.board.dto;

import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardCommentDTO {

    private Long id;
    private Long boardDataId;
    private Long parentId;
    private String authorId;
    private String authorName; // 작성자 이름 (조회용)
    private String content;
    private Boolean isModified;
    private DataStateCode dataStateCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 계층형 댓글을 위한 필드
    @Builder.Default
    private List<BoardCommentDTO> children = new ArrayList<>();
    private Integer childCount;

    // 권한 체크용
    private Boolean canEdit;
    private Boolean canDelete;
}
