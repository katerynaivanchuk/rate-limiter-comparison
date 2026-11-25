package com.example.ratelimiter.algorithm;

public interface RateLimiter {
    // moment in millis
    public boolean allowRequest(String clientId, long moment);
}
