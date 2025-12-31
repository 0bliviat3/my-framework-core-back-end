package com.wan.framework.board.domain;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.BoardDataStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.DataStateCode.I;
import static com.wan.framework.board.constant.BoardDataStatus.PUBLISHED;

@Entity
@Table(name = "t_board_data", indexes = {
    @Index(name = "idx_board_meta_status", columnList = "board_meta_id, status"),
    @Index(name = "idx_author_id", columnList = "author_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_meta_id", nullable = false)
    private BoardMeta boardMeta;

    @Column(nullable = false, length = 100)
    private String authorId; // 작성자 ID

    @Column(nullable = false, length = 200)
    private String title; // 제목

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content; // 내용

    @Lob
    @Column(columnDefinition = "TEXT")
    private String fieldDataJson; // 동적 필드 데이터 (JSON)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BoardDataStatus status; // 상태 (DRAFT, PUBLISHED, PINNED)

    @Column(nullable = false)
    @Builder.Default
    private Long viewCount = 0L; // 조회수

    @Column(nullable = false)
    @Builder.Default
    private Integer commentCount = 0; // 댓글 수

    @Column(name = "data_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataStateCode dataStateCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt; // 발행 일시

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.dataStateCode = I;
        if (this.status == null) {
            this.status = PUBLISHED;
        }
        if (this.status == PUBLISHED && this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status == PUBLISHED && this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 댓글 수 증가
    public void incrementCommentCount() {
        this.commentCount++;
    }

    // 댓글 수 감소
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
