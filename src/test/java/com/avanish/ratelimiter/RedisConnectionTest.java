package com.avanish.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedisConnectionTest {

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Test
    void redisRespondsToPing() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            assertEquals("PONG", connection.ping());
        }
    }
}