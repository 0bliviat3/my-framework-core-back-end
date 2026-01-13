package com.wan.framework.redis.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Redis 설정
 * - Spring Boot 자동 설정 활용
 * - RedisConnectionFactory, StringRedisTemplate은 자동 생성
 * - JSON 직렬화를 위한 RedisTemplate만 추가 정의
 * - Lock Watch Dog를 위한 스케줄링 활성화
 */
@Slf4j
@Configuration
@EnableScheduling
@AllArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    /**
     * JSON 직렬화를 위한 RedisTemplate
     * - Key: String
     * - Value: JSON (GenericJackson2JsonRedisSerializer)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer: String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);


        // ObjectMapper 설정
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Value Serializer: JSON

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        log.info("RedisTemplate configured with JSON serialization");
        return template;
    }

    /**
     * Redis Pub/Sub 메시지 리스너 컨테이너
     * - 캐시 동기화를 위한 Pub/Sub 지원
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        log.info("RedisMessageListenerContainer configured for Pub/Sub");
        return container;
    }
}
