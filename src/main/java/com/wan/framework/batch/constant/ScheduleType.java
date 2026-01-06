package com.wan.framework.batch.constant;

/**
 * 스케줄 타입
 */
public enum ScheduleType {

    /**
     * CRON 표현식
     * 예: "0 0 * * * ?" (매 시간)
     */
    CRON,

    /**
     * 실행 간격 (밀리초)
     * 예: "60000" (60초)
     */
    INTERVAL
}
