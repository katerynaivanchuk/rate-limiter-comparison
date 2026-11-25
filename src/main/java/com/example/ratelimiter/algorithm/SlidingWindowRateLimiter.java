package com.example.ratelimiter.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlidingWindowRateLimiter implements RateLimiter {
    private int requestLimit;
    private long mlsLimit;
    private Map<String, List<Long>> clientWindows = new HashMap<>();

    public SlidingWindowRateLimiter(int requestLimit,  long mlsLimit) {
        this.requestLimit = requestLimit;
        this.mlsLimit = mlsLimit;
    }

    @Override
    public boolean allowRequest(String clientId, long moment) {
        if (clientWindows.containsKey(clientId)) {
            List<Long> windows = clientWindows.get(clientId);
            if(moment - mlsLimit <= 0){
                if(windows.size() < requestLimit){
                    windows.add(moment);
                    return true;
                } else {
                    return false;
                }
            }
            else {
                long edge = moment -  mlsLimit;
                windows.removeIf(w -> w < edge);
                if(windows.size() < requestLimit){
                    windows.add(moment);
                    return true;
                } else {
                    return false;
                }
            }
        }
        List<Long> clientWindow = new ArrayList<>();
        clientWindow.add(moment);
        clientWindows.put(clientId, clientWindow);
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

    public Map<String, List<Long>> getClientWindows() {
        return clientWindows;
    }

    public void setClientWindows(Map<String, List<Long>> clientWindows) {
        this.clientWindows = clientWindows;
    }

}
