package com.wan.framework.user.domain;

import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_user")
@Setter
@Getter
@RequiredArgsConstructor
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

    @Column(name = "auth_level")
    private String authLevel;
}
