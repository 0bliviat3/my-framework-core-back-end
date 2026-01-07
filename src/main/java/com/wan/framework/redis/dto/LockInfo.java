package com.wan.framework.redis.dto;

import lombok.Getter;

/**
 * 락 정보 (재진입 락용)
 */
@Getter
public class LockInfo {
    private final String lockValue;
    private int count;

    public LockInfo(String lockValue, int count) {
        this.lockValue = lockValue;
        this.count = count;
    }

    /**
     * 재진입 카운트 증가
     */
    public void incrementCount() {
        this.count++;
    }

    /**
     * 재진입 카운트 감소
     */
    public void decrementCount() {
        this.count--;
    }
}
