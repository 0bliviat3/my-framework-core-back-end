package com.wan.framework.proxy.constant;

/**
 * API 실행 트리거 타입
 */
public enum ExecutionTrigger {

    /**
     * 수동 실행
     */
    MANUAL,

    /**
     * 스케줄러에 의한 실행
     */
    SCHEDULER,

    /**
     * 배치 작업에 의한 실행
     */
    BATCH
}
