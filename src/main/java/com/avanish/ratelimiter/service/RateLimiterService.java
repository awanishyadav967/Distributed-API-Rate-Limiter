package com.avanish.ratelimiter.service;


import com.avanish.ratelimiter.config.RateLimitProperties;
import com.avanish.ratelimiter.model.TokenBucket;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final ConcurrentHashMap<String, TokenBucket> buckets =
            new ConcurrentHashMap<>();

    private final RateLimitProperties properties;
    public RateLimiterService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public boolean isAllowed(String clientId) {
        TokenBucket bucket = buckets.computeIfAbsent(
                clientId,
                key -> new TokenBucket(10, 1)
        );

        return bucket.tryConsume();
    }

    }


