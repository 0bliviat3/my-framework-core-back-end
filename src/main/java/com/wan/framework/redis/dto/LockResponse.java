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
public class LockResponse {
    private String lockKey;
    private String lockValue;  // Lock owner identifier
    private boolean acquired;
    private Long ttl;  // Remaining TTL in seconds
}
