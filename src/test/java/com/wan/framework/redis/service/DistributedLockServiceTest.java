package com.wan.framework.redis.service;

import com.wan.framework.redis.exception.RedisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("분산 락 서비스 테스트")
class DistributedLockServiceTest {

    @Autowired
    private DistributedLockService lockService;

    @Autowired
    private RedisTemplate<String, String> stringRedisTemplate;

    @BeforeEach
    void setUp() {
        // 테스트 전 LOCK:* 키 모두 삭제
        stringRedisTemplate.keys("LOCK:*").forEach(stringRedisTemplate::delete);
    }

    @Test
    @DisplayName("분산 락 획득 성공")
    void acquireLock_Success() {
        // Given
        String key = "TEST:BATCH:001";
        long ttl = 10L;

        // When
        String lockValue = lockService.acquireLock(key, ttl);

        // Then
        assertThat(lockValue).isNotNull();
        assertThat(lockValue).contains(":");  // UUID:ServerId 형식
        assertThat(lockService.isLockExists(key)).isTrue();
        assertThat(lockService.isLockOwner(key, lockValue)).isTrue();
    }

    @Test
    @DisplayName("분산 락 중복 획득 실패")
    void acquireLock_Duplicate_Fail() {
        // Given
        String key = "TEST:BATCH:002";
        long ttl = 10L;
        String firstLockValue = lockService.acquireLock(key, ttl);

        // When & Then
        assertThat(firstLockValue).isNotNull();
        assertThatThrownBy(() -> lockService.acquireLock(key, ttl))
                .isInstanceOf(RedisException.class)
                .hasMessageContaining("락 획득");
    }

    @Test
    @DisplayName("분산 락 해제 성공")
    void releaseLock_Success() {
        // Given
        String key = "TEST:BATCH:003";
        long ttl = 10L;
        String lockValue = lockService.acquireLock(key, ttl);

        // When
        lockService.releaseLock(key, lockValue);

        // Then
        assertThat(lockService.isLockExists(key)).isFalse();
    }

    @Test
    @DisplayName("분산 락 해제 실패 - 소유자가 아님")
    void releaseLock_NotOwner_Fail() {
        // Given
        String key = "TEST:BATCH:004";
        long ttl = 10L;
        String lockValue = lockService.acquireLock(key, ttl);
        String wrongValue = "wrong-uuid:wrong-server";

        // When & Then
        assertThatThrownBy(() -> lockService.releaseLock(key, wrongValue))
                .isInstanceOf(RedisException.class)
                .hasMessageContaining("소유자");
    }

    @Test
    @DisplayName("분산 락 연장 성공")
    void extendLock_Success() {
        // Given
        String key = "TEST:BATCH:005";
        long initialTtl = 5L;
        long extendedTtl = 20L;
        String lockValue = lockService.acquireLock(key, initialTtl);

        // When
        lockService.extendLock(key, lockValue, extendedTtl);

        // Then
        long ttl = lockService.getLockTTL(key);
        assertThat(ttl).isGreaterThan(initialTtl);
        assertThat(ttl).isLessThanOrEqualTo(extendedTtl);
    }

    @Test
    @DisplayName("분산 락 연장 실패 - 소유자가 아님")
    void extendLock_NotOwner_Fail() {
        // Given
        String key = "TEST:BATCH:006";
        long ttl = 10L;
        String lockValue = lockService.acquireLock(key, ttl);
        String wrongValue = "wrong-uuid:wrong-server";

        // When & Then
        assertThatThrownBy(() -> lockService.extendLock(key, wrongValue, 20L))
                .isInstanceOf(RedisException.class)
                .hasMessageContaining("소유자");
    }

    @Test
    @DisplayName("분산 락 TTL 조회")
    void getLockTTL() {
        // Given
        String key = "TEST:BATCH:007";
        long ttl = 10L;
        lockService.acquireLock(key, ttl);

        // When
        long remainingTtl = lockService.getLockTTL(key);

        // Then
        assertThat(remainingTtl).isGreaterThan(0);
        assertThat(remainingTtl).isLessThanOrEqualTo(ttl);
    }

    @Test
    @DisplayName("분산 락 타임아웃 획득 성공")
    void acquireLockWithTimeout_Success() throws InterruptedException {
        // Given
        String key = "TEST:BATCH:008";
        long ttl = 2L;
        long waitTime = 3000L;
        long retryInterval = 500L;

        // 다른 스레드에서 2초 후 락 해제
        String firstLock = lockService.acquireLock(key, ttl);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                lockService.releaseLock(key, firstLock);
            } catch (Exception ignored) {
            }
        }).start();

        // When
        String lockValue = lockService.acquireLockWithTimeout(key, 10L, waitTime, retryInterval);

        // Then
        assertThat(lockValue).isNotNull();
        assertThat(lockService.isLockOwner(key, lockValue)).isTrue();

        // Cleanup
        lockService.releaseLock(key, lockValue);
    }

    @Test
    @DisplayName("분산 락 타임아웃 획득 실패")
    void acquireLockWithTimeout_Timeout() {
        // Given
        String key = "TEST:BATCH:009";
        long ttl = 100L;  // 긴 TTL
        long waitTime = 1000L;  // 짧은 대기 시간
        long retryInterval = 200L;
        lockService.acquireLock(key, ttl);  // 먼저 락 획득

        // When & Then
        assertThatThrownBy(() ->
                lockService.acquireLockWithTimeout(key, 10L, waitTime, retryInterval))
                .isInstanceOf(RedisException.class)
                .hasMessageContaining("타임아웃");
    }

    @Test
    @DisplayName("분산 락 소유자 확인")
    void isLockOwner() {
        // Given
        String key = "TEST:BATCH:010";
        long ttl = 10L;
        String lockValue = lockService.acquireLock(key, ttl);
        String wrongValue = "wrong-value";

        // When & Then
        assertThat(lockService.isLockOwner(key, lockValue)).isTrue();
        assertThat(lockService.isLockOwner(key, wrongValue)).isFalse();
    }
}
