package com.wan.framework.redis.service;

import com.wan.framework.redis.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.wan.framework.redis.constant.RedisExceptionMessage.*;

/**
 * Redis 캐시 서비스
 * - Key-Value 캐시 관리
 * - TTL 기반 자동 만료
 * - Hash, Set, List 자료구조 지원
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // ==================== String Operations ====================

    /**
     * 캐시 저장
     *
     * @param key   키
     * @param value 값
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Cache set: key={}", key);
        } catch (Exception e) {
            log.error("Failed to set cache: key={}", key, e);
            throw new RedisException(CACHE_SET_FAILED, e);
        }
    }

    /**
     * 캐시 저장 (TTL 포함)
     *
     * @param key        키
     * @param value      값
     * @param ttlSeconds TTL (초)
     */
    public void set(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            log.debug("Cache set with TTL: key={}, ttl={}s", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Failed to set cache with TTL: key={}", key, e);
            throw new RedisException(CACHE_SET_FAILED, e);
        }
    }

    /**
     * 캐시 조회
     *
     * @param key 키
     * @return 값 (없으면 null)
     */
    public Object get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Cache get: key={}, found={}", key, value != null);
            return value;
        } catch (Exception e) {
            log.error("Failed to get cache: key={}", key, e);
            throw new RedisException(CACHE_GET_FAILED, e);
        }
    }

    /**
     * 캐시 조회 (타입 지정)
     *
     * @param key   키
     * @param clazz 타입
     * @return 값 (없으면 null)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 캐시 삭제
     *
     * @param key 키
     * @return 삭제 성공 여부
     */
    public boolean delete(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            log.debug("Cache delete: key={}, result={}", key, result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to delete cache: key={}", key, e);
            throw new RedisException(CACHE_DELETE_FAILED, e);
        }
    }

    /**
     * 캐시 다중 삭제
     *
     * @param keys 키 목록
     * @return 삭제된 개수
     */
    public long delete(Collection<String> keys) {
        try {
            Long count = redisTemplate.delete(keys);
            log.debug("Cache bulk delete: count={}", count);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Failed to bulk delete cache", e);
            throw new RedisException(CACHE_DELETE_FAILED, e);
        }
    }

    /**
     * 캐시 존재 여부 확인
     *
     * @param key 키
     * @return 존재 여부
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * TTL 조회 (초)
     *
     * @param key 키
     * @return TTL (초), 없으면 -2, 만료 없으면 -1
     */
    public long getTTL(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2;
    }

    /**
     * TTL 설정
     *
     * @param key        키
     * @param ttlSeconds TTL (초)
     * @return 설정 성공 여부
     */
    public boolean setTTL(String key, long ttlSeconds) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds)));
    }

    /**
     * 패턴 매칭 키 조회
     *
     * @param pattern 패턴 (예: "USER:*")
     * @return 키 Set
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    // ==================== Hash Operations ====================

    /**
     * Hash 필드 저장
     *
     * @param key   키
     * @param field 필드
     * @param value 값
     */
    public void hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            log.debug("Hash set: key={}, field={}", key, field);
        } catch (Exception e) {
            log.error("Failed to set hash: key={}, field={}", key, field, e);
            throw new RedisException(CACHE_SET_FAILED, e);
        }
    }

    /**
     * Hash 필드 조회
     *
     * @param key   키
     * @param field 필드
     * @return 값 (없으면 null)
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Failed to get hash: key={}, field={}", key, field, e);
            throw new RedisException(CACHE_GET_FAILED, e);
        }
    }

    /**
     * Hash 전체 조회
     *
     * @param key 키
     * @return Hash Map
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Failed to get all hash: key={}", key, e);
            throw new RedisException(CACHE_GET_FAILED, e);
        }
    }

    /**
     * Hash 필드 삭제
     *
     * @param key    키
     * @param fields 필드 목록
     * @return 삭제된 개수
     */
    public long hDelete(String key, Object... fields) {
        try {
            Long count = redisTemplate.opsForHash().delete(key, fields);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Failed to delete hash fields: key={}", key, e);
            throw new RedisException(CACHE_DELETE_FAILED, e);
        }
    }

    /**
     * Hash 필드 존재 여부
     *
     * @param key   키
     * @param field 필드
     * @return 존재 여부
     */
    public boolean hExists(String key, String field) {
        return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(key, field));
    }

    // ==================== Set Operations ====================

    /**
     * Set에 값 추가
     *
     * @param key    키
     * @param values 값 목록
     * @return 추가된 개수
     */
    public long sAdd(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Failed to add to set: key={}", key, e);
            throw new RedisException(CACHE_SET_FAILED, e);
        }
    }

    /**
     * Set에서 값 제거
     *
     * @param key    키
     * @param values 값 목록
     * @return 제거된 개수
     */
    public long sRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Failed to remove from set: key={}", key, e);
            throw new RedisException(CACHE_DELETE_FAILED, e);
        }
    }

    /**
     * Set 멤버 조회
     *
     * @param key 키
     * @return Set 멤버
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Failed to get set members: key={}", key, e);
            throw new RedisException(CACHE_GET_FAILED, e);
        }
    }

    /**
     * Set 멤버 존재 여부
     *
     * @param key   키
     * @param value 값
     * @return 존재 여부
     */
    public boolean sIsMember(String key, Object value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    // ==================== Utility ====================

    /**
     * 모든 캐시 삭제 (주의: 운영 환경에서 사용 금지)
     */
    public void flushAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            log.warn("All cache flushed - USE WITH CAUTION!");
        } catch (Exception e) {
            log.error("Failed to flush all cache", e);
            throw new RedisException(OPERATION_FAILED, e);
        }
    }
}
