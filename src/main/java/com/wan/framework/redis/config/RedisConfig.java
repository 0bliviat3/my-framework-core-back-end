package com.wan.framework.redis.config;

import com.wan.framework.redis.constant.RedisMode;
import com.wan.framework.redis.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static com.wan.framework.redis.constant.RedisExceptionMessage.INVALID_CONFIGURATION;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    /**
     * Redis 연결 팩토리 생성
     * Redis Mode에 따라 Standalone / Sentinel / Cluster 구성
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(redisProperties.getTimeout()))
                .build();

        RedisMode mode = redisProperties.getMode();
        log.info("Redis Mode: {}", mode);

        return switch (mode) {
            case STANDALONE -> createStandaloneConnectionFactory(clientConfig);
            case SENTINEL -> createSentinelConnectionFactory(clientConfig);
            case CLUSTER -> createClusterConnectionFactory(clientConfig);
        };
    }

    /**
     * Standalone 연결 팩토리 생성
     */
    private LettuceConnectionFactory createStandaloneConnectionFactory(
            LettucePoolingClientConfiguration clientConfig) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
        config.setDatabase(redisProperties.getDatabase());

        if (redisProperties.getPassword() != null && !redisProperties.getPassword().isEmpty()) {
            config.setPassword(redisProperties.getPassword());
        }

        log.info("Creating Standalone Redis Connection: {}:{}",
                redisProperties.getHost(), redisProperties.getPort());

        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * Sentinel 연결 팩토리 생성
     */
    private LettuceConnectionFactory createSentinelConnectionFactory(
            LettucePoolingClientConfiguration clientConfig) {

        RedisProperties.Sentinel sentinel = redisProperties.getSentinel();

        if (sentinel.getMaster() == null || sentinel.getNodes() == null || sentinel.getNodes().isEmpty()) {
            throw new RedisException(INVALID_CONFIGURATION);
        }

        RedisSentinelConfiguration config = new RedisSentinelConfiguration()
                .master(sentinel.getMaster());

        // Sentinel 노드 추가
        Set<RedisNode> sentinelNodes = new HashSet<>();
        for (String node : sentinel.getNodes()) {
            String[] parts = node.split(":");
            if (parts.length != 2) {
                throw new RedisException(INVALID_CONFIGURATION);
            }
            sentinelNodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
        }
        config.setSentinels(sentinelNodes);

        if (sentinel.getPassword() != null && !sentinel.getPassword().isEmpty()) {
            config.setPassword(sentinel.getPassword());
        }

        config.setDatabase(redisProperties.getDatabase());

        log.info("Creating Sentinel Redis Connection: master={}, sentinels={}",
                sentinel.getMaster(), sentinel.getNodes());

        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * Cluster 연결 팩토리 생성
     */
    private LettuceConnectionFactory createClusterConnectionFactory(
            LettucePoolingClientConfiguration clientConfig) {

        RedisProperties.Cluster cluster = redisProperties.getCluster();

        if (cluster.getNodes() == null || cluster.getNodes().isEmpty()) {
            throw new RedisException(INVALID_CONFIGURATION);
        }

        RedisClusterConfiguration config = new RedisClusterConfiguration(cluster.getNodes());
        config.setMaxRedirects(cluster.getMaxRedirects());

        if (cluster.getPassword() != null && !cluster.getPassword().isEmpty()) {
            config.setPassword(cluster.getPassword());
        }

        log.info("Creating Cluster Redis Connection: nodes={}", cluster.getNodes());

        return new LettuceConnectionFactory(config, clientConfig);
    }

    /**
     * RedisTemplate 설정
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

        // Value Serializer: JSON
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * String 전용 RedisTemplate
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
