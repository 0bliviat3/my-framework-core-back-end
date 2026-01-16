package com.wan.framework.board.domain;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.base.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.AbleState.ABLE;
import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_board_meta")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardMeta extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String title; // 게시판 이름

    @Column(length = 255)
    private String description; // 게시판 설명

    @Lob
    @Column(columnDefinition = "TEXT")
    private String formDefinitionJson; // 게시판 입력폼 정의(JSON)

    @Column(length = 50)
    private String roles; // 접근권한 (ROLE_USER, ROLE_ADMIN 등)

    private Boolean useComment; // 댓글 사용 여부

    @Column(name = "data_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataStateCode dataStateCode;

    @Column(name = "able_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private AbleState ableState;

    @PrePersist
    protected void onCreate() {
        this.ableState = ABLE;
        this.dataStateCode = I;
    }

    @PreUpdate
    protected void onUpdate() {
        // BaseAuditEntity에서 updatedAt 처리
    }
}
