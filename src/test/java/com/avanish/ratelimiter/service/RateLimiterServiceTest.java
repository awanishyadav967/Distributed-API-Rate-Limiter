package com.avanish.ratelimiter.service;

import com.avanish.ratelimiter.config.RateLimitProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimiterServiceTest {
    @Test
    void differentClientsHaveSeparateBuckets() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setCapacity(1);
        properties.setRefillTokensPerSecond(1);

        RateLimiterService service = new RateLimiterService(properties);

        assertTrue(service.isAllowed("client-A"));
        assertTrue(service.isAllowed("client-B"));
    }
}
