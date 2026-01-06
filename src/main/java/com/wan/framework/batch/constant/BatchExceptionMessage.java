package com.wan.framework.batch.constant;

import lombok.Getter;

/**
 * 배치 관리 예외 메시지
 */
@Getter
public enum BatchExceptionMessage {

    // 배치 작업 관련
    BATCH_JOB_NOT_FOUND("BATCH_001", "배치 작업을 찾을 수 없습니다."),
    BATCH_ID_ALREADY_EXISTS("BATCH_002", "이미 존재하는 배치 ID입니다."),
    BATCH_JOB_DISABLED("BATCH_003", "비활성화된 배치 작업입니다."),
    BATCH_JOB_RUNNING("BATCH_004", "실행 중인 배치 작업은 삭제할 수 없습니다."),
    INVALID_SCHEDULE_EXPRESSION("BATCH_005", "유효하지 않은 스케줄 표현식입니다."),

    // 배치 실행 관련
    BATCH_EXECUTION_NOT_FOUND("BATCH_006", "배치 실행 이력을 찾을 수 없습니다."),
    BATCH_EXECUTION_FAILED("BATCH_007", "배치 실행에 실패했습니다."),
    BATCH_LOCK_ACQUISITION_FAILED("BATCH_008", "배치 락 획득에 실패했습니다."),
    BATCH_ALREADY_RUNNING("BATCH_009", "이미 실행 중인 배치입니다."),
    BATCH_TIMEOUT("BATCH_010", "배치 실행 시간이 초과되었습니다."),

    // 재시도 관련
    MAX_RETRY_EXCEEDED("BATCH_011", "최대 재시도 횟수를 초과했습니다."),
    RETRY_NOT_ALLOWED("BATCH_012", "재시도가 허용되지 않은 상태입니다."),
    CANNOT_RETRY_SUCCESS("BATCH_016", "성공한 배치는 재시도할 수 없습니다."),
    CANNOT_RETRY_RUNNING("BATCH_017", "실행 중인 배치는 재시도할 수 없습니다."),

    // Proxy API 연계 관련
    PROXY_API_NOT_CONFIGURED("BATCH_013", "Proxy API가 설정되지 않았습니다."),
    PROXY_API_EXECUTION_FAILED("BATCH_014", "Proxy API 실행에 실패했습니다."),

    // 파라미터 관련
    INVALID_EXECUTION_PARAMETERS("BATCH_015", "유효하지 않은 실행 파라미터입니다.");

    private final String code;
    private final String message;

    BatchExceptionMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
