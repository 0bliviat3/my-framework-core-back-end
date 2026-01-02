package com.wan.framework.redis.web;

import com.wan.framework.redis.dto.LockRequest;
import com.wan.framework.redis.dto.LockResponse;
import com.wan.framework.redis.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Redis 분산 락 관리 Controller
 */
@Slf4j
@RestController
@RequestMapping("/redis/locks")
@RequiredArgsConstructor
public class RedisLockController {

    private final DistributedLockService lockService;

    /**
     * 분산 락 획득
     */
    @PostMapping("/acquire")
    public ResponseEntity<LockResponse> acquireLock(@RequestBody LockRequest request) {
        String lockValue;

        if (request.getWaitTimeMillis() != null && request.getRetryInterval() != null) {
            // 타임아웃 기반 락 획득
            lockValue = lockService.acquireLockWithTimeout(
                    request.getKey(),
                    request.getTtlSeconds(),
                    request.getWaitTimeMillis(),
                    request.getRetryInterval()
            );
        } else {
            // 즉시 락 획득
            lockValue = lockService.acquireLock(request.getKey(), request.getTtlSeconds());
        }

        long ttl = lockService.getLockTTL(request.getKey());

        LockResponse response = LockResponse.builder()
                .lockKey(request.getKey())
                .lockValue(lockValue)
                .acquired(true)
                .ttl(ttl)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 분산 락 해제
     */
    @DeleteMapping("/release")
    public ResponseEntity<Void> releaseLock(@RequestParam String key, @RequestParam String lockValue) {
        lockService.releaseLock(key, lockValue);
        return ResponseEntity.ok().build();
    }

    /**
     * 분산 락 연장
     */
    @PutMapping("/extend")
    public ResponseEntity<LockResponse> extendLock(
            @RequestParam String key,
            @RequestParam String lockValue,
            @RequestParam long ttlSeconds) {

        lockService.extendLock(key, lockValue, ttlSeconds);
        long ttl = lockService.getLockTTL(key);

        LockResponse response = LockResponse.builder()
                .lockKey(key)
                .lockValue(lockValue)
                .acquired(true)
                .ttl(ttl)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 락 존재 여부 확인
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkLockExists(@RequestParam String key) {
        boolean exists = lockService.isLockExists(key);
        return ResponseEntity.ok(exists);
    }

    /**
     * 락 소유 여부 확인
     */
    @GetMapping("/owner")
    public ResponseEntity<Boolean> checkLockOwner(
            @RequestParam String key,
            @RequestParam String lockValue) {

        boolean isOwner = lockService.isLockOwner(key, lockValue);
        return ResponseEntity.ok(isOwner);
    }

    /**
     * 락 TTL 조회
     */
    @GetMapping("/ttl")
    public ResponseEntity<Long> getLockTTL(@RequestParam String key) {
        long ttl = lockService.getLockTTL(key);
        return ResponseEntity.ok(ttl);
    }
}
