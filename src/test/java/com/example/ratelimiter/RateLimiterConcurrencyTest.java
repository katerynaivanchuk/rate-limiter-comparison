package com.example.ratelimiter;

import com.example.ratelimiter.algorithm.*;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimiterConcurrencyTest {

    RateLimiter limiter;

    @BeforeEach
    public void setup() {
        limiter = new FixedWindowRateLimiter(3, 10000);
    }

    @RepeatedTest(20)
    public void testRateLimiterConcurrency() throws InterruptedException {
        int threads = 100;

        ExecutorService pool =  Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger countRequest = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
           pool.submit(() -> {
               try {
                   ready.countDown();
                   start.await();
                   if(limiter.allowRequest("client-1", 5000)){
                       countRequest.incrementAndGet();
                   }
               } catch (InterruptedException e) {
                   Thread.currentThread().interrupt();
               } catch (Exception e) {
                   e.printStackTrace();
               } finally {
                   done.countDown();
               }
           });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        System.out.println(countRequest.get());
        assertTrue(countRequest.get() <= 3);
    }
}
