package com.example.ratelimiter;

import com.example.ratelimiter.algorithm.FixedWindowRateLimiter;

import com.example.ratelimiter.algorithm.RateLimiter;
import com.example.ratelimiter.algorithm.SlidingWindowRateLimiter;
import com.example.ratelimiter.algorithm.TokenBucketRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RatelimiterApplicationTests {

	@Test
	void testAllRateLimiter() {
        TokenBucketRateLimiter tlimiter = new TokenBucketRateLimiter(3, 3000);
        SlidingWindowRateLimiter slimiter = new SlidingWindowRateLimiter(3, 10000);
        FixedWindowRateLimiter flimiter = new FixedWindowRateLimiter(3, 10000);

        long[] firstSequence = {8000, 16000, 17000,18000,19000, 20000};
        long[] secondSequence = {8000, 9000, 9500,10500,11000, 12000};
        long[] thirdSequence = {1000, 2000, 3000,4000,5000, 12000};
        long[] fourthSequence = {0, 0, 0,7000,8000, 8500,9000};
        long[] fifthSequence = {1000, 100000, 100000, 100000, 100000};

        printResult("token-bucket-limiter", tlimiter, firstSequence);
        printResult("sliding-window-limiter", slimiter, firstSequence);
        printResult("fixed-window-limiter", flimiter, firstSequence);

        System.out.println("-------------------");

        printResult("token-bucket-limiter", new TokenBucketRateLimiter(3, 3000), secondSequence);
        printResult("sliding-window-limiter", new SlidingWindowRateLimiter(3, 10000), secondSequence);
        printResult("fixed-window-limiter", new FixedWindowRateLimiter(3, 10000), secondSequence);

        System.out.println("-------------------");

        printResult("token-bucket-limiter", new TokenBucketRateLimiter(3, 3000), thirdSequence);
        printResult("sliding-window-limiter", new SlidingWindowRateLimiter(3, 10000), thirdSequence);
        printResult("fixed-window-limiter", new FixedWindowRateLimiter(3, 10000), thirdSequence);

        System.out.println("-------------------");

        printResult("token-bucket-limiter", new TokenBucketRateLimiter(3, 3000), fourthSequence);
        printResult("sliding-window-limiter", new SlidingWindowRateLimiter(3, 10000), fourthSequence);
        printResult("fixed-window-limiter", new FixedWindowRateLimiter(3, 10000), fourthSequence);

        System.out.println("-------------------");

        printResult("token-bucket-limiter", new TokenBucketRateLimiter(3, 3000), fifthSequence);
        printResult("sliding-window-limiter", new SlidingWindowRateLimiter(3, 10000), fifthSequence);
        printResult("fixed-window-limiter", new FixedWindowRateLimiter(3, 10000), fifthSequence);

	}

    public void printResult(String name, RateLimiter ratelimiter, long[] moments) {
        StringBuilder sb = new StringBuilder(name + ": ");
        for(long moment: moments) {
            sb.append(moment).append(ratelimiter.allowRequest("client-1", moment) ? " ✅ " : " ❌");
        }
        System.out.println(sb);
    }
}
