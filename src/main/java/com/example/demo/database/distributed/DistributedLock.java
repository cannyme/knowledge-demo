package com.example.demo.database.distributed;

import java.util.*;
import java.util.concurrent.*;

/**
 * 分布式锁实现方案
 *
 * 【为什么需要分布式锁】
 * 单机环境可以用 synchronized、ReentrantLock 保证线程安全。
 * 但在分布式系统中，多个服务实例部署在不同机器上，
 * 需要跨进程的锁机制来保证资源访问的互斥性。
 *
 * 【常见实现方案】
 * 1. 数据库（唯一索引）
 * 2. Redis（SETNX / Redlock）
 * 3. ZooKeeper（临时节点）
 * 4. etcd
 *
 * 【分布式锁要求】
 * 1. 互斥性：任意时刻只有一个客户端持有锁
 * 2. 防死锁：锁必须能释放（超时释放）
 * 3. 高可用：锁服务稳定
 * 4. 可重入：同一线程可多次获取同一把锁
 * 5. 防误删：只能删除自己加的锁
 */
public class DistributedLock {

    // ==================== 方案一：Redis SETNX ====================
    /**
     * Redis实现分布式锁
     *
     * 基本原理：
     * SET key value NX PX milliseconds
     * - NX：key不存在才设置
     * - PX：设置过期时间（毫秒）
     *
     * 问题与解决：
     * ┌──────────────────┬─────────────────────────────────┐
     * │ 问题              │ 解决方案                         │
     * ├──────────────────┼─────────────────────────────────┤
     * │ 死锁              │ 设置过期时间                      │
     * │ 锁过期时间不好估计 │ 看门狗机制（自动续期）             │
     * │ 误删别人的锁       │ value存唯一标识，删除时校验        │
     * │ 删除操作非原子     │ Lua脚本                          │
     * │ Redis主从切换丢锁  │ Redlock算法                       │
     * └──────────────────┴─────────────────────────────────┘
     */
    static class RedisDistributedLock {

        // private final RedisTemplate<String, String> redisTemplate;

        private static final String LOCK_PREFIX = "lock:";
        private static final long DEFAULT_EXPIRE = 30000; // 30秒

        /**
         * 加锁
         * 使用SET NX EX原子操作
         */
        /*
        public boolean tryLock(String lockKey, String requestId, long expireTime) {
            String key = LOCK_PREFIX + lockKey;
            // Lua脚本或使用SET命令
            // SET key value NX PX expireTime
            Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, requestId, expireTime, TimeUnit.MILLISECONDS);
            return Boolean.TRUE.equals(result);
        }
        */

        /**
         * 解锁（Lua脚本保证原子性）
         *
         * 为什么用Lua？
         * 因为"判断value+删除key"需要是原子操作
         * 否则可能出现：
         * 1. 线程A判断value相等
         * 2. 锁过期，线程B获取锁
         * 3. 线程A删除了B的锁
         */
        /*
        public boolean releaseLock(String lockKey, String requestId) {
            String key = LOCK_PREFIX + lockKey;
            String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "    return redis.call('del', KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";
            Long result = redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key),
                requestId
            );
            return Long.valueOf(1).equals(result);
        }
        */

        /**
         * 看门狗（自动续期）
         *
         * 问题：业务执行时间 > 锁过期时间
         * 解决：后台线程定期延长锁过期时间
         *
         * Redisson的Watchdog机制：
         * - 默认过期时间30秒
         * - 每隔10秒（过期时间的1/3）检查锁是否还被持有
         * - 如果持有，延长过期时间到30秒
         */
        /*
        public void tryLockWithWatchdog(String lockKey, String requestId) {
            if (tryLock(lockKey, requestId, 30000)) {
                // 启动看门狗线程
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(() -> {
                    if (redisTemplate.hasKey(LOCK_PREFIX + lockKey)) {
                        redisTemplate.expire(LOCK_PREFIX + lockKey, 30, TimeUnit.SECONDS);
                    } else {
                        scheduler.shutdown();
                    }
                }, 10, 10, TimeUnit.SECONDS);
            }
        }
        */
    }

    // ==================== 方案二：Redisson ====================
    /**
     * Redisson：Redis分布式锁的最佳实践
     *
     * 优势：
     * 1. 封装完善，API简单
     * 2. 支持可重入锁
     * 3. 内置看门狗机制
     * 4. 支持多种锁类型
     */
    /*
    // 使用示例
    public void demo() {
        RedissonClient redisson = Redisson.create(config);

        // 获取锁
        RLock lock = redisson.getLock("myLock");

        try {
            // 尝试获取锁，最多等待100秒，锁过期时间10秒
            boolean acquired = lock.tryLock(100, 10, TimeUnit.SECONDS);
            if (acquired) {
                // 执行业务逻辑
            }
        } finally {
            // 只释放自己持有的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    */

    // ==================== 方案三：Redlock算法 ====================
    /**
     * Redlock：解决Redis主从切换丢锁问题
     *
     * 场景：
     * 1. 客户端A在Master获取了锁
     * 2. Master宕机，锁数据还没同步到Slave
     * 3. Slave升级为Master
     * 4. 客户端B也获取了同一把锁
     * → 两个客户端同时持有锁！
     *
     * Redlock方案：
     * 1. 获取当前时间戳
     * 2. 依次向N个Redis节点请求加锁
     * 3. 计算获取锁消耗的时间
     * 4. 如果在大多数节点(N/2+1)获取成功，且消耗时间<锁过期时间，则加锁成功
     * 5. 否则，向所有节点释放锁
     */
    /*
    public boolean tryRedlock(List<RedisClient> clients, String lockKey,
                              String requestId, long ttl) {
        int successCount = 0;
        long startTime = System.currentTimeMillis();

        for (RedisClient client : clients) {
            if (client.setNx(lockKey, requestId, ttl)) {
                successCount++;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        // 需要在有效时间内，且超过半数节点成功
        if (successCount >= clients.size() / 2 + 1 && elapsed < ttl) {
            return true;
        }

        // 失败则释放所有锁
        for (RedisClient client : clients) {
            client.del(lockKey, requestId);
        }
        return false;
    }
    */

    // ==================== 方案四：ZooKeeper ====================
    /**
     * ZooKeeper实现分布式锁
     *
     * 原理：利用ZooKeeper的临时顺序节点
     *
     * 实现步骤：
     * 1. 创建持久节点 /locks
     * 2. 客户端在 /locks 下创建临时顺序节点
     * 3. 检查自己是否是最小序号的节点
     *    - 是：获取锁成功
     *    - 否：监听前一个节点的删除事件
     * 4. 业务完成后，删除自己的节点
     *
     * 优点：
     * - 公平锁（按创建顺序获取）
     * - 避免惊群效应（只监听前一个节点）
     * - 天然防止死锁（临时节点随会话删除）
     *
     * 缺点：
     * - 性能不如Redis
     * - 需要维护ZooKeeper集群
     */
    /*
    public void zkLockDemo() {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkConnectString, retryPolicy);

        // 可重入锁
        InterProcessMutex lock = new InterProcessMutex(client, "/locks/myLock");

        try {
            // 获取锁
            lock.acquire(10, TimeUnit.SECONDS);

            // 执行业务

        } finally {
            // 释放锁
            lock.release();
        }
    }
    */

    // ==================== 方案对比 ====================
    /**
     * ┌─────────────┬─────────────────┬─────────────────┬─────────────────┐
     * │ 方案         │ 性能             │ 可靠性           │ 适用场景         │
     * ├─────────────┼─────────────────┼─────────────────┼─────────────────┤
     * │ Redis        │ 高               │ 中（可能丢锁）    │ 大多数场景       │
     * │ Redlock      │ 中               │ 高               │ 高可靠性要求     │
     * │ ZooKeeper    │ 低               │ 高               │ 强一致性要求     │
     * │ 数据库        │ 低               │ 中               │ 简单场景         │
     * └─────────────┴─────────────────┴─────────────────┴─────────────────┘
     *
     * 选择建议：
     * 1. 一般场景：Redisson（简单、性能好）
     * 2. 高可靠性：ZooKeeper 或 Redlock
     * 3. 已有数据库：数据库唯一索引（简单但性能差）
     */
}
