package com.example.demo.javabase.concurrency;

import java.util.concurrent.*;

/**
 * 线程池详解
 *
 * 【为什么要用线程池】
 * 1. 降低资源消耗：重复利用已创建的线程
 * 2. 提高响应速度：任务到达时无需创建线程
 * 3. 便于管理：统一管理线程，防止无限制创建
 *
 * 【线程池核心参数】
 * ┌──────────────────────┬────────────────────────────────────────┐
 * │ 参数                  │ 说明                                    │
 * ├──────────────────────┼────────────────────────────────────────┤
 * │ corePoolSize         │ 核心线程数（常驻线程）                    │
 * │ maximumPoolSize      │ 最大线程数                               │
 * │ keepAliveTime        │ 非核心线程空闲存活时间                    │
 * │ unit                 │ 时间单位                                 │
 * │ workQueue            │ 任务队列                                 │
 * │ threadFactory        │ 线程工厂                                 │
 * │ handler              │ 拒绝策略                                 │
 * └──────────────────────┴────────────────────────────────────────┘
 *
 * 【线程池工作流程】
 * 1. 任务提交后，先判断核心线程是否已满
 * 2. 未满 → 创建核心线程执行任务
 * 3. 已满 → 加入任务队列
 * 4. 队列已满 → 创建非核心线程
 * 5. 达到最大线程数 → 执行拒绝策略
 */
public class ThreadPools {

    // ==================== JDK内置线程池 ====================
    /**
     * 1. FixedThreadPool（固定线程数）
     *
     * 特点：
     * - 核心线程数 = 最大线程数
     * - 使用无界队列 LinkedBlockingQueue
     *
     * 问题：
     * - 队列无界，可能导致OOM
     * - 不推荐直接使用 Executors 创建
     */
    // ExecutorService fixedPool = Executors.newFixedThreadPool(10);
    // 等价于：
    static ExecutorService createFixedThreadPool() {
        return new ThreadPoolExecutor(
            10,  // corePoolSize
            10,  // maximumPoolSize
            0L, TimeUnit.MILLISECONDS, // keepAliveTime（无效，因为core=max）
            new LinkedBlockingQueue<>() // 无界队列！
        );
    }

    /**
     * 2. CachedThreadPool（可缓存线程池）
     *
     * 特点：
     * - 核心线程数为0，最大线程数无限制
     * - 空闲线程存活60秒
     * - 使用同步队列 SynchronousQueue
     *
     * 问题：
     * - 线程数无限制，可能导致OOM
     * - 高并发时会创建大量线程
     */
    // ExecutorService cachedPool = Executors.newCachedThreadPool();
    static ExecutorService createCachedThreadPool() {
        return new ThreadPoolExecutor(
            0,                          // corePoolSize
            Integer.MAX_VALUE,          // maximumPoolSize（无限制！）
            60L, TimeUnit.SECONDS,      // keepAliveTime
            new SynchronousQueue<>()    // 直接交接，不排队
        );
    }

    /**
     * 3. SingleThreadExecutor（单线程线程池）
     *
     * 特点：
     * - 只有一个工作线程
     * - 任务按顺序执行
     *
     * 问题：
     * - 使用无界队列
     */
    // ExecutorService singlePool = Executors.newSingleThreadExecutor();

    /**
     * 4. ScheduledThreadPool（定时任务线程池）
     *
     * 特点：
     * - 支持定时执行、周期执行
     */
    static ScheduledExecutorService createScheduledThreadPool() {
        return new ScheduledThreadPoolExecutor(10);
        // 或：Executors.newScheduledThreadPool(10)
    }

    /**
     * 5. WorkStealingPool（JDK 8+）
     *
     * 特点：
     * - 使用 ForkJoinPool
     * - 工作窃取算法
     * - 适合任务分治场景
     */
    // ExecutorService workStealingPool = Executors.newWorkStealingPool();

    // ==================== 任务队列类型 ====================
    /**
     * 队列选择对线程池行为的影响：
     *
     * ┌──────────────────────────┬─────────────────────────────────┐
     * │ 队列类型                  │ 特点                             │
     * ├──────────────────────────┼─────────────────────────────────┤
     * │ SynchronousQueue         │ 不存储，直接交接，可能创建大量线程 │
     * │ LinkedBlockingQueue      │ 无界队列，可能导致OOM            │
     * │ ArrayBlockingQueue       │ 有界队列，需指定容量              │
     * │ PriorityBlockingQueue    │ 优先级队列                       │
     * │ DelayQueue               │ 延迟队列                         │
     * └──────────────────────────┴─────────────────────────────────┘
     */

    // ==================== 拒绝策略 ====================
    /**
     * 当队列满且线程数达到最大值时，触发拒绝策略
     *
     * 内置策略：
     * 1. AbortPolicy（默认）- 抛出 RejectedExecutionException
     * 2. CallerRunsPolicy - 由调用线程执行任务
     * 3. DiscardPolicy - 直接丢弃，不抛异常
     * 4. DiscardOldestPolicy - 丢弃队列最老的任务，重新提交
     *
     * 自定义策略：实现 RejectedExecutionHandler 接口
     */
    static class CustomRejectedHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 自定义处理逻辑
            System.out.println("任务被拒绝，记录日志或存入数据库重试");
            // 可以调用者运行
            if (!executor.isShutdown()) {
                r.run();
            }
        }
    }

    // ==================== 推荐的线程池创建方式 ====================
    /**
     * 根据业务类型创建合适的线程池
     */
    static class ThreadPoolConfig {

        /**
         * CPU密集型任务
         * - 任务主要是计算
         * - 建议：核心线程数 = CPU核心数 + 1
         */
        static ExecutorService cpuIntensivePool() {
            int coreSize = Runtime.getRuntime().availableProcessors() + 1;
            return new ThreadPoolExecutor(
                coreSize,
                coreSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000), // 有界队列
                new ThreadPoolExecutor.AbortPolicy()
            );
        }

        /**
         * IO密集型任务
         * - 任务主要是IO等待（网络请求、文件读写）
         * - 建议：核心线程数 = CPU核心数 * 2
         * - 实际可根据IO等待时间调整
         */
        static ExecutorService ioIntensivePool() {
            int coreSize = Runtime.getRuntime().availableProcessors() * 2;
            return new ThreadPoolExecutor(
                coreSize,
                coreSize * 2, // IO等待时可以更多线程
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(500),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
        }

        /**
         * 混合型任务
         * - 既有计算又有IO
         * - 建议：根据计算和IO占比调整
         * - 公式：线程数 = CPU核心数 * (1 + 等待时间/计算时间)
         */
        static ExecutorService mixedPool() {
            // 假设等待时间:计算时间 = 2:1
            int coreSize = (int) (Runtime.getRuntime().availableProcessors() * (1 + 2));
            return new ThreadPoolExecutor(
                coreSize,
                coreSize * 2,
                120L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200),
                new CustomRejectedHandler()
            );
        }
    }

    // ==================== 线程池监控 ====================
    /**
     * 监控指标
     */
    static void monitorThreadPool(ThreadPoolExecutor executor) {
        System.out.println("核心线程数：" + executor.getCorePoolSize());
        System.out.println("最大线程数：" + executor.getMaximumPoolSize());
        System.out.println("当前线程数：" + executor.getPoolSize());
        System.out.println("活跃线程数：" + executor.getActiveCount());
        System.out.println("已完成任务数：" + executor.getCompletedTaskCount());
        System.out.println("队列大小：" + executor.getQueue().size());
    }

    // ==================== 优雅关闭 ====================
    /**
     * 线程池关闭的正确方式
     */
    static void shutdownGracefully(ExecutorService executor) {
        // 1. 停止接收新任务
        executor.shutdown();

        try {
            // 2. 等待已提交任务完成
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                // 3. 超时后强制关闭
                executor.shutdownNow();

                // 4. 再次等待
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("线程池未完全关闭");
                }
            }
        } catch (InterruptedException e) {
            // 5. 当前线程被中断，强制关闭
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ==================== CompletableFuture（JDK 8+）====================
    /**
     * CompletableFuture：异步编程的利器
     *
     * 优势：
     * 1. 链式调用，代码简洁
     * 2. 异常处理方便
     * 3. 多个Future组合
     */
    static class CompletableFutureDemo {
        public static void main(String[] args) {
            ExecutorService executor = Executors.newFixedThreadPool(10);

            // 异步执行
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                // 模拟耗时操作
                return "Hello";
            }, executor);

            // 链式调用
            CompletableFuture<String> result = future
                .thenApply(s -> s + " World")     // 同步转换
                .thenApplyAsync(String::toUpperCase, executor) // 异步转换
                .exceptionally(ex -> "ERROR: " + ex.getMessage()); // 异常处理

            // 组合多个Future
            CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "A");
            CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "B");

            // 两个都完成后合并
            CompletableFuture<String> combined = future1.thenCombine(future2, (a, b) -> a + b);

            // 任一完成即可
            CompletableFuture<Object> anyOf = CompletableFuture.anyOf(future1, future2);

            // 全部完成
            CompletableFuture<Void> allOf = CompletableFuture.allOf(future1, future2);

            executor.shutdown();
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5, 10, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            executor.submit(() -> {
                System.out.println("任务 " + taskId + " 执行中，线程：" +
                    Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        Thread.sleep(1000);
        monitorThreadPool(executor);

        shutdownGracefully(executor);
    }
}
