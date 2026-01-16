package com.wan.framework.user.domain;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.permission.domain.Role;
import com.wan.framework.user.constant.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    @Column(name = "data_code")
    @Enumerated(EnumType.STRING)
    private DataStateCode dataCode;

    @Column(name = "password_salt")
    private String passwordSalt;

    // 기존 RoleType Enum 기반 (레거시, DB 매핑 제거됨)
    // @Transient로 변경하여 Hibernate가 테이블을 생성하지 않도록 함
    @Transient
    @Builder.Default
    private Set<RoleType> roles = new HashSet<>();

    // 신규 Role Entity 기반 (DB FK 관계)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "t_user_role_mapping",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roleEntities = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.dataCode = I;
        // 기본 역할 설정은 Service 계층에서 처리 (SignService)
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedTime = LocalDateTime.now();
    }

    // 비즈니스 로직을 위한 Setter (불변성 유지하면서 필요한 경우만 제공)
    public void updatePassword(String password, String passwordSalt) {
        this.password = password;
        this.passwordSalt = passwordSalt;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDataCode(DataStateCode dataCode) {
        this.dataCode = dataCode;
    }

    /**
     * Role Entity에서 roleCode 리스트 추출
     */
    public List<String> extractRoleCodes() {
        if (roleEntities == null || roleEntities.isEmpty()) {
            return List.of();
        }
        return roleEntities.stream()
                .map(Role::getRoleCode)
                .toList();
    }

    public void addRole(RoleType role) {
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        this.roles.add(role);
    }

    public void removeRole(RoleType role) {
        if (this.roles != null) {
            this.roles.remove(role);
        }
    }

    // === 신규 Role Entity 기반 메서드 ===

    /**
     * Role Entity 추가
     */
    public void addRoleEntity(Role role) {
        if (this.roleEntities == null) {
            this.roleEntities = new HashSet<>();
        }
        this.roleEntities.add(role);
    }

    /**
     * Role Entity 제거
     */
    public void removeRoleEntity(Role role) {
        if (this.roleEntities != null) {
            this.roleEntities.remove(role);
        }
    }

    /**
     * Role Code 목록 조회 (신규 Entity 기반)
     */
    public Set<String> getRoleCodes() {
        if (this.roleEntities == null || this.roleEntities.isEmpty()) {
            return Set.of();
        }
        return roleEntities.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toSet());
    }

    /**
     * 특정 Role 보유 여부 확인 (Role Code 기반)
     */
    public boolean hasRole(String roleCode) {
        if (this.roleEntities == null) {
            return false;
        }
        return roleEntities.stream()
                .anyMatch(role -> role.getRoleCode().equals(roleCode));
    }

    /**
     * ADMIN 권한 보유 여부 확인
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    /**
     * Role Entity 목록 설정
     */
    public void setRoleEntities(Set<Role> roleEntities) {
        this.roleEntities = roleEntities;
    }
}
