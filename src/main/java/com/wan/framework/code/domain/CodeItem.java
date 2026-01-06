package com.wan.framework.code.domain;

import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.DataStateCode.I;

/**
 * 공통코드 항목 엔티티
 * - 그룹에 속하는 개별 코드 항목 (예: ACTIVE, INACTIVE 등)
 */
@Entity
@Table(name = "t_code_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", nullable = false, length = 50)
    private String groupCode;  // 그룹 코드 (FK)

    @Column(name = "code_value", nullable = false, length = 50)
    private String codeValue;  // 코드 값 (예: ACTIVE, DRAFT, PUBLISHED)

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;  // 코드명 (예: 활성, 임시저장, 게시됨)

    @Column(name = "description", length = 500)
    private String description;  // 코드 설명

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;  // 활성화 여부

    @Column(name = "sort_order")
    private Integer sortOrder;  // 정렬 순서

    @Column(name = "attribute1", length = 100)
    private String attribute1;  // 추가 속성 1

    @Column(name = "attribute2", length = 100)
    private String attribute2;  // 추가 속성 2

    @Column(name = "attribute3", length = 100)
    private String attribute3;  // 추가 속성 3

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @Column(name = "data_state")
    @Enumerated(EnumType.STRING)
    private DataStateCode dataState;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.dataState = I;
        if (this.enabled == null) {
            this.enabled = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedTime = LocalDateTime.now();
    }
}
