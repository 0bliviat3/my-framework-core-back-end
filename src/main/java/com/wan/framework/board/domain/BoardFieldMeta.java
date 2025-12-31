package com.wan.framework.board.domain;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.FieldType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_board_field_meta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardFieldMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_meta_id", nullable = false)
    private BoardMeta boardMeta;

    @Column(nullable = false, length = 100)
    private String fieldName; // 필드 이름 (영문, 컬럼명)

    @Column(nullable = false, length = 100)
    private String fieldLabel; // 필드 라벨 (화면 표시명)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FieldType fieldType; // 필드 타입

    @Column(nullable = false)
    private Integer displayOrder; // 표시 순서

    @Column(nullable = false)
    private Boolean required; // 필수 여부

    @Column(nullable = false)
    private Boolean showInList; // 목록 화면 노출

    @Column(nullable = false)
    private Boolean showInDetail; // 상세 화면 노출

    @Column(nullable = false)
    private Boolean showInForm; // 작성/수정 폼 노출

    @Column(nullable = false)
    private Boolean searchable; // 검색 가능 여부

    @Lob
    @Column(columnDefinition = "TEXT")
    private String fieldOptions; // 필드 옵션 (JSON: select 옵션, validation 등)

    @Column(length = 255)
    private String placeholder; // 입력 힌트

    @Column(length = 255)
    private String defaultValue; // 기본값

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
}
