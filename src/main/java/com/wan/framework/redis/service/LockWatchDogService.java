package com.wan.framework.redis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lock Watch Dog 서비스
 * - 활성 락의 TTL 자동 연장
 * - 작업이 완료되지 않은 락의 만료 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LockWatchDogService {

    private final DistributedLockService distributedLockService;

    // 활성 락 추적 (key -> lockValue)
    private final Map<String, String> activeLocks = new ConcurrentHashMap<>();

    // Watch Dog 활성화 여부 (key -> enabled)
    private final Map<String, Boolean> watchDogEnabled = new ConcurrentHashMap<>();

    /**
     * Lock Watch Dog 등록
     *
     * @param key       락 키
     * @param lockValue 락 소유자 식별 값
     */
    public void registerLock(String key, String lockValue) {
        activeLocks.put(key, lockValue);
        watchDogEnabled.put(key, true);
        log.debug("Lock registered for Watch Dog: key={}", key);
    }

    /**
     * Lock Watch Dog 해제
     *
     * @param key 락 키
     */
    public void unregisterLock(String key) {
        activeLocks.remove(key);
        watchDogEnabled.remove(key);
        log.debug("Lock unregistered from Watch Dog: key={}", key);
    }

    /**
     * 특정 락 Watch Dog 중지 (TTL 자동 연장 중지)
     *
     * @param key 락 키
     */
    public void disableWatchDog(String key) {
        watchDogEnabled.put(key, false);
        log.debug("Watch Dog disabled for lock: key={}", key);
    }

    /**
     * 특정 락 Watch Dog 재개
     *
     * @param key 락 키
     */
    public void enableWatchDog(String key) {
        if (activeLocks.containsKey(key)) {
            watchDogEnabled.put(key, true);
            log.debug("Watch Dog enabled for lock: key={}", key);
        }
    }

    /**
     * Watch Dog 스케줄러 (10초마다 실행)
     * 활성 락의 TTL을 자동으로 연장
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 10000)
    public void renewActiveLocks() {
        if (activeLocks.isEmpty()) {
            return;
        }

        log.debug("Watch Dog running, active locks: {}", activeLocks.size());

        for (Map.Entry<String, String> entry : activeLocks.entrySet()) {
            String key = entry.getKey();
            String lockValue = entry.getValue();

            // Watch Dog가 비활성화된 락은 스킵
            if (Boolean.FALSE.equals(watchDogEnabled.get(key))) {
                continue;
            }

            try {
                // 락이 아직 소유 중인지 확인
                if (distributedLockService.isLockOwner(key, lockValue)) {
                    // TTL 30초 연장
                    distributedLockService.extendLock(key, lockValue, 30);
                    log.debug("Watch Dog extended lock TTL: key={}, ttl=30s", key);
                } else {
                    // 락을 더 이상 소유하지 않으면 Watch Dog에서 제거
                    unregisterLock(key);
                    log.warn("Lock not owned anymore, removed from Watch Dog: key={}", key);
                }
            } catch (Exception e) {
                log.error("Failed to extend lock in Watch Dog: key={}", key, e);
                // 에러 발생 시에도 Watch Dog는 계속 실행
            }
        }
    }

    /**
     * 모든 활성 락 정보 조회
     *
     * @return 활성 락 맵 (key -> lockValue)
     */
    public Map<String, String> getActiveLocks() {
        return new ConcurrentHashMap<>(activeLocks);
    }

    /**
     * 활성 락 개수 조회
     *
     * @return 활성 락 개수
     */
    public int getActiveLockCount() {
        return activeLocks.size();
    }
}
