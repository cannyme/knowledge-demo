package com.example.demo.database.redis;

/**
 * Redis核心数据结构与应用场景
 *
 * 【Redis特点】
 * 1. 基于内存，速度快（单线程QPS可达10万+）
 * 2. 支持持久化（RDB、AOF）
 * 3. 支持多种数据结构
 * 4. 支持主从复制、哨兵、集群
 *
 * 【为什么Redis快】
 * 1. 纯内存操作
 * 2. 单线程模型，避免上下文切换
 * 3. IO多路复用（epoll）
 * 4. 高效的数据结构
 */
public class RedisDataStructures {

    // ==================== String 字符串 ====================
    /**
     * 底层实现：SDS（Simple Dynamic String）
     *
     * 应用场景：
     * 1. 缓存对象（JSON序列化）
     * 2. 计数器
     * 3. 分布式锁
     * 4. 分布式ID生成
     */
    /*
    # 设置值
    SET user:1 '{"name":"张三","age":25}'
    SETNX lock:order 1          # 不存在才设置（分布式锁）
    SET lock:order 1 EX 30 NX   # 设置并添加过期时间

    # 计数器
    INCR page:view:home         # 自增
    INCRBY article:read:123 5   # 增加指定值

    # 获取值
    GET user:1
    MGET user:1 user:2          # 批量获取
    */

    // ==================== Hash 哈希 ====================
    /**
     * 底层实现：ziplist（小数据量）/ hashtable（大数据量）
     *
     * 应用场景：
     * 1. 对象存储（比String更灵活，可以只修改某个字段）
     * 2. 购物车
     */
    /*
    # 存储用户对象
    HSET user:1 name "张三" age 25 email "zhangsan@example.com"
    HGET user:1 name            # 获取单个字段
    HGETALL user:1              # 获取所有字段
    HMSET user:1 name "李四" age 26  # 设置多个字段

    # 购物车
    HSET cart:user:1 item:1001 2    # 商品1001，数量2
    HINCRBY cart:user:1 item:1001 1 # 增加数量
    HDEL cart:user:1 item:1001      # 删除商品
    HLEN cart:user:1                # 购物车商品数量
    */

    // ==================== List 列表 ====================
    /**
     * 底层实现：quicklist（ziplist + linkedlist）
     *
     * 应用场景：
     * 1. 消息队列
     * 2. 最新列表
     * 3. 时间线
     */
    /*
    # 消息队列（生产者-消费者）
    LPUSH queue:order '{"orderId":123}'   # 左边推入
    RPOP queue:order                       # 右边弹出
    BRPOP queue:order 10                   # 阻塞弹出（10秒超时）

    # 最新文章列表
    LPUSH articles:latest article:100
    LTRIM articles:latest 0 99             # 只保留最新100篇
    LRANGE articles:latest 0 9             # 获取最新10篇
    */

    // ==================== Set 集合 ====================
    /**
     * 底层实现：intset（整数）/ hashtable
     *
     * 应用场景：
     * 1. 标签系统
     * 2. 共同关注
     * 3. 抽奖
     */
    /*
    # 用户标签
    SADD user:1:tags "90后" "程序员" "游戏"
    SMEMBERS user:1:tags              # 获取所有标签
    SISMEMBER user:1:tags "程序员"    # 判断是否有某标签

    # 共同关注
    SADD user:1:following user:2 user:3 user:4
    SADD user:2:following user:3 user:4 user:5
    SINTER user:1:following user:2:following  # 共同关注
    SDIFF user:1:following user:2:following   # 差集
    SUNION user:1:following user:2:following  # 并集

    # 抽奖
    SADD lottery:2024 user:1 user:2 user:3
    SRANDMEMBER lottery:2024 1         # 随机抽1人（不移除）
    SPOP lottery:2024 1                # 随机抽1人（移除）
    */

    // ==================== ZSet 有序集合 ====================
    /**
     * 底层实现：ziplist（小数据量）/ skiplist + dict（大数据量）
     *
     * 应用场景：
     * 1. 排行榜
     * 2. 延时队列
     * 3. 范围查询
     */
    /*
    # 排行榜
    ZADD leaderboard:score 100 user:1 200 user:2 150 user:3
    ZINCRBY leaderboard:score 50 user:1    # 增加分数
    ZREVRANGE leaderboard:score 0 9 WITHSCORES  # 降序前10名
    ZRANK leaderboard:score user:1         # 获取排名

    # 延时队列（订单超时取消）
    # score为订单过期时间戳
    ZADD delay:queue 1704067200 order:123
    # 消费者定时检查
    ZRANGEBYSCORE delay:queue 0 {当前时间戳} LIMIT 0 100
    ZREM delay:queue order:123             # 处理后移除
    */

    // ==================== Bitmap 位图 ====================
    /**
     * 基于 String 实现，支持位操作
     *
     * 应用场景：
     * 1. 用户签到
     * 2. 在线状态
     * 3. 布隆过滤器
     */
    /*
    # 用户签到（一年365天）
    SETBIT user:1:signin:2024 0 1    # 第1天签到
    SETBIT user:1:signin:2024 364 1  # 第365天签到
    GETBIT user:1:signin:2024 0      # 检查第1天是否签到
    BITCOUNT user:1:signin:2024      # 统计签到天数

    # 在线用户统计
    SETBIT online:20240101 user:1 1  # 用户1在线
    BITCOUNT online:20240101         # 在线用户数
    */

    // ==================== HyperLogLog ====================
    /**
     * 用于基数统计（不精确，误差约0.81%）
     * 每个key只需12KB内存
     *
     * 应用场景：
     * 1. UV统计
     * 2. IP去重计数
     */
    /*
    # 页面UV统计
    PFADD page:home:uv user:1 user:2 user:3
    PFCOUNT page:home:uv              # 获取UV数
    PFMERGE total:uv page:home:uv page:detail:uv  # 合并多个UV
    */

    // ==================== Stream ====================
    /**
     * Redis 5.0 新增，消息队列
     * 支持消费者组、消息确认、持久化
     */
    /*
    # 生产者
    XADD mystream * field1 value1

    # 消费者组
    XGROUP CREATE mystream mygroup $

    # 消费者读取
    XREADGROUP GROUP mygroup consumer1 COUNT 1 BLOCK 2000 STREAMS mystream >

    # 确认消息
    XACK mystream mygroup 1526569495631-0
    */
}
