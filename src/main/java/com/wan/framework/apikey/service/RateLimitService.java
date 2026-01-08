package com.wan.framework.apikey.service;

import com.wan.framework.apikey.constant.ApiKeyExceptionMessage;
import com.wan.framework.apikey.exception.ApiKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.wan.framework.apikey.constant.ApiKeyExceptionMessage.RATE_LIMIT_EXCEEDED;

/**
 * Rate Limiting 서비스
 * - Redis Sliding Window 알고리즘 사용
 * - API Key별 요청 제한
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "RATE_LIMIT:API_KEY:";
    private static final int WINDOW_SIZE_SECONDS = 60; // 1분 윈도우
    private static final int MAX_REQUESTS_PER_MINUTE = 100; // 분당 최대 100 요청

    /**
     * Rate Limit 확인 및 요청 기록
     * @param apiKeyId API Key ID
     * @throws ApiKeyException Rate Limit 초과 시
     */
    public void checkRateLimit(Long apiKeyId) {
        String key = RATE_LIMIT_PREFIX + apiKeyId;
        long now = System.currentTimeMillis();
        long windowStart = now - (WINDOW_SIZE_SECONDS * 1000L);

        try {
            // 윈도우 밖의 요청 삭제
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

            // 현재 윈도우 내 요청 수 확인
            Long count = redisTemplate.opsForZSet().zCard(key);

            if (count != null && count >= MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for API Key: {}, count: {}", apiKeyId, count);
                throw new ApiKeyException(RATE_LIMIT_EXCEEDED);
            }

            // 현재 요청 추가
            String member = String.valueOf(now);
            redisTemplate.opsForZSet().add(key, member, now);

            // TTL 설정 (윈도우 크기 + 10초 여유)
            redisTemplate.expire(key, WINDOW_SIZE_SECONDS + 10, TimeUnit.SECONDS);

            log.debug("Rate limit check passed for API Key: {}, current count: {}", apiKeyId, count != null ? count + 1 : 1);

        } catch (ApiKeyException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to check rate limit for API Key: {}", apiKeyId, e);
            // Redis 장애 시에도 요청 허용 (Fail-open 전략)
        }
    }

    /**
     * API Key의 현재 요청 수 조회
     * @param apiKeyId API Key ID
     * @return 현재 윈도우 내 요청 수
     */
    public long getCurrentRequestCount(Long apiKeyId) {
        String key = RATE_LIMIT_PREFIX + apiKeyId;
        long now = System.currentTimeMillis();
        long windowStart = now - (WINDOW_SIZE_SECONDS * 1000L);

        try {
            // 윈도우 밖의 요청 삭제
            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

            // 현재 윈도우 내 요청 수 반환
            Long count = redisTemplate.opsForZSet().zCard(key);
            return count != null ? count : 0;

        } catch (Exception e) {
            log.error("Failed to get current request count for API Key: {}", apiKeyId, e);
            return 0;
        }
    }

    /**
     * API Key의 Rate Limit 초기화
     * @param apiKeyId API Key ID
     */
    public void resetRateLimit(Long apiKeyId) {
        String key = RATE_LIMIT_PREFIX + apiKeyId;
        try {
            redisTemplate.delete(key);
            log.info("Rate limit reset for API Key: {}", apiKeyId);
        } catch (Exception e) {
            log.error("Failed to reset rate limit for API Key: {}", apiKeyId, e);
        }
    }

    /**
     * Rate Limit 설정 정보 반환
     */
    public RateLimitInfo getRateLimitInfo() {
        return new RateLimitInfo(
                WINDOW_SIZE_SECONDS,
                MAX_REQUESTS_PER_MINUTE
        );
    }

    /**
     * Rate Limit 정보 DTO
     */
    public record RateLimitInfo(
            int windowSizeSeconds,
            int maxRequestsPerMinute
    ) {
        public int getRemainingSeconds() {
            return windowSizeSeconds;
        }

        public String getDescription() {
            return String.format("%d requests per %d seconds", maxRequestsPerMinute, windowSizeSeconds);
        }
    }
}
