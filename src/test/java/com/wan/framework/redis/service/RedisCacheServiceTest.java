package com.wan.framework.redis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("Redis 캐시 서비스 테스트")
class RedisCacheServiceTest {

    @Autowired
    private RedisCacheService cacheService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // 테스트 전 TEST:* 키 모두 삭제
        redisTemplate.keys("TEST:*").forEach(redisTemplate::delete);
    }

    // ==================== String Operations ====================

    @Test
    @DisplayName("캐시 저장 및 조회")
    void setAndGet() {
        // Given
        String key = "TEST:USER:001";
        String value = "John Doe";

        // When
        cacheService.set(key, value);
        Object result = cacheService.get(key);

        // Then
        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("캐시 저장 (TTL 포함)")
    void setWithTTL() throws InterruptedException {
        // Given
        String key = "TEST:USER:002";
        String value = "Jane Doe";
        long ttl = 2L;

        // When
        cacheService.set(key, value, ttl);

        // Then
        assertThat(cacheService.get(key)).isEqualTo(value);
        assertThat(cacheService.getTTL(key)).isLessThanOrEqualTo(ttl);

        // Wait for expiration
        Thread.sleep(3000);
        assertThat(cacheService.exists(key)).isFalse();
    }

    @Test
    @DisplayName("캐시 삭제")
    void delete() {
        // Given
        String key = "TEST:USER:003";
        String value = "Test Value";
        cacheService.set(key, value);

        // When
        boolean result = cacheService.delete(key);

        // Then
        assertThat(result).isTrue();
        assertThat(cacheService.exists(key)).isFalse();
    }

    @Test
    @DisplayName("캐시 존재 여부 확인")
    void exists() {
        // Given
        String key = "TEST:USER:004";
        String value = "Exists Test";

        // When
        cacheService.set(key, value);

        // Then
        assertThat(cacheService.exists(key)).isTrue();
        assertThat(cacheService.exists("NON_EXISTENT_KEY")).isFalse();
    }

    @Test
    @DisplayName("캐시 TTL 설정")
    void setTTL() {
        // Given
        String key = "TEST:USER:005";
        String value = "TTL Test";
        cacheService.set(key, value);

        // When
        boolean result = cacheService.setTTL(key, 10L);

        // Then
        assertThat(result).isTrue();
        assertThat(cacheService.getTTL(key)).isGreaterThan(0);
    }

    @Test
    @DisplayName("패턴 매칭 키 조회")
    void keys() {
        // Given
        cacheService.set("TEST:PATTERN:001", "value1");
        cacheService.set("TEST:PATTERN:002", "value2");
        cacheService.set("TEST:OTHER:001", "value3");

        // When
        Set<String> keys = cacheService.keys("TEST:PATTERN:*");

        // Then
        assertThat(keys).hasSize(2);
        assertThat(keys).contains("TEST:PATTERN:001", "TEST:PATTERN:002");
    }

    // ==================== Hash Operations ====================

    @Test
    @DisplayName("Hash 필드 저장 및 조회")
    void hSetAndGet() {
        // Given
        String key = "TEST:HASH:001";
        String field = "name";
        String value = "Alice";

        // When
        cacheService.hSet(key, field, value);
        Object result = cacheService.hGet(key, field);

        // Then
        assertThat(result).isEqualTo(value);
    }

    @Test
    @DisplayName("Hash 전체 조회")
    void hGetAll() {
        // Given
        String key = "TEST:HASH:002";
        cacheService.hSet(key, "field1", "value1");
        cacheService.hSet(key, "field2", "value2");
        cacheService.hSet(key, "field3", "value3");

        // When
        Map<Object, Object> result = cacheService.hGetAll(key);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsKeys("field1", "field2", "field3");
    }

    @Test
    @DisplayName("Hash 필드 삭제")
    void hDelete() {
        // Given
        String key = "TEST:HASH:003";
        cacheService.hSet(key, "field1", "value1");
        cacheService.hSet(key, "field2", "value2");

        // When
        long count = cacheService.hDelete(key, "field1");

        // Then
        assertThat(count).isEqualTo(1);
        assertThat(cacheService.hExists(key, "field1")).isFalse();
        assertThat(cacheService.hExists(key, "field2")).isTrue();
    }

    @Test
    @DisplayName("Hash 필드 존재 여부")
    void hExists() {
        // Given
        String key = "TEST:HASH:004";
        cacheService.hSet(key, "existingField", "value");

        // When & Then
        assertThat(cacheService.hExists(key, "existingField")).isTrue();
        assertThat(cacheService.hExists(key, "nonExistentField")).isFalse();
    }

    // ==================== Set Operations ====================

    @Test
    @DisplayName("Set에 값 추가 및 조회")
    void sAddAndMembers() {
        // Given
        String key = "TEST:SET:001";

        // When
        long count = cacheService.sAdd(key, "member1", "member2", "member3");
        Set<Object> members = cacheService.sMembers(key);

        // Then
        assertThat(count).isEqualTo(3);
        assertThat(members).hasSize(3);
        assertThat(members).contains("member1", "member2", "member3");
    }

    @Test
    @DisplayName("Set에서 값 제거")
    void sRemove() {
        // Given
        String key = "TEST:SET:002";
        cacheService.sAdd(key, "member1", "member2", "member3");

        // When
        long count = cacheService.sRemove(key, "member2");
        Set<Object> members = cacheService.sMembers(key);

        // Then
        assertThat(count).isEqualTo(1);
        assertThat(members).hasSize(2);
        assertThat(members).contains("member1", "member3");
        assertThat(members).doesNotContain("member2");
    }

    @Test
    @DisplayName("Set 멤버 존재 여부")
    void sIsMember() {
        // Given
        String key = "TEST:SET:003";
        cacheService.sAdd(key, "member1", "member2");

        // When & Then
        assertThat(cacheService.sIsMember(key, "member1")).isTrue();
        assertThat(cacheService.sIsMember(key, "member3")).isFalse();
    }

    @Test
    @DisplayName("Set 중복 추가")
    void sAddDuplicate() {
        // Given
        String key = "TEST:SET:004";
        cacheService.sAdd(key, "member1");

        // When
        long count = cacheService.sAdd(key, "member1");  // 중복 추가

        // Then
        assertThat(count).isEqualTo(0);  // 중복이므로 추가 안됨
        assertThat(cacheService.sMembers(key)).hasSize(1);
    }
}
