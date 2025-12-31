package com.wan.framework.board.constant;

import com.wan.framework.base.exception.ExceptionConst;

public enum BoardExceptionMessage implements ExceptionConst {
    // BoardMeta
    DUPLICATED_TITLE("BOARD_001", "중복된 게시판 이름입니다."),
    NOT_FOUND_META("BOARD_002", "존재하지 않는 게시판입니다."),

    // BoardFieldMeta
    NOT_FOUND_FIELD_META("BOARD_003", "존재하지 않는 필드입니다."),
    DUPLICATED_FIELD_NAME("BOARD_004", "중복된 필드 이름입니다."),

    // BoardData
    NOT_FOUND_BOARD_DATA("BOARD_005", "존재하지 않는 게시글입니다."),
    UNAUTHORIZED_ACCESS("BOARD_006", "접근 권한이 없습니다."),
    CANNOT_MODIFY_OTHER_POST("BOARD_007", "다른 사용자의 게시글은 수정할 수 없습니다."),
    CANNOT_DELETE_OTHER_POST("BOARD_008", "다른 사용자의 게시글은 삭제할 수 없습니다."),

    // BoardComment
    NOT_FOUND_COMMENT("BOARD_009", "존재하지 않는 댓글입니다."),
    CANNOT_MODIFY_OTHER_COMMENT("BOARD_010", "다른 사용자의 댓글은 수정할 수 없습니다."),
    CANNOT_DELETE_OTHER_COMMENT("BOARD_011", "다른 사용자의 댓글은 삭제할 수 없습니다."),
    COMMENT_NOT_ALLOWED("BOARD_012", "댓글을 사용할 수 없는 게시판입니다."),

    // BoardAttachment
    NOT_FOUND_ATTACHMENT("BOARD_013", "존재하지 않는 첨부파일입니다."),
    FILE_UPLOAD_FAILED("BOARD_014", "파일 업로드에 실패했습니다."),

    // BoardPermission
    NOT_FOUND_PERMISSION("BOARD_015", "존재하지 않는 권한입니다."),
    DUPLICATED_PERMISSION("BOARD_016", "이미 존재하는 권한입니다."),

    // General
    NOT_FOUND_ID("BOARD_099", "요청한 리소스를 찾을 수 없습니다.");

    private final String code;
    private final String message;

    BoardExceptionMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
