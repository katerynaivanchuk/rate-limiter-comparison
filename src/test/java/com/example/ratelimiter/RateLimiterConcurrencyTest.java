package com.example.ratelimiter;

import com.example.ratelimiter.algorithm.*;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimiterConcurrencyTest {

    RateLimiter limiter;


    static Stream<Arguments> getLimiters() {
        return Stream.of(
                Arguments.of("fixed-window",
                        (Supplier<RateLimiter>) () -> new FixedWindowRateLimiter(3, 10000)),
                Arguments.of("sliding-window",
                        (Supplier<RateLimiter>) () -> new SlidingWindowRateLimiter(3, 10000)),
                Arguments.of("token-bucket",
                        (Supplier<RateLimiter>) () -> new TokenBucketRateLimiter(3, 3000))
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("getLimiters")
    public void testRateLimiterConcurrency(String name, Supplier<RateLimiter> factory) throws InterruptedException {
        List<Integer> results = new ArrayList<>();
        int threads = 100;
        System.out.println("=== " + name + " ===");

        for(int run = 0; run < 20; run++) {
            RateLimiter limiter = factory.get();

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

            results.add(countRequest.get());

        }




        System.out.println(name + ": " + results);
        assertTrue(results.stream().allMatch(value -> value <= 3), name + ": " + results );
    }
}
