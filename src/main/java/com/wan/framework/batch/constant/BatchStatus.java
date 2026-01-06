package com.wan.framework.batch.constant;

/**
 * 배치 실행 상태
 */
public enum BatchStatus {

    /**
     * 대기
     */
    WAIT,

    /**
     * 실행 중
     */
    RUNNING,

    /**
     * 성공
     */
    SUCCESS,

    /**
     * 실패
     */
    FAIL,

    /**
     * 재처리 중
     */
    RETRY,

    /**
     * 타임아웃
     */
    TIMEOUT
}
