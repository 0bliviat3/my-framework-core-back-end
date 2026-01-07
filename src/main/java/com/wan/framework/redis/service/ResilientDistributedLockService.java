package com.wan.framework.redis.service;

import com.wan.framework.redis.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

/**
 * Resilient 분산 락 서비스
 * - Redis 장애 시 LocalLock으로 자동 Fallback
 * - Circuit Breaker 패턴 적용
 * - 운영 환경에서는 이 서비스 사용 권장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResilientDistributedLockService {

    private final DistributedLockService distributedLockService;
    private final LocalLockService localLockService;

    // Circuit Breaker 상태
    private volatile boolean redisAvailable = true;
    private volatile long lastFailureTime = 0;
    private static final long CIRCUIT_OPEN_DURATION = 30000; // 30초

    /**
     * 분산 락 획득 (Fallback 포함)
     *
     * @param key        락 키
     * @param ttlSeconds TTL (초)
     * @return 락 소유자 식별 값
     */
    public String acquireLock(String key, long ttlSeconds) {
        if (!isRedisAvailable()) {
            log.warn("Redis unavailable, using local lock fallback: key={}", key);
            return localLockService.acquireLock(key, ttlSeconds);
        }

        try {
            String lockValue = distributedLockService.acquireLock(key, ttlSeconds);
            markRedisAvailable();
            return lockValue;
        } catch (RedisConnectionFailureException e) {
            markRedisUnavailable();
            log.error("Redis connection failed, falling back to local lock: key={}", key, e);
            return localLockService.acquireLock(key, ttlSeconds);
        } catch (RedisException e) {
            // 락 획득 실패는 정상적인 경쟁 상황이므로 Fallback하지 않음
            throw e;
        }
    }

    /**
     * 분산 락 획득 시도 (Fallback 포함, 타임아웃 포함)
     *
     * @param key            락 키
     * @param ttlSeconds     TTL (초)
     * @param waitTimeMillis 대기 시간 (밀리초)
     * @param retryInterval  재시도 간격 (밀리초)
     * @return 락 소유자 식별 값
     */
    public String acquireLockWithTimeout(String key, long ttlSeconds,
                                          long waitTimeMillis, long retryInterval) {
        if (!isRedisAvailable()) {
            log.warn("Redis unavailable, using local lock fallback with timeout: key={}", key);
            return localLockService.acquireLockWithTimeout(key, ttlSeconds, waitTimeMillis, retryInterval);
        }

        try {
            String lockValue = distributedLockService.acquireLockWithTimeout(
                    key, ttlSeconds, waitTimeMillis, retryInterval);
            markRedisAvailable();
            return lockValue;
        } catch (RedisConnectionFailureException e) {
            markRedisUnavailable();
            log.error("Redis connection failed, falling back to local lock with timeout: key={}", key, e);
            return localLockService.acquireLockWithTimeout(key, ttlSeconds, waitTimeMillis, retryInterval);
        } catch (RedisException e) {
            // 타임아웃은 정상적인 상황이므로 Fallback하지 않음
            throw e;
        }
    }

    /**
     * 분산 락 해제 (Fallback 포함)
     *
     * @param key       락 키
     * @param lockValue 락 소유자 식별 값
     */
    public void releaseLock(String key, String lockValue) {
        if (!isRedisAvailable()) {
            log.warn("Redis unavailable, releasing local lock: key={}", key);
            localLockService.releaseLock(key, lockValue);
            return;
        }

        try {
            distributedLockService.releaseLock(key, lockValue);
            markRedisAvailable();
        } catch (RedisConnectionFailureException e) {
            markRedisUnavailable();
            log.error("Redis connection failed, releasing local lock: key={}", key, e);
            localLockService.releaseLock(key, lockValue);
        }
    }

    /**
     * 재진입 락 획득 (Fallback 포함)
     *
     * @param key        락 키
     * @param ttlSeconds TTL (초)
     * @return 락 소유자 식별 값
     */
    public String acquireReentrantLock(String key, long ttlSeconds) {
        if (!isRedisAvailable()) {
            log.warn("Redis unavailable, using local lock fallback: key={}", key);
            return localLockService.acquireLock(key, ttlSeconds);
        }

        try {
            String lockValue = distributedLockService.acquireReentrantLock(key, ttlSeconds);
            markRedisAvailable();
            return lockValue;
        } catch (RedisConnectionFailureException e) {
            markRedisUnavailable();
            log.error("Redis connection failed, falling back to local lock: key={}", key, e);
            return localLockService.acquireLock(key, ttlSeconds);
        }
    }

    /**
     * 재진입 락 해제 (Fallback 포함)
     *
     * @param key       락 키
     * @param lockValue 락 소유자 식별 값
     */
    public void releaseReentrantLock(String key, String lockValue) {
        if (!isRedisAvailable()) {
            log.warn("Redis unavailable, releasing local lock: key={}", key);
            localLockService.releaseLock(key, lockValue);
            return;
        }

        try {
            distributedLockService.releaseReentrantLock(key, lockValue);
            markRedisAvailable();
        } catch (RedisConnectionFailureException e) {
            markRedisUnavailable();
            log.error("Redis connection failed, releasing local lock: key={}", key, e);
            localLockService.releaseLock(key, lockValue);
        }
    }

    /**
     * Redis 가용성 확인 (Circuit Breaker)
     *
     * @return Redis 사용 가능 여부
     */
    private boolean isRedisAvailable() {
        if (!redisAvailable) {
            // Circuit Open 상태: 일정 시간 후 Half-Open으로 전환
            long timeSinceFailure = System.currentTimeMillis() - lastFailureTime;
            if (timeSinceFailure > CIRCUIT_OPEN_DURATION) {
                log.info("Circuit Breaker transitioning to Half-Open state");
                redisAvailable = true; // Half-Open: 한 번 시도
            }
        }
        return redisAvailable;
    }

    /**
     * Redis 사용 가능으로 표시
     */
    private void markRedisAvailable() {
        if (!redisAvailable) {
            log.info("Circuit Breaker closed: Redis is available again");
            redisAvailable = true;
        }
    }

    /**
     * Redis 사용 불가로 표시
     */
    private void markRedisUnavailable() {
        if (redisAvailable) {
            log.error("Circuit Breaker opened: Redis is unavailable");
            redisAvailable = false;
            lastFailureTime = System.currentTimeMillis();
        }
    }

    /**
     * Circuit Breaker 상태 확인
     *
     * @return Redis 가용성 상태
     */
    public boolean isCircuitClosed() {
        return redisAvailable;
    }
}
