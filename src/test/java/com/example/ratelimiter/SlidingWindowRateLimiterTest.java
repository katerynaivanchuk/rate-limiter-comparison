package com.example.ratelimiter;

import com.example.ratelimiter.algorithm.SlidingWindowRateLimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SlidingWindowRateLimiterTest {
    SlidingWindowRateLimiter limiter;

    @BeforeEach
    public void setUp() {
        limiter = new SlidingWindowRateLimiter(3, 10000);
    }

    @Test
    public void testSlidingWindowRateLimiter() {
        assertTrue(limiter.allowRequest("client-1", 1000));
        assertEquals(1000L, limiter.getClientWindows().get("client-1").getFirst());
        assertTrue(limiter.allowRequest("client-1", 2000));
        assertTrue(limiter.allowRequest("client-1", 3000));

        assertFalse(limiter.allowRequest("client-1", 4000));
        assertFalse(limiter.allowRequest("client-1", 5000));

        assertEquals(3, limiter.getClientWindows().get("client-1").size());

        assertTrue(limiter.allowRequest("client-1", 12000));
        assertEquals(List.of(2000L, 3000L, 12000L), limiter.getClientWindows().get("client-1"));
        assertTrue(limiter.allowRequest("client-2", 12000));
    }

    @Test
    public void testSlidingWindowRateLimiterOnEdge() {
        assertTrue(limiter.allowRequest("client-1", 8000));
        assertTrue(limiter.allowRequest("client-1", 9000));
        assertTrue(limiter.allowRequest("client-1", 9500));
        assertFalse(limiter.allowRequest("client-1", 10500));
        assertFalse(limiter.allowRequest("client-1", 11000));
        assertFalse(limiter.allowRequest("client-1", 12000));
    }

    @Test
    public void testSlidingWindowRateLimiterSimple() {
        assertTrue(limiter.allowRequest("client-1", 1000));
        assertTrue(limiter.allowRequest("client-1", 2000));
        assertTrue(limiter.allowRequest("client-1", 3000));
        assertFalse(limiter.allowRequest("client-1", 4000));
        assertFalse(limiter.allowRequest("client-1", 5000));
        assertTrue(limiter.allowRequest("client-1", 12000));
    }
}
