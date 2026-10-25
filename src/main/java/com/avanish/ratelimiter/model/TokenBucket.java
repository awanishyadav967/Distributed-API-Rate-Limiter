package com.avanish.ratelimiter.model;

import java.util.function.LongSupplier;

public class TokenBucket {
    private final int capacity;
    private final int refillTokensPerSecond;
    private double tokens;
    private long lastRefillNanos;
    private final LongSupplier clock;


    public TokenBucket(int capacity, int refillTokensPerSecond) {
        this(capacity, refillTokensPerSecond, () -> System.nanoTime());
    }

    TokenBucket(int capacity, int refillTokensPerSecond, LongSupplier clock) {
        if (capacity <= 0 || refillTokensPerSecond <= 0) {
            throw new IllegalArgumentException(
                    "Capacity and refill rate must be positive"
            );
        }

        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.clock = clock;
        this.tokens = capacity;
        this.lastRefillNanos = clock.getAsLong();
    }

    private void refill() {
        long now = clock.getAsLong();

        double elapsedSeconds =
                (now - lastRefillNanos) / 1_000_000_000.0;

        double tokensToAdd = elapsedSeconds * refillTokensPerSecond;

        tokens = Math.min(capacity, tokens + tokensToAdd);

        lastRefillNanos = now;
    }

    public synchronized boolean tryConsume() {
        refill();

        if (tokens >= 1) {
            tokens = tokens - 1;
            return true;
        }

        return false;
    }
}