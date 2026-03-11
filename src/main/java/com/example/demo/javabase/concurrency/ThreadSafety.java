package com.example.demo.javabase.concurrency;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

/**
 * 并发编程基础 - 线程安全与同步机制
 *
 * 【线程安全的三个特性】
 * 1. 原子性 (Atomicity)：操作不可分割，要么全部成功，要么全部失败
 * 2. 可见性 (Visibility)：一个线程的修改对其他线程立即可见
 * 3. 有序性 (Ordering)：程序执行顺序与代码顺序一致
 *
 * 【Java提供的解决方案】
 * ┌─────────────────┬───────────────────────────────────────┐
 * │ 特性             │ 解决方案                               │
 * ├─────────────────┼───────────────────────────────────────┤
 * │ 原子性           │ synchronized, Lock, Atomic类          │
 * │ 可见性           │ volatile, synchronized, Lock, final   │
 * │ 有序性           │ volatile, synchronized, Lock          │
 * └─────────────────┴───────────────────────────────────────┘
 */
public class ThreadSafety {

    // ==================== 问题演示：线程不安全 ====================
    /**
     * 线程不安全的计数器
     * 问题：count++ 不是原子操作
     * 实际执行步骤：
     *   1. 读取 count 的值
     *   2. 将 count 加 1
     *   3. 将新值写回 count
     * 多线程环境下，两个线程可能同时读取相同的值，导致计数不准
     */
    static class UnsafeCounter {
        private int count = 0;

        public void increment() {
            count++; // 非原子操作！
        }

        public int getCount() {
            return count;
        }
    }

    // ==================== 方案一：synchronized 关键字 ====================
    /**
     * synchronized 实现原理：
     *
     * 1. 字节码层面
     *    - 方法级：ACC_SYNCHRONIZED 标志
     *    - 代码块：monitorenter / monitorexit 指令
     *
     * 2. 底层机制
     *    - 基于对象的 Monitor 实现
     *    - Monitor Enter：获取锁，计数器+1
     *    - Monitor Exit：释放锁，计数器-1
     *
     * 3. 锁升级（JDK 6+）
     *    无锁 → 偏向锁 → 轻量级锁 → 重量级锁
     *    （只升级不降级）
     */
    static class SynchronizedCounter {
        private int count = 0;

        // 方式1：同步方法
        public synchronized void increment() {
            count++;
        }

        // 方式2：同步代码块（更灵活，减小锁粒度）
        public void incrementWithBlock() {
            synchronized (this) {
                count++;
            }
        }

        // 不同对象作为锁，实现更细粒度的控制
        private final Object lock = new Object();

        public void incrementWithCustomLock() {
            synchronized (lock) {
                count++;
            }
        }

        public int getCount() {
            return count;
        }
    }

    // ==================== 方案二：ReentrantLock ====================
    /**
     * ReentrantLock vs synchronized
     *
     * ┌────────────────┬─────────────────────┬─────────────────────┐
     * │ 特性            │ synchronized        │ ReentrantLock       │
     * ├────────────────┼─────────────────────┼─────────────────────┤
     * │ 锁的获取/释放    │ JVM自动管理          │ 手动lock()/unlock() │
     * │ 可中断          │ 否                   │ 是（lockInterruptibly）│
     * │ 超时获取        │ 否                   │ 是（tryLock时间）   │
     * │ 公平锁          │ 否（只能非公平）      │ 是（可选）          │
     * │ 条件变量        │ 单个（wait/notify）  │ 多个（Condition）   │
     * │ 性能            │ JDK6后差异不大       │ 高竞争时略优        │
     * └────────────────┴─────────────────────┴─────────────────────┘
     *
     * 使用模板（必须try-finally！）：
     * lock.lock();
     * try {
     *     // 临界区代码
     * } finally {
     *     lock.unlock(); // 确保释放锁
     * }
     */
    static class ReentrantLockCounter {
        private final ReentrantLock lock = new ReentrantLock();
        // 公平锁：new ReentrantLock(true)
        // 公平锁保证等待时间最长的线程先获取锁，但性能略低
        private int count = 0;

        public void increment() {
            lock.lock();
            try {
                count++;
            } finally {
                lock.unlock();
            }
        }

        // 尝试获取锁（非阻塞）
        public boolean tryIncrement() {
            if (lock.tryLock()) {
                try {
                    count++;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false;
        }

        // 超时获取锁
        public boolean tryIncrementWithTimeout() throws InterruptedException {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    count++;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false;
        }

        public int getCount() {
            return count;
        }
    }

    // ==================== 方案三：Atomic 原子类 ====================
    /**
     * Atomic类实现原理：CAS (Compare-And-Swap)
     *
     * CAS 操作：
     * - 比较当前值与预期值
     * - 如果相等，更新为新值
     * - 如果不等，重试（自旋）
     *
     * 优点：
     * - 无锁并发，性能好
     * - 不会阻塞线程
     *
     * 缺点：
     * - ABA问题（可通过版本号解决）
     * - 长时间自旋消耗CPU
     * - 只能保证一个变量的原子性
     */
    static class AtomicCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        public void increment() {
            count.incrementAndGet();
            // 其他常用方法：
            // get() - 获取值
            // set() - 设置值
            // getAndIncrement() - 先获取后自增
            // compareAndSet(expect, update) - CAS操作
        }

        // 原子引用类型
        private final AtomicReference<String> ref = new AtomicReference<>("init");

        public void updateRef(String newValue) {
            ref.compareAndSet("init", newValue);
        }

        // 原子更新字段（需要配合 @Volatile 使用）
        // private volatile int flag;
        // private final AtomicIntegerFieldUpdater<AtomicCounter> flagUpdater =
        //     AtomicIntegerFieldUpdater.newUpdater(AtomicCounter.class, "flag");

        // LongAdder：高并发下的更好选择
        // 原理：分段累加，减少竞争
        private final LongAdder adder = new LongAdder();

        public void add() {
            adder.increment();
        }

        public long getSum() {
            return adder.sum(); // 最终一致性
        }

        public int getCount() {
            return count.get();
        }
    }

    // ==================== 方案四：volatile 关键字 ====================
    /**
     * volatile 的作用：
     * 1. 保证可见性（不保证原子性！）
     * 2. 禁止指令重排序
     *
     * 适用场景：
     * 1. 状态标志位
     * 2. 双重检查锁中的单例
     * 3. 独立观察（一个线程写，多个线程读）
     *
     * 不适用场景：
     * - count++ 这种复合操作（不保证原子性）
     */
    static class VolatileExample {
        // 状态标志：一个线程修改，其他线程立即可见
        private volatile boolean running = true;

        public void stop() {
            running = false; // 对其他线程立即可见
        }

        public void doWork() {
            while (running) {
                // 执行任务
            }
        }

        // ❌ 错误用法：volatile 不能保证原子性
        // private volatile int count = 0;
        // public void increment() {
        //     count++; // 仍然线程不安全！
        // }
    }

    // ==================== 方案五：ThreadLocal ====================
    /**
     * ThreadLocal：线程本地变量
     *
     * 每个线程持有变量的独立副本，互不干扰。
     *
     * 应用场景：
     * 1. 数据库连接（每个线程独立的Connection）
     * 2. 用户会话信息
     * 3. SimpleDateFormat（非线程安全）
     * 4. 请求上下文（Spring中存储Request信息）
     *
     * 内存泄漏问题：
     * - ThreadLocalMap 的 key 是弱引用，value 是强引用
     * - 线程池场景下，线程不销毁，value 无法回收
     * - 解决：使用后调用 remove() 方法
     */
    static class ThreadLocalExample {
        // 每个线程独立的变量
        private static final ThreadLocal<String> userContext = new ThreadLocal<>();

        // 使用 initialValue 设置初始值
        private static final ThreadLocal<Integer> requestId =
            ThreadLocal.withInitial(() -> 0);

        public void setUser(String user) {
            userContext.set(user);
        }

        public String getUser() {
            return userContext.get();
        }

        public void cleanup() {
            // 重要：使用完毕必须清理，防止内存泄漏
            userContext.remove();
            requestId.remove();
        }

        // InheritableThreadLocal：子线程可继承父线程的值
        private static final InheritableThreadLocal<String> inheritable =
            new InheritableThreadLocal<>();
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) throws InterruptedException {
        // 测试线程安全计数器
        final int THREAD_COUNT = 100;
        final int INCREMENT_PER_THREAD = 10000;

        // UnsafeCounter unsafeCounter = new UnsafeCounter();
        SynchronizedCounter safeCounter = new SynchronizedCounter();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < INCREMENT_PER_THREAD; j++) {
                    safeCounter.increment();
                }
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("预期结果：" + (THREAD_COUNT * INCREMENT_PER_THREAD));
        System.out.println("实际结果：" + safeCounter.getCount());
    }
}
