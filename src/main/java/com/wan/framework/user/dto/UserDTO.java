package com.wan.framework.user.dto;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.user.constant.RoleType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String userId;
    private String password;
    private String name;
    private DataStateCode dataCode;
    private String passwordSalt;
    private Set<RoleType> roles;
    private LocalDateTime createTime;
    private LocalDateTime modifiedTime;

    /**
     * 비밀번호 및 Salt 제거 (보안)
     */
    public UserDTO removePass() {
        this.password = null;
        this.passwordSalt = null;
        return this;
    }

    /**
     * 역할을 문자열 리스트로 반환
     */
    public List<String> getRoleNames() {
        if (roles == null || roles.isEmpty()) {
            return List.of("ROLE_USER");
        }
        return roles.stream()
                .map(RoleType::name)
                .toList();
    }
}
