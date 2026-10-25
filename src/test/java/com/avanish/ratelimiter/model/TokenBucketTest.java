package com.avanish.ratelimiter.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketTest {

    @Test
    void newBucketAllowsARequest() {
        TokenBucket bucket = new TokenBucket(10, 1);

        boolean allowed = bucket.tryConsume();

        assertTrue(allowed);
    }

    @Test
    void zeroCapacityIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TokenBucket(0, 1)
        );
    }

    @Test
    void zeroRefillRateIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new TokenBucket(10, 0)
        );
    }

    @Test
    void emptyBucketRejectsARequest() {
        TokenBucket bucket = new TokenBucket(2, 1, () -> 0L);

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }

    @Test
    void oneSecondRestoresOneToken() {
        AtomicLong time = new AtomicLong(0L);
        TokenBucket bucket = new TokenBucket(1, 1, () -> time.get());

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());

        time.set(1_000_000_000L);

        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }
    @Test
    void halfATokenDoesNotAllowARequest() {
        AtomicLong time = new AtomicLong(0L);
        TokenBucket bucket = new TokenBucket(1, 1, () -> time.get());

        assertTrue(bucket.tryConsume());

        time.set(500_000_000L);
        assertFalse(bucket.tryConsume());

        time.set(1_000_000_000L);
        assertTrue(bucket.tryConsume());
    }
    @Test
    void refillingDoesNotExceedCapacity() {
        AtomicLong time = new AtomicLong(0L);
        TokenBucket bucket = new TokenBucket(2, 1, () -> time.get());

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());

        time.set(100_000_000_000L);

        assertTrue(bucket.tryConsume());
        assertTrue(bucket.tryConsume());
        assertFalse(bucket.tryConsume());
    }

    @Test
    void concurrentRequestsCannotSpendMoreThanCapacity() throws Exception {
        TokenBucket bucket = new TokenBucket(10, 1, () -> 0L);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        try {
            for (int i = 0; i < 100; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return bucket.tryConsume();
                }));
            }

            start.countDown();

            int allowed = 0;

            for (Future<Boolean> result : results) {
                if (result.get(5, TimeUnit.SECONDS)) {
                    allowed++;
                }
            }

            assertEquals(10, allowed);
        } finally {
            executor.shutdownNow();
        }
    }
}