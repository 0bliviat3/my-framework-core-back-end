package com.wan.framework.menu.domain;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.permission.domain.Role;
import com.wan.framework.program.domain.Program;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.AbleState.ABLE;
import static com.wan.framework.base.constant.DataStateCode.I;
import static com.wan.framework.base.constant.DataStateCode.U;

@Entity
@Table(name = "t_menu")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @ManyToOne
    @JoinColumn(name = "program_id", referencedColumnName = "program_id")
    private Program program;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "t_menu_role_mapping",
        joinColumns = @JoinColumn(name = "menu_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "icon")
    private String icon;

    @Column(name = "data_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataStateCode dataStateCode;

    @Column(name = "able_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private AbleState ableState;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.ableState = ABLE;
        this.dataStateCode = I;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === Role Entity 기반 헬퍼 메서드 ===

    /**
     * Role Code 목록 조회
     * 메서드명을 extractRoleCodes로 변경하여 MapStruct가 JavaBean property로 인식하지 않도록 함
     */
    public Set<String> extractRoleCodes() {
        if (this.roles == null || this.roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());
    }

    /**
     * 특정 Role 보유 여부 확인
     */
    public boolean hasRole(String roleCode) {
        if (this.roles == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> role.getRoleCode().equals(roleCode));
    }
}
