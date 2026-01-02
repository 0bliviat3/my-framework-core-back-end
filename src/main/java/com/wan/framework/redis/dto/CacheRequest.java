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
public class CacheRequest {
    private String key;
    private Object value;
    private Long ttlSeconds;  // Optional: TTL in seconds
}
