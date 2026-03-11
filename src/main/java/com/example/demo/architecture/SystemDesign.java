package com.example.demo.architecture;

import java.util.*;
import java.util.concurrent.*;

/**
 * 系统设计核心问题
 *
 * 【三大缓存问题】
 * 1. 缓存穿透：查询不存在的数据
 * 2. 缓存击穿：热点Key过期
 * 3. 缓存雪崩：大量Key同时过期
 *
 * 【其他核心问题】
 * 4. 分布式ID生成
 * 5. 秒杀系统设计
 */
public class SystemDesign {

    // ==================== 缓存穿透 ====================

    /**
     * 问题：大量请求查询不存在的数据，绕过缓存直接打到数据库
     *
     * 场景示例：
     * - 恶意请求 id=-1
     * - 查询不存在的用户
     *
     * 解决方案：
     */
    static class CachePenetrationSolution {

        /**
         * 方案1：缓存空对象（Null Caching）
         */
        /*
        public Object getWithNullCache(String key) {
            Object value = redis.get(key);
            if (value != null) {
                // 命中缓存（可能是空值标记）
                return "NULL".equals(value) ? null : value;
            }

            // 查询数据库
            value = db.query(key);

            if (value != null) {
                redis.set(key, value, 300);  // 正常过期时间
            } else {
                // 缓存空值，设置较短过期时间
                redis.set(key, "NULL", 60);
            }

            return value;
        }
        */

        /**
         * 方案2：布隆过滤器（Bloom Filter）
         *
         * 原理：位图 + 多个哈希函数
         * - 判断元素可能存在：可能误判（假阳性）
         * - 判断元素一定不存在：一定准确
         *
         * 适用场景：
         * - 数据量大
         * - 允许少量误判
         */
        /*
        public Object getWithBloomFilter(String key) {
            // 先检查布隆过滤器
            if (!bloomFilter.mightContain(key)) {
                // 一定不存在，直接返回
                return null;
            }

            // 可能存在，继续查询
            Object value = redis.get(key);
            if (value != null) {
                return value;
            }

            value = db.query(key);
            if (value != null) {
                redis.set(key, value);
            }

            return value;
        }
        */

        /**
         * 布隆过滤器实现示意
         */
        static class SimpleBloomFilter {
            private final BitSet bits;
            private final int size;
            private final int[] seeds = {3, 5, 7, 11, 13, 17, 19, 23};

            public SimpleBloomFilter(int size) {
                this.size = size;
                this.bits = new BitSet(size);
            }

            public void add(String value) {
                for (int seed : seeds) {
                    int hash = hash(value, seed);
                    bits.set(Math.abs(hash) % size);
                }
            }

            public boolean mightContain(String value) {
                for (int seed : seeds) {
                    int hash = hash(value, seed);
                    if (!bits.get(Math.abs(hash) % size)) {
                        return false;  // 一定不存在
                    }
                }
                return true;  // 可能存在
            }

            private int hash(String value, int seed) {
                int h = 0;
                for (char c : value.toCharArray()) {
                    h = h * seed + c;
                }
                return h;
            }
        }

        /**
         * 方案3：接口层校验
         * - 参数合法性校验
         * - 限流
         * - 黑名单
         */
    }

    // ==================== 缓存击穿 ====================

    /**
     * 问题：热点Key过期，大量请求同时穿透到数据库
     *
     * 与缓存穿透的区别：
     * - 穿透：Key不存在
     * - 击穿：Key存在但过期了
     */
    static class CacheBreakdownSolution {

        /**
         * 方案1：互斥锁（Mutex Lock）
         */
        /*
        public Object getWithMutex(String key) {
            Object value = redis.get(key);
            if (value != null) {
                return value;
            }

            String lockKey = "lock:" + key;

            try {
                // 尝试获取锁
                boolean locked = redis.setnx(lockKey, "1", 10);

                if (locked) {
                    // 获取锁成功，查询数据库
                    value = db.query(key);
                    redis.set(key, value, 300);
                } else {
                    // 获取锁失败，等待后重试
                    Thread.sleep(50);
                    return getWithMutex(key);  // 递归重试
                }
            } finally {
                redis.del(lockKey);
            }

            return value;
        }
        */

        /**
         * 方案2：逻辑过期（永不过期）
         */
        /*
        public Object getWithLogicalExpire(String key) {
            Object value = redis.get(key);
            if (value == null) {
                return null;
            }

            // 检查逻辑过期时间
            CacheData cacheData = (CacheData) value;
            if (cacheData.getExpireTime() > System.currentTimeMillis()) {
                return cacheData.getData();
            }

            // 已过期，异步刷新
            String lockKey = "lock:" + key;
            if (redis.setnx(lockKey, "1", 10)) {
                // 获取锁成功，异步刷新
                executor.submit(() -> {
                    Object newData = db.query(key);
                    CacheData newCache = new CacheData(newData,
                        System.currentTimeMillis() + 300000);
                    redis.set(key, newCache);
                    redis.del(lockKey);
                });
            }

            // 返回旧数据（即使过期）
            return cacheData.getData();
        }
        */

        /**
         * 方案3：热点Key永不过期
         * - 物理不过期
         * - 后台定时刷新
         */
    }

    // ==================== 缓存雪崩 ====================

    /**
     * 问题：大量Key同时过期，或Redis宕机
     */
    static class CacheAvalancheSolution {

        /**
         * 方案1：随机过期时间
         */
        /*
        public void setWithRandomExpire(String key, Object value, int baseExpire) {
            // 基础过期时间 + 随机时间
            int randomExpire = baseExpire + ThreadLocalRandom.current().nextInt(0, 300);
            redis.set(key, value, randomExpire);
        }
        */

        /**
         * 方案2：多级缓存
         *
         * ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
         * │ 客户端   │───→│  Nginx  │───→│  Redis  │───→│   DB    │
         * │ 本地缓存 │    │  缓存   │    │ 集群    │    │         │
         * └─────────┘    └─────────┘    └─────────┘    └─────────┘
         */
        /*
        public Object getWithMultiCache(String key) {
            // L1: 本地缓存（Caffeine）
            Object value = localCache.get(key);
            if (value != null) {
                return value;
            }

            // L2: Redis缓存
            value = redis.get(key);
            if (value != null) {
                localCache.put(key, value);
                return value;
            }

            // L3: 数据库
            value = db.query(key);
            if (value != null) {
                redis.set(key, value);
                localCache.put(key, value);
            }

            return value;
        }
        */

        /**
         * 方案3：熔断降级
         */
        /*
        @HystrixCommand(
            fallbackMethod = "getFallback",
            commandProperties = {
                @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "20"),
                @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")
            }
        )
        public Object get(String key) {
            return redis.get(key);
        }

        public Object getFallback(String key) {
            // 降级：返回默认值或从本地缓存读取
            return localCache.get(key);
        }
        */

        /**
         * 方案4：Redis高可用
         * - 主从复制 + 哨兵
         * - Redis Cluster
         */
    }

    // ==================== 分布式ID生成 ====================

    /**
     * 分布式ID要求：
     * 1. 全局唯一
     * 2. 趋势递增（利于索引）
     * 3. 高性能
     * 4. 高可用
     */
    static class DistributedIdGenerator {

        /**
         * 方案1：UUID
         *
         * 优点：简单、本地生成
         * 缺点：无序、太长、不利于索引
         */
        public static String uuid() {
            return java.util.UUID.randomUUID().toString().replace("-", "");
        }

        /**
         * 方案2：数据库自增
         *
         * 优点：简单、递增
         * 缺点：单点问题、性能瓶颈
         *
         * 优化：步长模式
         * - 服务器A: 1, 11, 21, 31...
         * - 服务器B: 2, 12, 22, 32...
         */

        /**
         * 方案3：Redis自增
         */
        /*
        public long nextId(String key) {
            return redis.incr(key);
        }

        // 带日期前缀
        public String nextIdWithDate(String prefix) {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String key = prefix + ":" + date;
            long seq = redis.incr(key);
            redis.expire(key, 86400);  // 一天过期
            return date + String.format("%06d", seq);
        }
        */

        /**
         * 方案4：雪花算法（Snowflake）推荐
         *
         * 结构：0 - 41位时间戳 - 10位机器ID - 12位序列号
         *
         * ┌─────────────────────────────────────────────────────────────┐
         * │ 1位符号 │    41位时间戳    │ 10位机器ID  │   12位序列号    │
         * │   0    │   (毫秒级)        │  (5位数据中心+5位机器) │ (同毫秒内的序列) │
         * └─────────────────────────────────────────────────────────────┘
         *
         * 优点：
         * - 本地生成，高性能
         * - 趋势递增
         * - 不依赖外部系统
         *
         * 缺点：
         * - 时钟回拨问题
         * - 机器ID需要分配
         */
        static class SnowflakeIdGenerator {
            private final long twepoch = 1640995200000L;  // 起始时间戳(2022-01-01)

            private final long workerIdBits = 5L;
            private final long datacenterIdBits = 5L;
            private final long maxWorkerId = ~(-1L << workerIdBits);
            private final long maxDatacenterId = ~(-1L << datacenterIdBits);
            private final long sequenceBits = 12L;

            private final long workerIdShift = sequenceBits;
            private final long datacenterIdShift = sequenceBits + workerIdBits;
            private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
            private final long sequenceMask = ~(-1L << sequenceBits);

            private long workerId;
            private long datacenterId;
            private long sequence = 0L;
            private long lastTimestamp = -1L;

            public SnowflakeIdGenerator(long workerId, long datacenterId) {
                if (workerId > maxWorkerId || workerId < 0) {
                    throw new IllegalArgumentException("worker Id error");
                }
                if (datacenterId > maxDatacenterId || datacenterId < 0) {
                    throw new IllegalArgumentException("datacenter Id error");
                }
                this.workerId = workerId;
                this.datacenterId = datacenterId;
            }

            public synchronized long nextId() {
                long timestamp = System.currentTimeMillis();

                // 时钟回拨处理
                if (timestamp < lastTimestamp) {
                    throw new RuntimeException("时钟回拨");
                }

                if (lastTimestamp == timestamp) {
                    // 同一毫秒内，序列号自增
                    sequence = (sequence + 1) & sequenceMask;
                    if (sequence == 0) {
                        // 序列号溢出，等待下一毫秒
                        timestamp = waitNextMillis(lastTimestamp);
                    }
                } else {
                    sequence = 0L;
                }

                lastTimestamp = timestamp;

                return ((timestamp - twepoch) << timestampLeftShift)
                    | (datacenterId << datacenterIdShift)
                    | (workerId << workerIdShift)
                    | sequence;
            }

            private long waitNextMillis(long lastTimestamp) {
                long timestamp = System.currentTimeMillis();
                while (timestamp <= lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
                return timestamp;
            }
        }

        /**
         * 方案5：号段模式（Leaf）
         *
         * 数据库表：
         * CREATE TABLE id_segment (
         *   biz_tag VARCHAR(64) PRIMARY KEY,
         *   max_id BIGINT,
         *   step INT,
         *   version INT
         * );
         *
         * 批量获取ID段，本地缓存在内存中
         * 用完后再次从数据库获取
         */
    }

    // ==================== 秒杀系统设计 ====================

    /**
     * 秒杀系统特点：
     * 1. 瞬时高并发
     * 2. 库存有限
     * 3. 防超卖
     * 4. 防刷
     */
    static class SeckillDesign {

        /**
         * 架构设计：
         *
         * ┌───────────┐    ┌───────────┐    ┌───────────┐    ┌───────────┐
         * │   用户    │───→│   CDN     │───→│   网关    │───→│  服务集群  │
         * │           │    │  静态资源  │    │  限流     │    │           │
         * └───────────┘    └───────────┘    └───────────┘    └───────────┘
         *                                                         │
         *                     ┌───────────────────────────────────┤
         *                     │                                   │
         *                     ▼                                   ▼
         *               ┌───────────┐                      ┌───────────┐
         *               │   Redis   │                      │    MQ     │
         *               │  库存扣减  │                      │  异步下单  │
         *               └───────────┘                      └───────────┘
         */

        /**
         * 核心流程：
         *
         * 1. 前端限流
         *    - 按钮置灰
         *    - 验证码
         *    - 答题
         *
         * 2. 网关限流
         *    - Nginx限流
         *    - 网关限流
         *
         * 3. 服务端限流
         *    - 令牌桶
         *    - 漏桶
         *
         * 4. 库存预热
         */
        /*
        // 秒杀开始前，将库存加载到Redis
        public void preloadStock(Long seckillId, Integer stock) {
            String key = "seckill:stock:" + seckillId;
            redis.set(key, stock);
        }
        */

        /**
         * 5. 库存扣减（原子操作）
         */
        /*
        // Lua脚本保证原子性
        String luaScript =
            "if redis.call('get', KEYS[1]) <= 0 then " +
            "    return 0 " +
            "end " +
            "redis.call('decr', KEYS[1]) " +
            "return 1";

        public boolean deductStock(Long seckillId) {
            String key = "seckill:stock:" + seckillId;
            Long result = redis.execute(luaScript, Collections.singletonList(key));
            return result == 1;
        }
        */

        /**
         * 6. 异步下单
         */
        /*
        public Result seckill(Long userId, Long seckillId) {
            // 1. 令牌校验
            if (!checkToken(userId, seckillId)) {
                return Result.fail("非法请求");
            }

            // 2. 库存扣减
            if (!deductStock(seckillId)) {
                return Result.fail("库存不足");
            }

            // 3. 发送MQ消息（异步创建订单）
            OrderMessage message = new OrderMessage(userId, seckillId);
            rocketMQTemplate.asyncSend("order-topic", message, new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    log.info("下单消息发送成功");
                }

                @Override
                public void onException(Throwable e) {
                    // 消息发送失败，回滚库存
                    rollbackStock(seckillId);
                }
            });

            // 4. 返回排队中
            return Result.success("排队中");
        }
        */

        /**
         * 防超卖方案：
         *
         * 1. Redis原子扣减
         * 2. 数据库乐观锁
         */
        /*
        // 数据库乐观锁
        UPDATE seckill_goods
        SET stock = stock - 1, version = version + 1
        WHERE seckill_id = ? AND stock > 0 AND version = ?
        */

        /**
         * 防刷方案：
         *
         * 1. 验证码
         * 2. 答题
         * 3. 用户限购（Redis记录已购买用户）
         * 4. IP限流
         * 5. 黑名单
         */
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // 测试雪花算法
        DistributedIdGenerator.SnowflakeIdGenerator generator = new DistributedIdGenerator.SnowflakeIdGenerator(1, 1);
        for (int i = 0; i < 10; i++) {
            System.out.println(generator.nextId());
        }

        // 测试布隆过滤器
        CachePenetrationSolution.SimpleBloomFilter filter = new CachePenetrationSolution.SimpleBloomFilter(10000);
        filter.add("user:1");
        filter.add("user:2");

        System.out.println("user:1 存在？" + filter.mightContain("user:1"));
        System.out.println("user:3 存在？" + filter.mightContain("user:3"));
    }
}
