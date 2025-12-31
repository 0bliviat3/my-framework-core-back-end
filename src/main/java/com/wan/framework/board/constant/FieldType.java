package com.wan.framework.board.constant;

public enum FieldType {
    TEXT,           // 단일 텍스트
    TEXTAREA,       // 여러 줄 텍스트
    NUMBER,         // 숫자
    DATE,           // 날짜
    DATETIME,       // 날짜+시간
    SELECT,         // 단일 선택 (드롭다운)
    CHECKBOX,       // 체크박스
    RADIO,          // 라디오 버튼
    FILE,           // 파일 업로드
    EMAIL,          // 이메일
    URL             // URL
}
