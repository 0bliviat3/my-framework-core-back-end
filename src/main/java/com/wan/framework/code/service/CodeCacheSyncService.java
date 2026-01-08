package com.wan.framework.code.service;

import com.wan.framework.redis.service.RedisCacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

/**
 * 코드 캐시 동기화 서비스
 * - Redis Pub/Sub을 통한 다중 서버 간 캐시 일관성 보장
 * - 한 서버에서 캐시 무효화 시 모든 서버에 전파
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeCacheSyncService {

    private final RedisMessageListenerContainer listenerContainer;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisCacheService redisCacheService;

    private static final String CHANNEL_GROUP_INVALIDATE = "code:cache:group:invalidate";
    private static final String CHANNEL_ITEM_INVALIDATE = "code:cache:item:invalidate";
    private static final String CHANNEL_ALL_INVALIDATE = "code:cache:all:invalidate";

    private static final String CACHE_PREFIX_GROUP = "CODE:GROUP:";
    private static final String CACHE_PREFIX_ITEMS = "CODE:ITEMS:";
    private static final String CACHE_ALL_GROUPS = "CODE:ALL_GROUPS";

    /**
     * Redis Pub/Sub 리스너 초기화
     */
    @PostConstruct
    public void init() {
        log.info("Initializing CodeCacheSyncService with Redis Pub/Sub");

        // 그룹 캐시 무효화 리스너
        listenerContainer.addMessageListener(
                new MessageListener() {
                    @Override
                    public void onMessage(Message message, byte[] pattern) {
                        String groupCode = new String(message.getBody());
                        log.debug("Received group cache invalidate message: {}", groupCode);

                        // 그룹 캐시 삭제
                        String cacheKey = CACHE_PREFIX_GROUP + groupCode;
                        redisCacheService.delete(cacheKey);

                        // 전체 그룹 목록 캐시도 삭제
                        redisCacheService.delete(CACHE_ALL_GROUPS);

                        log.debug("Group cache invalidated: {}", groupCode);
                    }
                },
                new PatternTopic(CHANNEL_GROUP_INVALIDATE)
        );

        // 항목 캐시 무효화 리스너
        listenerContainer.addMessageListener(
                new MessageListener() {
                    @Override
                    public void onMessage(Message message, byte[] pattern) {
                        String groupCode = new String(message.getBody());
                        log.debug("Received item cache invalidate message: {}", groupCode);

                        // 항목 캐시 삭제
                        String cacheKey = CACHE_PREFIX_ITEMS + groupCode;
                        redisCacheService.delete(cacheKey);

                        log.debug("Item cache invalidated: {}", groupCode);
                    }
                },
                new PatternTopic(CHANNEL_ITEM_INVALIDATE)
        );

        // 전체 캐시 무효화 리스너
        listenerContainer.addMessageListener(
                new MessageListener() {
                    @Override
                    public void onMessage(Message message, byte[] pattern) {
                        log.debug("Received all cache invalidate message");

                        // 전체 그룹 목록 캐시 삭제
                        redisCacheService.delete(CACHE_ALL_GROUPS);

                        log.debug("All groups cache invalidated");
                    }
                },
                new PatternTopic(CHANNEL_ALL_INVALIDATE)
        );

        log.info("CodeCacheSyncService initialized successfully");
    }

    /**
     * 모든 서버에 그룹 캐시 무효화 메시지 전송
     */
    public void invalidateGroupCacheOnAllServers(String groupCode) {
        try {
            log.debug("Publishing group cache invalidate message: {}", groupCode);
            redisTemplate.convertAndSend(CHANNEL_GROUP_INVALIDATE, groupCode);
            log.debug("Group cache invalidate message published: {}", groupCode);
        } catch (Exception e) {
            log.error("Failed to publish group cache invalidate message: {}", groupCode, e);
            // 메시지 발송 실패해도 로컬 캐시는 이미 무효화되었으므로 예외를 던지지 않음
        }
    }

    /**
     * 모든 서버에 항목 캐시 무효화 메시지 전송
     */
    public void invalidateItemCacheOnAllServers(String groupCode) {
        try {
            log.debug("Publishing item cache invalidate message: {}", groupCode);
            redisTemplate.convertAndSend(CHANNEL_ITEM_INVALIDATE, groupCode);
            log.debug("Item cache invalidate message published: {}", groupCode);
        } catch (Exception e) {
            log.error("Failed to publish item cache invalidate message: {}", groupCode, e);
            // 메시지 발송 실패해도 로컬 캐시는 이미 무효화되었으므로 예외를 던지지 않음
        }
    }

    /**
     * 모든 서버에 전체 캐시 무효화 메시지 전송
     */
    public void invalidateAllCacheOnAllServers() {
        try {
            log.debug("Publishing all cache invalidate message");
            redisTemplate.convertAndSend(CHANNEL_ALL_INVALIDATE, "ALL");
            log.debug("All cache invalidate message published");
        } catch (Exception e) {
            log.error("Failed to publish all cache invalidate message", e);
            // 메시지 발송 실패해도 로컬 캐시는 이미 무효화되었으므로 예외를 던지지 않음
        }
    }
}
