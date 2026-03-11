package com.example.demo.database.mysql;

import java.util.*;

/**
 * 分库分表策略
 *
 * 【为什么需要分库分表】
 * 1. 单表数据量过大（>1000万），查询性能下降
 * 2. 单库连接数不足
 * 3. 单机存储容量限制
 *
 * 【分库 vs 分表】
 * 分库：解决连接数、存储容量问题
 * 分表：解决单表数据量问题
 *
 * 【分片策略】
 * 1. 垂直分片：按业务拆分
 * 2. 水平分片：按数据拆分
 */
public class Sharding {

    // ==================== 垂直分片 ====================

    /**
     * 垂直分库：按业务拆分
     *
     * 拆分前：
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    单库 (shop_db)                            │
     * │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐          │
     * │  │用户表    │ │订单表   │ │商品表    │ │支付表   │           │
     * │  └─────────┘ └─────────┘ └─────────┘ └─────────┘          │
     * └─────────────────────────────────────────────────────────────┘
     *
     * 拆分后：
     * ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
     * │  user_db    │  │  order_db   │  │ product_db  │  │  pay_db     │
     * │  用户相关表  │  │  订单相关表  │  │  商品相关表  │  │  支付相关表  │
     * └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘
     *
     * 优点：
     * 1. 业务隔离，互不影响
     * 2. 便于微服务拆分
     * 3. 故障隔离
     *
     * 缺点：
     * 1. 跨库JOIN复杂
     * 2. 分布式事务问题
     */

    /**
     * 垂直分表：按字段拆分
     *
     * 拆分前：
     * ┌─────────────────────────────────────────────────────────────┐
     * │                        user表                                │
     * │  id, name, password, email, phone, avatar, intro, ...      │
     * │  (字段很多，有些字段很大)                                      │
     * └─────────────────────────────────────────────────────────────┘
     *
     * 拆分后：
     * ┌─────────────────────────┐  ┌─────────────────────────────┐
     * │       user_base         │  │        user_detail          │
     * │  id, name, password     │  │  user_id, avatar, intro...  │
     * │  (基础信息，访问频繁)     │  │  (详情信息，访问较少)         │
     * └─────────────────────────┘  └─────────────────────────────┘
     *
     * 优点：
     * 1. 减少单表字段数
     * 2. 减少IO（只查需要的表）
     * 3. 便于冷热数据分离
     */

    // ==================== 水平分片 ====================

    /**
     * 水平分库分表
     *
     * 拆分前：
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    order表 (1亿条数据)                       │
     * └─────────────────────────────────────────────────────────────┘
     *
     * 拆分后：
     * ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
     * │  order_db_0   │  │  order_db_1   │  │  order_db_2   │
     * │  order_0      │  │  order_0      │  │  order_0      │
     * │  order_1      │  │  order_1      │  │  order_1      │
     * │  ...          │  │  ...          │  │  ...          │
     * └───────────────┘  └───────────────┘  └───────────────┘
     *
     * 分片维度：
     * - 3个分库 × 2个分表 = 6个分片
     */

    // ==================== 分片键选择 ====================

    /**
     * 分片键选择原则：
     *
     * ┌──────────────────┬─────────────────────────────────────────┐
     * │ 分片键            │ 适用场景                                 │
     * ├──────────────────┼─────────────────────────────────────────┤
     * │ 用户ID           │ 电商订单、用户相关业务                    │
     * │ 订单ID           │ 订单系统，按订单查询                      │
     * │ 时间             │ 日志、流水类数据                          │
     * │ 地区             │ 地域性强的业务                            │
     * └──────────────────┴─────────────────────────────────────────┘
     *
     * 选择原则：
     * 1. 高频查询字段
     * 2. 数据分布均匀
     * 3. 业务关联性强
     * 4. 避免跨分片查询
     */

    // ==================== 分片算法 ====================

    /**
     * 1. Hash取模
     *
     * 分片位置 = shard_key % 分片数
     *
     * 优点：数据分布均匀
     * 缺点：扩容困难，需要数据迁移
     */
    static class HashSharding {
        public static int getShardIndex(long userId, int shardCount) {
            return (int) (userId % shardCount);
        }

        // 示例：用户ID % 4
        // userId=100 → 分片0
        // userId=101 → 分片1
        // userId=102 → 分片2
        // userId=103 → 分片3
    }

    /**
     * 2. 范围分片
     *
     * 按范围划分分片
     *
     * 优点：扩容简单，只需增加新区间
     * 缺点：可能数据分布不均
     */
    static class RangeSharding {
        public static int getShardIndex(long orderId) {
            if (orderId < 1000000) return 0;
            if (orderId < 2000000) return 1;
            if (orderId < 3000000) return 2;
            return 3;
        }

        // 示例：
        // [0, 100万) → 分片0
        // [100万, 200万) → 分片1
        // [200万, 300万) → 分片2
        // [300万, +∞) → 分片3
    }

    /**
     * 3. 一致性Hash
     *
     * 优点：扩容时只影响相邻节点
     * 缺点：实现复杂
     */
    static class ConsistentHashSharding {
        // 虚拟节点解决数据倾斜
        private static final int VIRTUAL_NODES = 150;
        private static final TreeMap<Integer, Integer> ring = new TreeMap<>();

        static {
            // 初始化环
            for (int i = 0; i < 4; i++) {  // 4个分片
                for (int j = 0; j < VIRTUAL_NODES; j++) {
                    int hash = hash("shard_" + i + "_" + j);
                    ring.put(hash, i);
                }
            }
        }

        public static int getShardIndex(long key) {
            int hash = hash(String.valueOf(key));
            Map.Entry<Integer, Integer> entry = ring.ceilingEntry(hash);
            if (entry == null) {
                entry = ring.firstEntry();
            }
            return entry.getValue();
        }

        private static int hash(String key) {
            return key.hashCode() & Integer.MAX_VALUE;
        }
    }

    /**
     * 4. 基因法
     *
     * 将分片基因嵌入ID，保证关联数据在同一分片
     *
     * 场景：订单和订单明细
     * - 订单按user_id分片
     * - 订单明细也要在同一分片
     * - 解决：将user_id的分片基因嵌入order_id
     */
    static class GeneSharding {
        // 假设4个分片，需要2位基因
        private static final int SHARD_COUNT = 4;
        private static final int GENE_BITS = 2;

        // 生成分片基因
        public static long getGene(long userId) {
            return userId % SHARD_COUNT;
        }

        // 将基因嵌入订单ID
        public static long generateOrderId(long sequence, long userId) {
            long gene = getGene(userId);
            return (sequence << GENE_BITS) | gene;
        }

        // 从订单ID提取基因
        public static long extractGene(long orderId) {
            return orderId & ((1 << GENE_BITS) - 1);
        }

        // 示例：
        // userId=100, gene=0
        // orderId=xxx0 (末尾嵌入基因0)
        // 查询时可以根据orderId定位到分片0
    }

    // ==================== ShardingSphere配置 ====================

    /**
     * ShardingSphere-JDBC配置示例
     */
    /*
    spring:
      shardingsphere:
        datasource:
          names: ds0,ds1
          ds0:
            type: com.zaxxer.hikari.HikariDataSource
            driver-class-name: com.mysql.cj.jdbc.Driver
            jdbc-url: jdbc:mysql://localhost:3306/order_db_0
          ds1:
            type: com.zaxxer.hikari.HikariDataSource
            driver-class-name: com.mysql.cj.jdbc.Driver
            jdbc-url: jdbc:mysql://localhost:3306/order_db_1

        rules:
          sharding:
            tables:
              t_order:
                actual-data-nodes: ds$->{0..1}.t_order_$->{0..1}
                table-strategy:
                  standard:
                    sharding-column: order_id
                    sharding-algorithm-name: t_order_inline
                key-generate-strategy:
                  column: order_id
                  key-generator-name: snowflake

            sharding-algorithms:
              t_order_inline:
                type: INLINE
                props:
                  algorithm-expression: t_order_$->{order_id % 2}

            key-generators:
              snowflake:
                type: SNOWFLAKE
    */

    // ==================== 分库分表问题 ====================

    /**
     * 1. 跨分片JOIN
     *
     * 解决方案：
     * - 数据冗余：将关联数据冗余存储
     * - 全局表：小表广播到所有分片
     * - 绑定表：关联表使用相同分片键
     * - 应用层JOIN：分别查询后代码组装
     */

    /**
     * 2. 跨分片排序
     *
     * 解决方案：
     * - 各分片排序后归并
     * - 使用搜索引擎（ES）辅助
     */

    /**
     * 3. 分布式事务
     *
     * 解决方案：
     * - XA事务（强一致性，性能差）
     * - TCC（柔性事务）
     * - Seata AT模式
     * - 本地消息表
     */

    /**
     * 4. 全局唯一ID
     *
     * 解决方案：
     * - UUID：无序，索引性能差
     * - 雪花算法：推荐
     * - 数据库号段模式
     * - Redis自增
     */

    /**
     * 5. 数据迁移
     *
     * 迁移步骤：
     * 1. 双写（新旧库同时写入）
     * 2. 历史数据迁移
     * 3. 数据校验
     * 4. 切换读流量
     * 5. 停止双写
     */

    // ==================== 最佳实践 ====================

    /**
     * 分库分表时机：
     * 1. 单表数据 > 1000万
     * 2. 单库连接数不足
     * 3. 单机存储不足
     *
     * 替代方案（优先考虑）：
     * 1. 冷热数据分离
     * 2. 读写分离
     * 3. 归档历史数据
     * 4. 使用NoSQL（ES、MongoDB）
     *
     * 设计原则：
     * 1. 分片键选择合理
     * 2. 避免跨分片查询
     * 3. 预留扩容空间
     * 4. 考虑数据倾斜问题
     */
}
