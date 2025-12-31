package com.wan.framework.board.dto;

import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardAttachmentDTO {

    private Long id;
    private Long boardDataId;
    private String originalFileName;
    private String savedFileName;
    private String filePath;
    private Long fileSize;
    private String formattedFileSize; // 읽기 쉬운 파일 크기
    private String contentType;
    private Integer displayOrder;
    private String uploadedBy;
    private String uploadedByName; // 업로더 이름 (조회용)
    private Long downloadCount;
    private DataStateCode dataStateCode;
    private LocalDateTime createdAt;
    private String fileExtension; // 파일 확장자
}
