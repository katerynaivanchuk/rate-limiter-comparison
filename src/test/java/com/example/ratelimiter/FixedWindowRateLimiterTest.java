package com.example.ratelimiter;

import com.example.ratelimiter.algorithm.FixedWindowRateLimiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class FixedWindowRateLimiterTest {

    private FixedWindowRateLimiter limiter;

    @BeforeEach
    public void setup() {
        this.limiter = new FixedWindowRateLimiter(3, 10000);
    }
    @Test
    public void testFixedWindowRateLimiter() {
        assertTrue(limiter.allowRequest("client-1", 5000));
        assertTrue(limiter.allowRequest("client-1", 7000));
        assertTrue(limiter.allowRequest("client-1", 9000));
        assertFalse(limiter.allowRequest("client-1", 9500));
        assertEquals(3, limiter.getClientRequestCounter("client-1"));
        assertTrue(limiter.allowRequest("client-2", 7000));
        assertTrue(limiter.allowRequest("client-3", 22000));
    }

    @Test
    public void testFixedWindowRateLimiterOnEdge() {
        assertTrue(limiter.allowRequest("client-1", 9000));
        assertEquals(1, limiter.getClientRequestCounter("client-1"));
        assertTrue(limiter.allowRequest("client-1", 9500));
        assertEquals(2, limiter.getClientRequestCounter("client-1"));
        assertTrue(limiter.allowRequest("client-1", 10000));
        assertEquals(1, limiter.getClientRequestCounter("client-1"));
    }

    @Test
    public void testFixedWindowRateLimiterOnEdgeAdditional() {
        assertTrue(limiter.allowRequest("client-1", 8000));
        assertTrue(limiter.allowRequest("client-1", 16000));
        assertTrue(limiter.allowRequest("client-1", 17000));
        assertTrue(limiter.allowRequest("client-1", 18000));
        assertFalse(limiter.allowRequest("client-1", 19000));
        assertTrue(limiter.allowRequest("client-1", 20000));
    }
}
