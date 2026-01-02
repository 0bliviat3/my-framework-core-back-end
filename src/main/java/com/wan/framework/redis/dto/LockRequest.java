package com.wan.framework.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LockRequest {
    private String key;
    private long ttlSeconds;
    private Long waitTimeMillis;  // Optional: for timeout-based lock
    private Long retryInterval;    // Optional: for timeout-based lock
}
