package com.wan.framework.redis.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RedisExceptionMessage {
    // 분산 락
    LOCK_ACQUIRE_FAILED("REDIS_001", "분산 락 획득에 실패했습니다."),
    LOCK_RELEASE_FAILED("REDIS_002", "분산 락 해제에 실패했습니다."),
    LOCK_NOT_OWNED("REDIS_003", "락 소유자가 아닙니다."),
    LOCK_TIMEOUT("REDIS_004", "락 획득 타임아웃이 발생했습니다."),

    // 캐시
    CACHE_GET_FAILED("REDIS_010", "캐시 조회에 실패했습니다."),
    CACHE_SET_FAILED("REDIS_011", "캐시 저장에 실패했습니다."),
    CACHE_DELETE_FAILED("REDIS_012", "캐시 삭제에 실패했습니다."),

    // 연결
    CONNECTION_FAILED("REDIS_020", "Redis 연결에 실패했습니다."),
    INVALID_CONFIGURATION("REDIS_021", "Redis 설정이 올바르지 않습니다."),

    // 일반
    OPERATION_FAILED("REDIS_030", "Redis 작업 실행에 실패했습니다.");

    private final String code;
    private final String message;
}
