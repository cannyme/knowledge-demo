package com.example.demo.database.mysql;

/**
 * MySQL索引优化
 *
 * 【索引本质】
 * 索引是帮助MySQL高效获取数据的数据结构。
 * InnoDB使用B+树作为索引结构。
 *
 * 【B+树特点】
 * 1. 非叶子节点不存储数据，只存储键值
 * 2. 叶子节点存储所有数据，并通过指针连接（便于范围查询）
 * 3. 树高度低（通常3层），减少磁盘IO
 *
 * 【索引类型】
 * ┌──────────────────┬─────────────────────────────────────────┐
 * │ 类型              │ 说明                                     │
 * ├──────────────────┼─────────────────────────────────────────┤
 * │ 主键索引          │ 聚簇索引，叶子节点存储完整行数据           │
 * │ 二级索引          │ 非聚簇索引，叶子节点存储主键值             │
 * │ 联合索引          │ 多列组合索引，遵循最左前缀原则             │
 * │ 唯一索引          │ 列值唯一，允许一个NULL                    │
 * │ 全文索引          │ 用于文本搜索                              │
 * └──────────────────┴─────────────────────────────────────────┘
 */
public class MySQLIndex {

    // ==================== 建表语句示例 ====================
    /*
    -- 用户表
    CREATE TABLE `user` (
        `id` BIGINT NOT NULL AUTO_INCREMENT,
        `name` VARCHAR(50) NOT NULL,
        `email` VARCHAR(100),
        `age` INT,
        `department_id` BIGINT,
        `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
        PRIMARY KEY (`id`),                    -- 主键索引
        UNIQUE KEY `uk_email` (`email`),       -- 唯一索引
        KEY `idx_name_age` (`name`, `age`),    -- 联合索引
        KEY `idx_department` (`department_id`) -- 普通索引
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    */

    // ==================== 最左前缀原则 ====================
    /**
     * 联合索引 idx_name_age(name, age)
     *
     * 【有效使用】
     * WHERE name = '张三'                  ✅ 使用索引
     * WHERE name = '张三' AND age = 25     ✅ 使用索引
     * WHERE name = '张三' AND age > 20     ✅ 使用索引（name定位，age范围扫描）
     *
     * 【无法使用】
     * WHERE age = 25                       ❌ 不满足最左前缀
     * WHERE name LIKE '%张'                ❌ 前缀模糊匹配
     * WHERE name = '张三' OR age = 25      ❌ OR破坏索引使用
     *
     * 【部分使用】
     * WHERE name LIKE '张%' AND age = 25   ✅ name用索引，age可能用到
     * WHERE name = '张三' ORDER BY age     ✅ name用索引，排序也用索引
     */

    // ==================== 索引失效场景 ====================
    /**
     * 1. 对索引列使用函数或运算
     *    ❌ WHERE YEAR(create_time) = 2024
     *    ✅ WHERE create_time >= '2024-01-01' AND create_time < '2025-01-01'
     *
     * 2. 隐式类型转换
     *    ❌ WHERE phone = 13800138000      (phone是VARCHAR)
     *    ✅ WHERE phone = '13800138000'
     *
     * 3. LIKE 以通配符开头
     *    ❌ WHERE name LIKE '%张%'
     *    ✅ WHERE name LIKE '张%'
     *
     * 4. OR 条件（除非所有列都有索引）
     *    ❌ WHERE name = '张三' OR age = 25
     *
     * 5. NOT IN, NOT EXISTS, <>
     *    ❌ WHERE age NOT IN (1, 2, 3)
     *
     * 6. 索引列允许NULL
     *    - NULL值会影响索引统计，建议设置NOT NULL
     *
     * 7. 联合索引中间列使用范围查询
     *    KEY idx_a_b_c(a, b, c)
     *    ❌ WHERE a = 1 AND b > 2 AND c = 3  (c无法使用索引)
     */

    // ==================== 聚簇索引 vs 非聚簇索引 ====================
    /**
     * 聚簇索引（主键索引）
     * - 叶子节点存储完整行数据
     * - 一张表只能有一个聚簇索引
     * - 数据按主键顺序存储
     *
     * 非聚簇索引（二级索引）
     * - 叶子节点存储主键值
     * - 查询需要"回表"：先查二级索引获取主键，再查聚簇索引获取完整数据
     *
     * 回表示例：
     * SELECT * FROM user WHERE name = '张三';
     * 1. 在idx_name索引中找到 name='张三' 对应的主键id
     * 2. 根据id去主键索引中查找完整行数据
     *
     * 覆盖索引：查询的列都在索引中，无需回表
     * SELECT id, name FROM user WHERE name = '张三';  ✅ 覆盖索引
     * SELECT * FROM user WHERE name = '张三';         ❌ 需要回表
     */

    // ==================== EXPLAIN 分析 ====================
    /**
     * EXPLAIN SELECT * FROM user WHERE name = '张三';
     *
     * 重要字段：
     *
     * 1. type：访问类型（从好到差）
     *    system   - 单行数据
     *    const    - 主键或唯一索引
     *    eq_ref   - JOIN时使用主键或唯一索引
     *    ref      - 非唯一索引
     *    range    - 范围扫描
     *    index    - 全索引扫描
     *    ALL      - 全表扫描（最差）
     *
     * 2. key：实际使用的索引
     *
     * 3. key_len：使用的索引长度（判断联合索引使用了多少列）
     *
     * 4. rows：预估扫描行数
     *
     * 5. Extra：额外信息
     *    Using index      - 覆盖索引
     *    Using where      - WHERE过滤
     *    Using temporary  - 使用临时表（需要优化）
     *    Using filesort   - 文件排序（需要优化）
     */

    // ==================== 索引优化策略 ====================
    /**
     * 1. 选择合适的列建索引
     *    - WHERE条件频繁使用的列
     *    - JOIN关联的列
     *    - ORDER BY / GROUP BY 的列
     *
     * 2. 联合索引设计原则
     *    - 将选择性高的列放前面
     *    - 考虑查询场景，尽量覆盖更多查询
     *
     * 3. 避免冗余索引
     *    KEY idx_a(a), KEY idx_a_b(a, b)  → idx_a 是冗余的
     *
     * 4. 前缀索引
     *    对长字符串列，可以使用前缀索引节省空间
     *    ALTER TABLE user ADD INDEX idx_email(email(10));
     *
     * 5. 索引下推（ICP，Index Condition Pushdown）
     *    MySQL 5.6+，将WHERE条件的过滤下推到存储引擎层
     *    KEY idx_name_age(name, age)
     *    WHERE name LIKE '张%' AND age = 25
     *    在存储引擎层就过滤掉不满足age条件的记录
     */

    // ==================== 索引维护 ====================
    /*
    -- 查看表索引
    SHOW INDEX FROM user;

    -- 分析索引使用情况
    ANALYZE TABLE user;

    -- 强制使用索引
    SELECT * FROM user FORCE INDEX(idx_name) WHERE name = '张三';

    -- 忽略索引
    SELECT * FROM user IGNORE INDEX(idx_name) WHERE name = '张三';
    */

    // ==================== 慢查询优化案例 ====================
    /**
     * 案例1：大偏移分页
     *
     * 问题SQL：
     * SELECT * FROM user LIMIT 1000000, 10;
     * 需要扫描前1000010行，丢弃前1000000行，效率极低
     *
     * 优化方案1：使用子查询
     * SELECT * FROM user u
     * INNER JOIN (SELECT id FROM user LIMIT 1000000, 10) t
     * ON u.id = t.id;
     * 先通过覆盖索引快速获取id，再JOIN查询完整数据
     *
     * 优化方案2：记录上次查询的最大id
     * SELECT * FROM user WHERE id > #{lastId} ORDER BY id LIMIT 10;
     */

    /**
     * 案例2：SELECT * 导致回表
     *
     * 问题SQL：
     * SELECT * FROM user WHERE name = '张三';
     *
     * 优化：
     * SELECT id, name, age FROM user WHERE name = '张三';
     * 添加 KEY idx_name_age(name, age)，实现覆盖索引
     */

    /**
     * 案例3：ORDER BY 导致 filesort
     *
     * 问题SQL：
     * SELECT * FROM user WHERE department_id = 1 ORDER BY create_time DESC;
     *
     * 问题：department_id索引无法用于排序，需要filesort
     *
     * 优化：
     * KEY idx_dept_time(department_id, create_time)
     * 满足最左前缀，同时用于条件过滤和排序
     */
}
