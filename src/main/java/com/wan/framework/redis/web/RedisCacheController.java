package com.wan.framework.redis.web;

import com.wan.framework.redis.dto.CacheRequest;
import com.wan.framework.redis.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * Redis 캐시 관리 Controller
 */
@Slf4j
@RestController
@RequestMapping("/redis/cache")
@RequiredArgsConstructor
public class RedisCacheController {

    private final RedisCacheService cacheService;

    /**
     * 캐시 저장
     */
    @PostMapping
    public ResponseEntity<Void> setCache(@RequestBody CacheRequest request) {
        if (request.getTtlSeconds() != null) {
            cacheService.set(request.getKey(), request.getValue(), request.getTtlSeconds());
        } else {
            cacheService.set(request.getKey(), request.getValue());
        }
        return ResponseEntity.ok().build();
    }

    /**
     * 캐시 조회
     */
    @GetMapping("/{key}")
    public ResponseEntity<Object> getCache(@PathVariable String key) {
        Object value = cacheService.get(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }

    /**
     * 캐시 삭제
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteCache(@PathVariable String key) {
        cacheService.delete(key);
        return ResponseEntity.ok().build();
    }

    /**
     * 캐시 존재 여부 확인
     */
    @GetMapping("/{key}/exists")
    public ResponseEntity<Boolean> checkCacheExists(@PathVariable String key) {
        boolean exists = cacheService.exists(key);
        return ResponseEntity.ok(exists);
    }

    /**
     * 캐시 TTL 조회
     */
    @GetMapping("/{key}/ttl")
    public ResponseEntity<Long> getCacheTTL(@PathVariable String key) {
        long ttl = cacheService.getTTL(key);
        return ResponseEntity.ok(ttl);
    }

    /**
     * 캐시 TTL 설정
     */
    @PutMapping("/{key}/ttl")
    public ResponseEntity<Boolean> setCacheTTL(@PathVariable String key, @RequestParam long ttlSeconds) {
        boolean result = cacheService.setTTL(key, ttlSeconds);
        return ResponseEntity.ok(result);
    }

    /**
     * 패턴 매칭 키 조회
     */
    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getKeys(@RequestParam String pattern) {
        Set<String> keys = cacheService.keys(pattern);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hash 필드 저장
     */
    @PostMapping("/hash/{key}")
    public ResponseEntity<Void> setHashField(
            @PathVariable String key,
            @RequestParam String field,
            @RequestBody Object value) {

        cacheService.hSet(key, field, value);
        return ResponseEntity.ok().build();
    }

    /**
     * Hash 필드 조회
     */
    @GetMapping("/hash/{key}/{field}")
    public ResponseEntity<Object> getHashField(@PathVariable String key, @PathVariable String field) {
        Object value = cacheService.hGet(key, field);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }

    /**
     * Hash 전체 조회
     */
    @GetMapping("/hash/{key}")
    public ResponseEntity<Map<Object, Object>> getAllHash(@PathVariable String key) {
        Map<Object, Object> hash = cacheService.hGetAll(key);
        return ResponseEntity.ok(hash);
    }

    /**
     * Hash 필드 삭제
     */
    @DeleteMapping("/hash/{key}")
    public ResponseEntity<Long> deleteHashField(@PathVariable String key, @RequestParam String... fields) {
        long count = cacheService.hDelete(key, (Object[]) fields);
        return ResponseEntity.ok(count);
    }

    /**
     * Set에 값 추가
     */
    @PostMapping("/set/{key}")
    public ResponseEntity<Long> addToSet(@PathVariable String key, @RequestBody Object[] values) {
        long count = cacheService.sAdd(key, values);
        return ResponseEntity.ok(count);
    }

    /**
     * Set 멤버 조회
     */
    @GetMapping("/set/{key}")
    public ResponseEntity<Set<Object>> getSetMembers(@PathVariable String key) {
        Set<Object> members = cacheService.sMembers(key);
        return ResponseEntity.ok(members);
    }

    /**
     * Set에서 값 제거
     */
    @DeleteMapping("/set/{key}")
    public ResponseEntity<Long> removeFromSet(@PathVariable String key, @RequestBody Object[] values) {
        long count = cacheService.sRemove(key, values);
        return ResponseEntity.ok(count);
    }

    /**
     * Set 멤버 존재 여부
     */
    @GetMapping("/set/{key}/member")
    public ResponseEntity<Boolean> checkSetMember(@PathVariable String key, @RequestBody Object value) {
        boolean isMember = cacheService.sIsMember(key, value);
        return ResponseEntity.ok(isMember);
    }
}
