package com.wan.framework.user.dto;

import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private String userId;
    private String password;
    private String name;
    private DataStateCode dataCode;
    private String passwordSalt;
    private String authLevel;

    public UserDTO removePass() {
        this.password = null;
        this.passwordSalt = null;
        return this;
    }
}
