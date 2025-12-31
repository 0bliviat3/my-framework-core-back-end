package com.wan.framework.board.domain;

import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_board_attachment", indexes = {
    @Index(name = "idx_board_data_id", columnList = "board_data_id"),
    @Index(name = "idx_uploaded_by", columnList = "uploaded_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_data_id", nullable = false)
    private BoardData boardData;

    @Column(nullable = false, length = 255)
    private String originalFileName; // 원본 파일명

    @Column(nullable = false, length = 255)
    private String savedFileName; // 저장된 파일명 (UUID 등)

    @Column(nullable = false, length = 500)
    private String filePath; // 파일 저장 경로

    @Column(nullable = false)
    private Long fileSize; // 파일 크기 (bytes)

    @Column(length = 100)
    private String contentType; // MIME 타입

    @Column(nullable = false)
    private Integer displayOrder; // 표시 순서

    @Column(nullable = false, length = 100)
    private String uploadedBy; // 업로드한 사용자 ID

    @Column(nullable = false)
    @Builder.Default
    private Long downloadCount = 0L; // 다운로드 횟수

    @Column(name = "data_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataStateCode dataStateCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.dataStateCode = I;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 다운로드 수 증가
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    // 파일 확장자 추출
    public String getFileExtension() {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        }
        return "";
    }

    // 파일 크기를 읽기 쉬운 형식으로 변환
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
        }
    }
}
