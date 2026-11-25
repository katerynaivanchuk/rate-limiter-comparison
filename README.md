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

Output of the comparison test (moments in ms, ✅ allowed, ❌ rejected):

```
token-bucket-limiter:   8000✅ 9000✅ 9500✅ 10500❌ 11000✅ 12000❌
sliding-window-limiter: 8000✅ 9000✅ 9500✅ 10500❌ 11000❌ 12000❌
fixed-window-limiter:   8000✅ 9000✅ 9500✅ 10500✅ 11000✅ 12000✅
```

| Request at | Fixed window | Sliding window | Token bucket |
|---|---|---|---|
| 8 | allowed | allowed | allowed |
| 9 | allowed | allowed | allowed |
| 9.5 | allowed | allowed | allowed |
| 10.5 | allowed | rejected | rejected |
| 11 | allowed | rejected | allowed |
| 12 | allowed | rejected | rejected |

Fixed window lets 6 requests through in 4 seconds because the counter resets at the window boundary.

TODO: more scenarios, memory comparison, measured numbers.

## Thread safety

Test: 100 threads call `allowRequest` for the same client at the same moment (limit 3, `FixedWindowRateLimiter`), repeated 20 times. Each number below is how many requests were allowed in one repetition. The correct result is never more than 3.

**Before the fix, no artificial delay**

Code under test: plain `HashMap`, no synchronisation. 19 runs correct, 1 run broken (a real race condition).

```
3 3 3 4 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3
```

**Before the fix, with `Thread.sleep(1)` between the check and the update**

Code under test: the same, plus a temporary `Thread.sleep(1)` inside `allowRequest` to widen the race window, so the bug shows up every time. Every thread passes the check before any of them updates the counter.

```
100 100 100 100 100 100 100 100 100 100 100 100 100 100 100 100 100 100 100 100
```

**After the fix**

TODO

## Running

TODO

## Results and conclusions

TODO
