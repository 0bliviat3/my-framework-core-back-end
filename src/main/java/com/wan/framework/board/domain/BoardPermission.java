package com.wan.framework.board.domain;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.PermissionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_board_permission", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"board_meta_id", "role_or_user_id", "permission_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_meta_id", nullable = false)
    private BoardMeta boardMeta;

    @Column(nullable = false, length = 100)
    private String roleOrUserId; // 역할(ROLE_USER) 또는 사용자 ID

    @Column(nullable = false)
    private Boolean isRole; // true: 역할, false: 사용자

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionType permissionType; // 권한 타입

    @Column(nullable = false)
    private Boolean allowed; // true: 허용, false: 거부

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
