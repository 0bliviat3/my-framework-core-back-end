package com.wan.framework.batch.constant;

/**
 * 배치 실행 트리거 유형
 */
public enum BatchTriggerType {

    /**
     * 스케줄러에 의한 정기 실행
     */
    SCHEDULER,

    /**
     * 관리자 수동 실행
     */
    MANUAL,

    /**
     * 자동 재처리
     */
    RETRY
}
