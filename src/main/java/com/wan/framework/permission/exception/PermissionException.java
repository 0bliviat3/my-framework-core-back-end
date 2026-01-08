package com.wan.framework.permission.exception;

import com.wan.framework.permission.constant.PermissionExceptionMessage;
import lombok.Getter;

/**
 * 권한 관리 예외
 */
@Getter
public class PermissionException extends RuntimeException {

    private final String code;
    private final String message;

    public PermissionException(PermissionExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
        this.code = exceptionMessage.getCode();
        this.message = exceptionMessage.getMessage();
    }

    public PermissionException(PermissionExceptionMessage exceptionMessage, Throwable cause) {
        super(exceptionMessage.getMessage(), cause);
        this.code = exceptionMessage.getCode();
        this.message = exceptionMessage.getMessage();
    }
}
