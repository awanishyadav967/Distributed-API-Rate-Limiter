package com.avanish.ratelimiter.service;

import com.avanish.ratelimiter.config.RateLimitProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisRateLimiterService {

    private final StringRedisTemplate redis;
    private final RateLimitProperties properties;
    private final DefaultRedisScript<Long> script;

    public RedisRateLimiterService(
            StringRedisTemplate redis,
            RateLimitProperties properties
    ) {
        this.redis = redis;
        this.properties = properties;

        this.script = new DefaultRedisScript<>();
        this.script.setLocation(
                new ClassPathResource("scripts/token_bucket.lua")
        );
        this.script.setResultType(Long.class);
    }

    public boolean isAllowed(String clientId) {
        Long result = redis.execute(
                script,
                List.of("rate-limiter:" + clientId),
                Integer.toString(properties.getCapacity()),
                Integer.toString(properties.getRefillTokensPerSecond())
        );

        if (result == null) {
            throw new IllegalStateException("Redis script returned no result");
        }

        return result == 1L;
    }
}