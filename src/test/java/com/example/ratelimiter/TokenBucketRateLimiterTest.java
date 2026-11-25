package com.example.ratelimiter;

import com.example.ratelimiter.algorithm.TokenBucketRateLimiter;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBucketRateLimiterTest {

    TokenBucketRateLimiter limiter;

    @BeforeEach
    public void setup() {
        this.limiter = new TokenBucketRateLimiter(3, 3000);
    }

    @Test
    public void testTokenBucketRateLimiter() {
        assertTrue(limiter.allowRequest("client-1", 1000));
        assertTrue(limiter.allowRequest("client-1", 2000));
        assertTrue(limiter.allowRequest("client-1", 3000));
        assertEquals(0, limiter.getCLientAvailableTokens("client-1"));
        assertTrue(limiter.allowRequest("client-1", 4000));
        assertFalse(limiter.allowRequest("client-1", 5000));

        assertTrue(limiter.allowRequest("client-1", 50000));
        assertEquals(2, limiter.getCLientAvailableTokens("client-1"));
    }

    @Test
    public void testTokenBucketRateLimiterFirstSequence() {
        assertTrue(limiter.allowRequest("client-1", 0));
        assertTrue(limiter.allowRequest("client-1", 0));
        assertTrue(limiter.allowRequest("client-1", 0));
        assertTrue(limiter.allowRequest("client-1", 7000));
        assertTrue(limiter.allowRequest("client-1", 8000));
        assertFalse(limiter.allowRequest("client-1", 8500));
        assertTrue(limiter.allowRequest("client-1", 9000));
    }

    @Test
    public void testTokenBucketRateLimiterSecondSequence() {
        assertTrue(limiter.allowRequest("client-1", 1000));
        assertTrue(limiter.allowRequest("client-1", 100000));
        assertTrue(limiter.allowRequest("client-1", 100000));
        assertTrue(limiter.allowRequest("client-1", 100000));
        assertFalse(limiter.allowRequest("client-1", 100000));
        assertTrue(limiter.allowRequest("client-2", 9000));

    }

    @Test
    public void testTokenBucketRateLimiterThirdSequence() {
        assertTrue(limiter.allowRequest("client-1", 8000));
        assertTrue(limiter.allowRequest("client-1", 16000));
        assertTrue(limiter.allowRequest("client-1", 17000));
        assertTrue(limiter.allowRequest("client-1", 18000));
        assertTrue(limiter.allowRequest("client-1", 19000));
        assertFalse(limiter.allowRequest("client-1", 20000));

    }
}
