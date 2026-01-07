package com.wan.framework.user.domain;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.user.constant.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<RoleType> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.dataCode = I;
        if (this.roles == null || this.roles.isEmpty()) {
            this.roles = new HashSet<>();
            this.roles.add(RoleType.ROLE_USER);  // 기본 역할
        }
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
}
