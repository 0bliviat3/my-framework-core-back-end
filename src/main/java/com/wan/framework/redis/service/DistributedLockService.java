package com.wan.framework.redis.service;

import com.wan.framework.redis.dto.LockInfo;
import com.wan.framework.redis.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.wan.framework.redis.constant.RedisExceptionMessage.*;

/**
 * 분산 락 서비스
 * - Redis SET NX EX 기반 락 획득
 * - Lua Script 기반 안전한 락 해제
 * - TTL 필수, 소유자 식별 값 포함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedisTemplate<String, String> stringRedisTemplate;

    private static final String LOCK_PREFIX = "LOCK:";
    private static final String SERVER_ID = getServerId();

    // 재진입 락을 위한 ThreadLocal 저장소
    private final ThreadLocal<Map<String, LockInfo>> threadLocks =
            ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * 분산 락 획득
     *
     * @param key        락 키
     * @param ttlSeconds TTL (초)
     * @return 락 소유자 식별 값 (UUID)
     */
    public String acquireLock(String key, long ttlSeconds) {
        String lockKey = LOCK_PREFIX + key;
        String lockValue = generateLockValue();

        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(ttlSeconds));

        if (Boolean.TRUE.equals(success)) {
            log.debug("Lock acquired: key={}, value={}, ttl={}s", lockKey, lockValue, ttlSeconds);
            return lockValue;
        }

        log.warn("Failed to acquire lock: key={}", lockKey);
        throw new RedisException(LOCK_ACQUIRE_FAILED);
    }

    /**
     * 분산 락 획득 시도 (타임아웃 포함)
     *
     * @param key            락 키
     * @param ttlSeconds     TTL (초)
     * @param waitTimeMillis 대기 시간 (밀리초)
     * @param retryInterval  재시도 간격 (밀리초)
     * @return 락 소유자 식별 값 (UUID)
     */
    public String acquireLockWithTimeout(String key, long ttlSeconds, long waitTimeMillis, long retryInterval) {
        long startTime = System.currentTimeMillis();
        String lockKey = LOCK_PREFIX + key;

        while (System.currentTimeMillis() - startTime < waitTimeMillis) {
            try {
                return acquireLock(key, ttlSeconds);
            } catch (RedisException e) {
                // 락 획득 실패, 재시도
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RedisException(LOCK_TIMEOUT, ie);
                }
            }
        }

        log.error("Lock acquisition timeout: key={}, waitTime={}ms", lockKey, waitTimeMillis);
        throw new RedisException(LOCK_TIMEOUT);
    }

    /**
     * 분산 락 해제 (Lua Script 기반 안전 해제)
     *
     * @param key       락 키
     * @param lockValue 락 소유자 식별 값
     */
    public void releaseLock(String key, String lockValue) {
        String lockKey = LOCK_PREFIX + key;

        // Lua Script: 소유자 검증 후 삭제
        String luaScript = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = stringRedisTemplate.execute(script, Collections.singletonList(lockKey), lockValue);

        if (result != null && result == 1) {
            log.debug("Lock released: key={}, value={}", lockKey, lockValue);
        } else {
            log.warn("Failed to release lock (not owner or expired): key={}, value={}", lockKey, lockValue);
            throw new RedisException(LOCK_NOT_OWNED);
        }
    }

    /**
     * 분산 락 연장
     *
     * @param key        락 키
     * @param lockValue  락 소유자 식별 값
     * @param ttlSeconds 연장할 TTL (초)
     */
    public void extendLock(String key, String lockValue, long ttlSeconds) {
        String lockKey = LOCK_PREFIX + key;

        // Lua Script: 소유자 검증 후 TTL 연장
        String luaScript = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('expire', KEYS[1], ARGV[2])
                else
                    return 0
                end
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = stringRedisTemplate.execute(script,
                Collections.singletonList(lockKey), lockValue, String.valueOf(ttlSeconds));

        if (result != null && result == 1) {
            log.debug("Lock extended: key={}, value={}, ttl={}s", lockKey, lockValue, ttlSeconds);
        } else {
            log.warn("Failed to extend lock (not owner or expired): key={}, value={}", lockKey, lockValue);
            throw new RedisException(LOCK_NOT_OWNED);
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
        String lockKey = LOCK_PREFIX + key;
        String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
        return lockValue.equals(currentValue);
    }

    /**
     * 락 존재 여부 확인
     *
     * @param key 락 키
     * @return 존재 여부
     */
    public boolean isLockExists(String key) {
        String lockKey = LOCK_PREFIX + key;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey));
    }

    /**
     * 락 남은 TTL 조회 (초)
     *
     * @param key 락 키
     * @return 남은 TTL (초), 없으면 -2, 만료 없으면 -1
     */
    public long getLockTTL(String key) {
        String lockKey = LOCK_PREFIX + key;
        Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2;
    }

    /**
     * 락 소유자 식별 값 생성
     * 형식: {uuid}:{serverId}
     */
    private String generateLockValue() {
        return UUID.randomUUID().toString() + ":" + SERVER_ID;
    }

    /**
     * 재진입 락 획득
     *
     * @param key        락 키
     * @param ttlSeconds TTL (초)
     * @return 락 소유자 식별 값 (UUID)
     */
    public String acquireReentrantLock(String key, long ttlSeconds) {
        String threadKey = Thread.currentThread().getId() + ":" + key;
        Map<String, LockInfo> locks = threadLocks.get();
        LockInfo lockInfo = locks.get(threadKey);

        if (lockInfo != null) {
            // 재진입: 카운트만 증가
            lockInfo.incrementCount();
            log.debug("Reentrant lock acquired (count={}): key={}, value={}",
                    lockInfo.getCount(), key, lockInfo.getLockValue());
            return lockInfo.getLockValue();
        }

        // 최초 획득: Redis에서 락 획득
        String lockValue = acquireLock(key, ttlSeconds);
        locks.put(threadKey, new LockInfo(lockValue, 1));
        log.debug("Reentrant lock acquired (initial): key={}, value={}", key, lockValue);
        return lockValue;
    }

    /**
     * 재진입 락 획득 시도 (타임아웃 포함)
     *
     * @param key            락 키
     * @param ttlSeconds     TTL (초)
     * @param waitTimeMillis 대기 시간 (밀리초)
     * @param retryInterval  재시도 간격 (밀리초)
     * @return 락 소유자 식별 값 (UUID)
     */
    public String acquireReentrantLockWithTimeout(String key, long ttlSeconds,
                                                   long waitTimeMillis, long retryInterval) {
        String threadKey = Thread.currentThread().getId() + ":" + key;
        Map<String, LockInfo> locks = threadLocks.get();
        LockInfo lockInfo = locks.get(threadKey);

        if (lockInfo != null) {
            // 재진입: 카운트만 증가
            lockInfo.incrementCount();
            log.debug("Reentrant lock acquired with timeout (count={}): key={}, value={}",
                    lockInfo.getCount(), key, lockInfo.getLockValue());
            return lockInfo.getLockValue();
        }

        // 최초 획득: 타임아웃 포함하여 락 획득
        String lockValue = acquireLockWithTimeout(key, ttlSeconds, waitTimeMillis, retryInterval);
        locks.put(threadKey, new LockInfo(lockValue, 1));
        log.debug("Reentrant lock acquired with timeout (initial): key={}, value={}", key, lockValue);
        return lockValue;
    }

    /**
     * 재진입 락 해제
     *
     * @param key       락 키
     * @param lockValue 락 소유자 식별 값
     */
    public void releaseReentrantLock(String key, String lockValue) {
        String threadKey = Thread.currentThread().getId() + ":" + key;
        Map<String, LockInfo> locks = threadLocks.get();
        LockInfo lockInfo = locks.get(threadKey);

        if (lockInfo == null) {
            log.warn("No reentrant lock found to release: key={}", key);
            throw new RedisException(LOCK_NOT_OWNED);
        }

        if (!lockInfo.getLockValue().equals(lockValue)) {
            log.warn("Lock value mismatch: key={}, expected={}, actual={}",
                    key, lockInfo.getLockValue(), lockValue);
            throw new RedisException(LOCK_NOT_OWNED);
        }

        lockInfo.decrementCount();
        if (lockInfo.getCount() > 0) {
            // 아직 재진입 중
            log.debug("Reentrant lock released (count={}): key={}", lockInfo.getCount(), key);
            return;
        }

        // 모든 재진입이 해제됨: Redis에서 락 해제
        locks.remove(threadKey);
        releaseLock(key, lockValue);
        log.debug("Reentrant lock fully released: key={}", key);
    }

    /**
     * ThreadLocal 정리 (메모리 누수 방지)
     * 작업 완료 후 반드시 호출해야 함
     */
    public void cleanupThreadLocals() {
        threadLocks.remove();
        log.debug("ThreadLocal locks cleaned up for thread: {}", Thread.currentThread().getId());
    }

    /**
     * 서버 ID 생성 (호스트명 사용)
     */
    private static String getServerId() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-server";
        }
    }
}
