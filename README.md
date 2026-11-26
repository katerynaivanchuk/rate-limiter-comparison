# Rate Limiter Comparison

A Spring Boot project that implements three rate limiting algorithms behind one interface and compares how they behave on the same traffic.

> Draft. Sections marked TODO are for results and conclusions that are still to be written.

## Algorithms

All three implement `RateLimiter.allowRequest(String client, long momentMillis)`. The time is passed in as a parameter (not read from the clock), so every scenario can be tested deterministically.

| Algorithm | State kept per client |
|---|---|
| Fixed window | window number/start + request counter |
| Sliding window (log) | list of timestamps of allowed requests |
| Token bucket | token count + moment the refill is counted from |

### Design decisions (edge cases)

- **Fixed window:** windows are a fixed grid (0-10 s, 10-20 s, ...). A request exactly at the window end belongs to the next window.
- **Sliding window:** a request made exactly one window length ago still counts. Rejected requests are not recorded.
- **Token bucket:** a new client starts with a full bucket. The refill reference moment advances to the time of the last added token (not to the time of the request), so partial refill time is not lost. When the bucket is full, the reference moment is set to the request time.

## Architecture

```
Request -> RateLimitFilter -> RateLimiter (one of three algorithms)
                |                  |
                |  allow == true   -> continue to controller (200)
                |  allow == false  -> filter responds 429
```

- `algorithm` - the three algorithms and the interface (no Spring dependencies)
- `web` - controller and filter
- `config` - bean that wires the chosen algorithm

## Comparison

Limit: 3 requests per 10 s (token bucket: capacity 3, one token per 3 s). Requests at seconds 8, 9, 9.5, 10.5, 11, 12:

| Request at | Fixed window | Sliding window | Token bucket |
|---|---|---|---|
| 8 | allowed | allowed | allowed |
| 9 | allowed | allowed | allowed |
| 9.5 | allowed | allowed | allowed |
| 10.5 | allowed | rejected | rejected |
| 11 | allowed | rejected | allowed |
| 12 | allowed | rejected | rejected |

Fixed window lets 6 requests through in 4 seconds because the counter resets at the window boundary.

### Output of the comparison test

Moments in milliseconds, ✅ allowed, ❌ rejected.

```
token-bucket-limiter:   8000 ✅ 16000 ✅ 17000 ✅ 18000 ✅ 19000 ✅ 20000 ❌
sliding-window-limiter: 8000 ✅ 16000 ✅ 17000 ✅ 18000 ❌ 19000 ✅ 20000 ❌
fixed-window-limiter:   8000 ✅ 16000 ✅ 17000 ✅ 18000 ✅ 19000 ❌ 20000 ✅
-------------------
token-bucket-limiter:   8000 ✅ 9000 ✅ 9500 ✅ 10500 ❌ 11000 ✅ 12000 ❌
sliding-window-limiter: 8000 ✅ 9000 ✅ 9500 ✅ 10500 ❌ 11000 ❌ 12000 ❌
fixed-window-limiter:   8000 ✅ 9000 ✅ 9500 ✅ 10500 ✅ 11000 ✅ 12000 ✅
-------------------
token-bucket-limiter:   1000 ✅ 2000 ✅ 3000 ✅ 4000 ✅ 5000 ❌ 12000 ✅
sliding-window-limiter: 1000 ✅ 2000 ✅ 3000 ✅ 4000 ❌ 5000 ❌ 12000 ✅
fixed-window-limiter:   1000 ✅ 2000 ✅ 3000 ✅ 4000 ❌ 5000 ❌ 12000 ✅
-------------------
token-bucket-limiter:   0 ✅ 0 ✅ 0 ✅ 7000 ✅ 8000 ✅ 8500 ❌ 9000 ✅
sliding-window-limiter: 0 ✅ 0 ✅ 0 ✅ 7000 ❌ 8000 ❌ 8500 ❌ 9000 ❌
fixed-window-limiter:   0 ✅ 0 ✅ 0 ✅ 7000 ❌ 8000 ❌ 8500 ❌ 9000 ❌
-------------------
token-bucket-limiter:   1000 ✅ 100000 ✅ 100000 ✅ 100000 ✅ 100000 ❌
sliding-window-limiter: 1000 ✅ 100000 ✅ 100000 ✅ 100000 ✅ 100000 ❌
fixed-window-limiter:   1000 ✅ 100000 ✅ 100000 ✅ 100000 ✅ 100000 ❌
```

TODO: memory comparison, measured numbers, conclusions about when each algorithm fits.


## Thread safety

Test: 100 threads call `allowRequest` for the same client at the same moment (limit 3), repeated 20 times with a fresh limiter each time. Each number below is how many requests were allowed in one repetition. The correct result is never more than 3.

### Before the fix, no artificial delay (all three algorithms)

Code under test: plain `HashMap`, unsynchronised per-client state.

```
fixed-window:   [4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3]
sliding-window: [3, 3, 3, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3]
token-bucket:   [3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3]
```

All three break the limit, but only in some repetitions: fixed window allowed 4 requests once, sliding window allowed 10 once, token bucket allowed 4 once (1 of 20 repetitions each). The rest looked correct. This is typical for a race condition: it depends on thread timing, so one green run proves nothing.


### Before the fix, with `Thread.sleep(1)` between the check and the update

Code under test: the same, plus a temporary `Thread.sleep(1)` inside `FixedWindowRateLimiter.allowRequest` to widen the race window, so the bug shows up every time. Every thread passes the check before any of them updates the state.

```
fixed-window: [100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100]
```

The delay was used only on the fixed window, to illustrate how a wider race window makes the failure deterministic. The other two algorithms were reproduced without any delay (see above).

### After the fix

TODO: describe the fix and show the same runs (without delay, and with `Thread.sleep(1)`). Expected: exactly 3 in every repetition.

## Running

TODO

## Results and conclusions

TODO
