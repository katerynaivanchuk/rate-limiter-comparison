package com.example.ratelimiter.algorithm;

import java.util.HashMap;
import java.util.Map;

public class TokenBucketRateLimiter implements RateLimiter {
    private int requestLimit;
    private long mlsLimit;
    private Map<String, BucketState> clientWindows = new HashMap<>();

    private static class BucketState{
        private int tokenAmount = 3;
        private long lastMoment;

        public BucketState(long lastMoment) {
            this.lastMoment = lastMoment;
        }

        public int getTokenAmount() {
            return tokenAmount;
        }
    }

    public TokenBucketRateLimiter(int requestLimit,  long mlsLimit) {
        this.requestLimit = requestLimit;
        this.mlsLimit = mlsLimit;
    }

    @Override
    public boolean allowRequest(String clientId, long moment) {
        if (clientWindows.containsKey(clientId)) {
            BucketState bucketState = clientWindows.get(clientId);

            int newTokens = (int) ((moment - bucketState.lastMoment) / mlsLimit);
            bucketState.tokenAmount += Math.min(newTokens, 3);
            if(bucketState.tokenAmount > requestLimit) {
                bucketState.tokenAmount = requestLimit;
                bucketState.lastMoment = moment;
            } else {
                bucketState.lastMoment = bucketState.lastMoment + newTokens * mlsLimit;
            }


            if(bucketState.tokenAmount > 0)
            {
                bucketState.tokenAmount -= 1;
                return true;
            } else {
                return false;
            }
        }
        BucketState bucketState = new BucketState(moment);
        bucketState.tokenAmount -= 1;
        bucketState.lastMoment = moment;
        clientWindows.put(clientId, bucketState);
        return true;
    }

    public int getRequestLimit() {
        return requestLimit;
    }

    public void setRequestLimit(int requestLimit) {
        this.requestLimit = requestLimit;
    }

    public long getMlsLimit() {
        return mlsLimit;
    }

    public void setMlsLimit(long mlsLimit) {
        this.mlsLimit = mlsLimit;
    }

    public Map<String, BucketState> getClientWindows() {
        return clientWindows;
    }

    public void setClientWindows(Map<String, BucketState> clientWindows) {
        this.clientWindows = clientWindows;
    }

    public int getCLientAvailableTokens(String clientId) {
        BucketState bucketState = clientWindows.get(clientId);
        return bucketState.tokenAmount;
    }
}
