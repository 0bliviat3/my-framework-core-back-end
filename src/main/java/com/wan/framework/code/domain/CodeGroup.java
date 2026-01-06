package com.wan.framework.code.domain;

import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.DataStateCode.I;

/**
 * 공통코드 그룹 엔티티
 * - 공통코드를 분류하는 그룹 (예: 사용자상태, 게시판타입 등)
 * - 1:N 관계로 CodeItem을 포함
 */
@Entity
@Table(name = "t_code_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeGroup {

    @Id
    @Column(name = "group_code", length = 50)
    private String groupCode;  // 그룹 코드 (PK, 예: USER_STATUS, BOARD_TYPE)

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;  // 그룹명 (예: 사용자 상태, 게시판 타입)

    @Column(name = "description", length = 500)
    private String description;  // 그룹 설명

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;  // 활성화 여부

    @Column(name = "sort_order")
    private Integer sortOrder;  // 정렬 순서

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
