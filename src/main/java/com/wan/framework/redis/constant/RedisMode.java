package com.wan.framework.redis.constant;

/**
 * Redis 구성 모드
 */
public enum RedisMode {
    STANDALONE,  // 단일 Redis 인스턴스
    SENTINEL,    // Master 장애 자동 전환
    CLUSTER      // 샤딩 + 고가용성
}
