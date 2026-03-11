package com.example.demo.framework.springcloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * 熔断与限流
 *
 * 【熔断器模式】
 * 当下游服务故障时，快速失败，避免级联崩溃。
 * 类似电路熔断器，电流过大时自动断开。
 *
 * 【限流】
 * 控制请求速率，保护系统不被压垮。
 *
 * 【主流框架】
 * - Hystrix：Netflix开源，已停止维护
 * - Sentinel：阿里开源，功能全面
 * - Resilience4j：轻量级，Spring Cloud推荐
 */
public class CircuitBreaker {

    // ==================== 熔断器状态机 ====================

    /**
     * 熔断器三种状态：
     *
     *           失败率 < 阈值
     *    ┌────────────────────────┐
     *    │                        │
     *    ▼                        │
     * ┌──────────┐  失败率 > 阈值  ┌──────────┐
     * │  CLOSED  │───────────────→│   OPEN   │
     * │  (关闭)   │                │  (打开)   │
     * └──────────┘                └────┬─────┘
     *    ▲                             │
     *    │                    时间窗口后
     *    │                             │
     *    │                             ▼
     *    │                       ┌──────────┐
     *    └───────────────────────│HALF_OPEN │
     *        成功则关闭            │ (半开)    │
     *                            └──────────┘
     *                                 │
     *                         失败则重新打开
     *
     * CLOSED：正常状态，请求正常通过
     * OPEN：熔断状态，直接拒绝请求
     * HALF_OPEN：半开状态，放行部分请求探测
     */

    /**
     * 简易熔断器实现
     */
    static class SimpleCircuitBreaker {
        // 状态
        private enum State { CLOSED, OPEN, HALF_OPEN }

        private final AtomicInteger failures = new AtomicInteger(0);
        private final AtomicInteger successes = new AtomicInteger(0);
        private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);

        private final int failureThreshold;      // 失败阈值
        private final int successThreshold;      // 半开状态成功阈值
        private final long timeout;              // 熔断超时时间
        private final TimeUnit timeUnit;

        private volatile long lastFailureTime;

        public SimpleCircuitBreaker(int failureThreshold, int successThreshold,
                                    long timeout, TimeUnit timeUnit) {
            this.failureThreshold = failureThreshold;
            this.successThreshold = successThreshold;
            this.timeout = timeout;
            this.timeUnit = timeUnit;
        }

        /**
         * 允许请求通过吗？
         */
        public boolean allowRequest() {
            State currentState = state.get();

            switch (currentState) {
                case CLOSED:
                    return true;

                case OPEN:
                    // 检查是否可以进入半开状态
                    if (System.currentTimeMillis() - lastFailureTime > timeUnit.toMillis(timeout)) {
                        return state.compareAndSet(State.OPEN, State.HALF_OPEN);
                    }
                    return false;

                case HALF_OPEN:
                    return true;

                default:
                    return false;
            }
        }

        /**
         * 记录成功
         */
        public void recordSuccess() {
            State currentState = state.get();

            if (currentState == State.HALF_OPEN) {
                int success = successes.incrementAndGet();
                if (success >= successThreshold) {
                    // 重置状态
                    failures.set(0);
                    successes.set(0);
                    state.set(State.CLOSED);
                }
            } else {
                failures.set(0);
            }
        }

        /**
         * 记录失败
         */
        public void recordFailure() {
            lastFailureTime = System.currentTimeMillis();
            State currentState = state.get();

            if (currentState == State.HALF_OPEN) {
                // 半开状态失败，重新打开
                state.set(State.OPEN);
            } else if (currentState == State.CLOSED) {
                int failure = failures.incrementAndGet();
                if (failure >= failureThreshold) {
                    state.set(State.OPEN);
                }
            }
        }

        public String getState() {
            return state.get().name();
        }
    }

    // ==================== 限流算法 ====================

    /**
     * 1. 固定窗口计数器
     *
     * 简单但有临界问题
     */
    static class FixedWindowRateLimiter {
        private final AtomicInteger counter = new AtomicInteger(0);
        private final int limit;          // 窗口内最大请求数
        private final long windowSize;    // 窗口大小（毫秒）
        private volatile long windowStart;

        public FixedWindowRateLimiter(int limit, long windowSize) {
            this.limit = limit;
            this.windowSize = windowSize;
            this.windowStart = System.currentTimeMillis();
        }

        public boolean tryAcquire() {
            long now = System.currentTimeMillis();

            // 检查是否需要重置窗口
            if (now - windowStart >= windowSize) {
                synchronized (this) {
                    if (now - windowStart >= windowSize) {
                        windowStart = now;
                        counter.set(0);
                    }
                }
            }

            return counter.incrementAndGet() <= limit;
        }
    }

    /**
     * 2. 滑动窗口
     *
     * 更精细的控制，无临界问题
     */
    static class SlidingWindowRateLimiter {
        private final int limit;
        private final long windowSize;
        private final LinkedList<Long> timestamps = new LinkedList<>();

        public SlidingWindowRateLimiter(int limit, long windowSize) {
            this.limit = limit;
            this.windowSize = windowSize;
        }

        public synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            long windowStart = now - windowSize;

            // 移除窗口外的请求
            while (!timestamps.isEmpty() && timestamps.getFirst() < windowStart) {
                timestamps.removeFirst();
            }

            if (timestamps.size() < limit) {
                timestamps.add(now);
                return true;
            }

            return false;
        }
    }

    /**
     * 3. 漏桶算法
     *
     * 以固定速率处理请求，平滑流量
     *
     * ┌───────────┐     ┌───────────┐     ┌───────────┐
     * │   请求    │────→│   漏桶    │────→│   处理    │
     * │  (任意速率)│     │ (固定速率) │     │           │
     * └───────────┘     └───────────┘     └───────────┘
     */
    static class LeakyBucketRateLimiter {
        private final int capacity;      // 桶容量
        private final int rate;          // 漏出速率（请求/秒）
        private final AtomicInteger water = new AtomicInteger(0);
        private volatile long lastLeakTime;

        public LeakyBucketRateLimiter(int capacity, int rate) {
            this.capacity = capacity;
            this.rate = rate;
            this.lastLeakTime = System.currentTimeMillis();
        }

        public synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastLeakTime;

            // 计算漏掉的水
            int leaked = (int) (elapsed * rate / 1000);
            if (leaked > 0) {
                water.set(Math.max(0, water.get() - leaked));
                lastLeakTime = now;
            }

            // 尝试加水
            if (water.get() < capacity) {
                water.incrementAndGet();
                return true;
            }

            return false; // 桶满了
        }
    }

    /**
     * 4. 令牌桶算法（推荐）
     *
     * 以固定速率生成令牌，请求需要获取令牌才能通过
     * 允许一定程度的突发流量
     *
     * ┌───────────┐     ┌───────────┐     ┌───────────┐
     * │  令牌生成 │────→│   令牌桶   │←────│   请求    │
     * │ (固定速率) │     │           │     │ (取令牌)   │
     * └───────────┘     └───────────┘     └───────────┘
     */
    static class TokenBucketRateLimiter {
        private final int capacity;      // 桶容量
        private final int rate;          // 令牌生成速率（个/秒）
        private final AtomicInteger tokens;
        private volatile long lastRefillTime;

        public TokenBucketRateLimiter(int capacity, int rate) {
            this.capacity = capacity;
            this.rate = rate;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }

        public boolean tryAcquire() {
            return tryAcquire(1);
        }

        public synchronized boolean tryAcquire(int permits) {
            // 补充令牌
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            int newTokens = (int) (elapsed * rate / 1000);

            if (newTokens > 0) {
                tokens.set(Math.min(capacity, tokens.get() + newTokens));
                lastRefillTime = now;
            }

            // 尝试获取令牌
            if (tokens.get() >= permits) {
                tokens.addAndGet(-permits);
                return true;
            }

            return false;
        }
    }

    /**
     * 5. 滑动日志算法（精确但内存占用高）
     */
    static class SlidingLogRateLimiter {
        private final int limit;
        private final long windowSize;
        private final TreeMap<Long, Integer> logs = new TreeMap<>();

        public SlidingLogRateLimiter(int limit, long windowSize) {
            this.limit = limit;
            this.windowSize = windowSize;
        }

        public synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            long windowStart = now - windowSize;

            // 计算窗口内的请求数
            int count = 0;
            for (Map.Entry<Long, Integer> entry : logs.entrySet()) {
                if (entry.getKey() > windowStart) {
                    count += entry.getValue();
                }
            }

            // 清理过期日志
            logs.headMap(windowStart).clear();

            if (count < limit) {
                logs.merge(now, 1, Integer::sum);
                return true;
            }

            return false;
        }
    }

    // ==================== 算法对比 ====================

    /**
     * ┌──────────────────┬─────────────────┬─────────────────┬─────────────────┐
     * │ 算法              │ 优点            │ 缺点            │ 适用场景        │
     * ├──────────────────┼─────────────────┼─────────────────┼─────────────────┤
     * │ 固定窗口          │ 简单高效        │ 临界问题        │ 简单限流        │
     * │ 滑动窗口          │ 精确            │ 内存占用        │ 精确限流        │
     * │ 漏桶              │ 平滑流量        │ 无法应对突发    │ 保护下游        │
     * │ 令牌桶            │ 允许突发        │ 实现稍复杂      │ API限流（推荐） │
     * │ 滑动日志          │ 最精确          │ 内存占用最高    │ 分布式限流      │
     * └──────────────────┴─────────────────┴─────────────────┴─────────────────┘
     */

    // ==================== Sentinel使用示例 ====================

    /**
     * Sentinel核心概念：
     * 1. 资源：需要保护的对象（API、方法）
     * 2. 规则：限流、熔断、降级规则
     * 3. 入口：流量的入口点
     *
     * 配置示例：
     */
    /*
    // 定义资源
    @SentinelResource(value = "getUser", blockHandler = "handleBlock",
                      fallback = "handleFallback")
    public User getUser(Long id) {
        return userMapper.selectById(id);
    }

    // 限流/熔断处理
    public User handleBlock(Long id, BlockException ex) {
        return new User(-1L, "限流中", null);
    }

    // 异常降级处理
    public User handleFallback(Long id, Throwable t) {
        return new User(-1L, "服务降级", null);
    }

    // 规则配置
    List<FlowRule> rules = new ArrayList<>();
    FlowRule rule = new FlowRule("getUser");
    rule.setCount(100);                    // QPS阈值
    rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
    rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
    rules.add(rule);
    FlowRuleManager.loadRules(rules);
    */

    // ==================== Resilience4j使用示例 ====================

    /**
     * Resilience4j核心模块：
     * - CircuitBreaker：熔断器
     * - RateLimiter：限流器
     * - Retry：重试
     * - Bulkhead：舱壁隔离
     * - TimeLimiter：超时控制
     */
    /*
    // 配置
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(50)              // 失败率阈值50%
        .waitDurationInOpenState(Duration.ofSeconds(10))  // 熔断等待时间
        .slidingWindowSize(10)                 // 滑动窗口大小
        .slidingWindowType(SlidingWindowType.COUNT_BASED)
        .build();

    CircuitBreaker circuitBreaker = CircuitBreaker.of("userService", config);

    // 使用
    Supplier<User> supplier = CircuitBreaker.decorateSupplier(
        circuitBreaker,
        () -> userClient.getUser(id)
    );

    Try<User> result = Try.ofSupplier(supplier)
        .recover(throwable -> getDefaultUser());
    */

    // ==================== 测试代码 ====================
    public static void main(String[] args) throws InterruptedException {
        // 测试熔断器
        System.out.println("=== 熔断器测试 ===");
        SimpleCircuitBreaker breaker = new SimpleCircuitBreaker(3, 2, 5, TimeUnit.SECONDS);

        // 模拟失败
        for (int i = 0; i < 5; i++) {
            if (breaker.allowRequest()) {
                breaker.recordFailure();
                System.out.println("请求" + (i+1) + "失败，状态：" + breaker.getState());
            } else {
                System.out.println("请求" + (i+1) + "被熔断");
            }
        }

        // 测试限流器
        System.out.println("\n=== 令牌桶限流测试 ===");
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 2);

        for (int i = 0; i < 10; i++) {
            if (limiter.tryAcquire()) {
                System.out.println("请求" + (i+1) + "通过");
            } else {
                System.out.println("请求" + (i+1) + "被限流");
            }
            Thread.sleep(100);
        }
    }
}
