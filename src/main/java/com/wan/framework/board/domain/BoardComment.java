package com.wan.framework.board.domain;

import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_board_comment", indexes = {
    @Index(name = "idx_board_data_id", columnList = "board_data_id"),
    @Index(name = "idx_parent_id", columnList = "parent_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_data_id", nullable = false)
    private BoardData boardData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BoardComment parent; // 부모 댓글 (대댓글용)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BoardComment> children = new ArrayList<>(); // 자식 댓글들

    @Column(nullable = false, length = 100)
    private String authorId; // 작성자 ID

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 댓글 내용

    @Column(nullable = false)
    @Builder.Default
    private Boolean isModified = false; // 수정 여부

    @Column(name = "data_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataStateCode dataStateCode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt; // 삭제 시각 (이력 관리)

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.dataStateCode = I;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 댓글 수정
    public void updateContent(String newContent) {
        this.content = newContent;
        this.isModified = true;
    }

    // 자식 댓글 추가
    public void addChild(BoardComment child) {
        this.children.add(child);
        child.setParent(this);
    }

    // 대댓글 수 조회
    public int getChildCount() {
        return children.size();
    }
}
