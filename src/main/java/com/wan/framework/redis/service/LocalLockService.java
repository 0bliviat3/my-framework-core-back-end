package com.wan.framework.redis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 로컬 락 서비스 (Redis Fallback용)
 * - Redis 장애 시 사용되는 로컬 락
 * - 단일 서버 환경에서만 유효
 * - 주의: 다중 서버 환경에서는 분산 락 기능을 제공하지 않음
 */
@Slf4j
@Service
public class LocalLockService {

    // 로컬 락 저장소 (key -> Lock)
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();

    // 락 소유자 저장소 (key -> lockValue)
    private final Map<String, String> lockOwners = new ConcurrentHashMap<>();

    /**
     * 로컬 락 획득
     *
     * @param key        락 키
     * @param ttlSeconds TTL (초) - 로컬 락에서는 무시됨
     * @return 락 소유자 식별 값
     */
    public String acquireLock(String key, long ttlSeconds) {
        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        String lockValue = UUID.randomUUID().toString();

        lock.lock();
        lockOwners.put(key, lockValue);

        log.debug("[LOCAL FALLBACK] Lock acquired: key={}", key);
        return lockValue;
    }

    /**
     * 로컬 락 획득 시도 (타임아웃 포함)
     *
     * @param key            락 키
     * @param ttlSeconds     TTL (초) - 로컬 락에서는 무시됨
     * @param waitTimeMillis 대기 시간 (밀리초)
     * @param retryInterval  재시도 간격 (밀리초) - 로컬 락에서는 무시됨
     * @return 락 소유자 식별 값
     */
    public String acquireLockWithTimeout(String key, long ttlSeconds,
                                          long waitTimeMillis, long retryInterval) {
        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        String lockValue = UUID.randomUUID().toString();

        try {
            boolean acquired = lock.tryLock(waitTimeMillis, TimeUnit.MILLISECONDS);
            if (acquired) {
                lockOwners.put(key, lockValue);
                log.debug("[LOCAL FALLBACK] Lock acquired with timeout: key={}", key);
                return lockValue;
            } else {
                log.warn("[LOCAL FALLBACK] Failed to acquire lock: key={}", key);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[LOCAL FALLBACK] Lock acquisition interrupted: key={}", key, e);
            return null;
        }
    }

    /**
     * 로컬 락 해제
     *
     * @param key       락 키
     * @param lockValue 락 소유자 식별 값
     */
    public void releaseLock(String key, String lockValue) {
        String currentOwner = lockOwners.get(key);

        if (currentOwner == null || !currentOwner.equals(lockValue)) {
            log.warn("[LOCAL FALLBACK] Not the owner, cannot release lock: key={}", key);
            return;
        }

        Lock lock = locks.get(key);
        if (lock != null) {
            lock.unlock();
            lockOwners.remove(key);
            log.debug("[LOCAL FALLBACK] Lock released: key={}", key);
        }
    }

    /**
     * 락 소유 여부 확인
     *
     * @param key       락 키
     * @param lockValue 락 소유자 식별 값
     * @return 소유 여부
     */
    public boolean isLockOwner(String key, String lockValue) {
        String currentOwner = lockOwners.get(key);
        return currentOwner != null && currentOwner.equals(lockValue);
    }

    /**
     * 모든 락 정리 (테스트용)
     */
    public void clearAllLocks() {
        locks.clear();
        lockOwners.clear();
        log.warn("[LOCAL FALLBACK] All locks cleared");
    }
}
