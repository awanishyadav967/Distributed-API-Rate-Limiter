package com.avanish.ratelimiter.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "rate-limiter")
@Validated
public class RateLimitProperties {
    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity = 10;

    @Min(value = 1, message = "Refill rate must be at least 1 token per second")
    private int refillTokensPerSecond = 1;

    public int getCapacity() {
        return capacity;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public int getRefillTokensPerSecond() {
        return refillTokensPerSecond;
    }
    public void setRefillTokensPerSecond(int refillTokensPerSecond) {
        this.refillTokensPerSecond = refillTokensPerSecond;
    }

}