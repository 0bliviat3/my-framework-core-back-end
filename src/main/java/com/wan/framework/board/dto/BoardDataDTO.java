package com.wan.framework.board.dto;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.BoardDataStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDataDTO {

    private Long id;
    private Long boardMetaId;
    private String boardMetaTitle; // 게시판 이름 (조회용)
    private String authorId;
    private String authorName; // 작성자 이름 (조회용)
    private String title;
    private String content;
    private String fieldDataJson;
    private BoardDataStatus status;
    private Long viewCount;
    private Integer commentCount;
    private DataStateCode dataStateCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    // 이전글/다음글 정보
    private Long prevId;
    private String prevTitle;
    private Long nextId;
    private String nextTitle;
}
