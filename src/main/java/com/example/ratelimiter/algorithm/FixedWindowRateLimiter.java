package com.example.ratelimiter.algorithm;

import java.util.HashMap;
import java.util.Map;

public class FixedWindowRateLimiter implements RateLimiter {

    private int requestLimit;
    private long mlsLimit;
    private Map<String, WindowCounter> clientWindows = new HashMap<>();

    private static class WindowCounter {
        private long startWindow;
        private int requestCounter;

        public WindowCounter(long startWindow) {
            this.startWindow = startWindow;
            this.requestCounter = 1;
        }

        public long getStartWindow() {
            return startWindow;
        }
        public int getRequestCounter() {
            return requestCounter;
        }

    }

    public FixedWindowRateLimiter(int requestLimit, long mlsLimit) {
        this.requestLimit = requestLimit;
        this.mlsLimit = mlsLimit;
    }

    @Override
    public boolean allowRequest(String client, long moment) {
        if(clientWindows.containsKey(client)) {
            WindowCounter counter = clientWindows.get(client);
            if((counter.startWindow / mlsLimit) == (moment / mlsLimit)) {
                if(counter.requestCounter < requestLimit) {
                    counter.requestCounter++;
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                counter.startWindow = moment;
                counter.requestCounter = 1;
                return true;
            }
        }

        WindowCounter counter = new WindowCounter(moment);
        clientWindows.put(client, counter);
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

    public Map<String, WindowCounter> getClientWindows() {
        return clientWindows;
    }

    public void setClientWindows(Map<String, WindowCounter> clientWindows) {
        this.clientWindows = clientWindows;
    }

    public int getClientRequestCounter(String client) {
        WindowCounter counter = clientWindows.get(client);
        return counter.requestCounter;
    }
}
