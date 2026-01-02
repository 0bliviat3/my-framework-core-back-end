package com.wan.framework.redis.config;

import com.wan.framework.redis.constant.RedisMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    /**
     * Redis 구성 모드 (STANDALONE, SENTINEL, CLUSTER)
     */
    private RedisMode mode = RedisMode.STANDALONE;

    /**
     * Standalone 설정
     */
    private String host = "localhost";
    private int port = 6379;
    private String password;
    private int database = 0;

    /**
     * Sentinel 설정
     */
    private Sentinel sentinel = new Sentinel();

    /**
     * Cluster 설정
     */
    private Cluster cluster = new Cluster();

    /**
     * 공통 설정
     */
    private int timeout = 3000; // 연결 타임아웃 (ms)
    private Pool pool = new Pool();

    @Getter
    @Setter
    public static class Sentinel {
        private String master; // Master 이름
        private List<String> nodes; // Sentinel 노드 목록 (host:port)
        private String password;
    }

    @Getter
    @Setter
    public static class Cluster {
        private List<String> nodes; // Cluster 노드 목록 (host:port)
        private int maxRedirects = 3;
        private String password;
    }

    @Getter
    @Setter
    public static class Pool {
        private int maxActive = 8;
        private int maxIdle = 8;
        private int minIdle = 0;
        private long maxWait = -1; // -1 = 무제한 대기
    }
}
