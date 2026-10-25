package com.avanish.ratelimiter.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "rate-limiter.capacity=2",
        "rate-limiter.refill-tokens-per-second=1"
})
class RedisRateLimiterServiceTest {

    @Autowired
    private RedisRateLimiterService service;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    void firstRequestCreatesBucketAndConsumesOneToken() {
        String clientId = "test-" + UUID.randomUUID();
        String key = "rate-limiter:" + clientId;

        try {
            assertTrue(service.isAllowed(clientId));

            assertEquals(
                    "1",
                    redis.opsForHash().get(key, "tokens")
            );
        } finally {
            redis.delete(key);
        }
    }

    @Test
    void emptyBucketWithFutureTimestampIsRejected() {
        String clientId = "test-" + UUID.randomUUID();
        String key = "rate-limiter:" + clientId;

        try {
            redis.opsForHash().putAll(key, Map.of(
                    "tokens", "0",
                    "last_refill_ms", "9999999999999"
            ));

            assertFalse(service.isAllowed(clientId));
        } finally {
            redis.delete(key);
        }
    }
    @Test
    void oldEmptyBucketRefillsOnlyToCapacity() {
        String clientId = "test-" + UUID.randomUUID();
        String key = "rate-limiter:" + clientId;

        try {
            redis.opsForHash().putAll(key, Map.of(
                    "tokens", "0",
                    "last_refill_ms", "0"
            ));

            assertTrue(service.isAllowed(clientId));

            assertEquals(
                    "1",
                    redis.opsForHash().get(key, "tokens")
            );
        } finally {
            redis.delete(key);
        }
    }
}

