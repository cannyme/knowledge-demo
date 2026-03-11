package com.example.demo.database.mysql;

/**
 * 数据库事务隔离级别与锁机制
 *
 * 【事务ACID】
 * A - Atomicity（原子性）：事务不可分割
 * C - Consistency（一致性）：事务前后数据完整
 * I - Isolation（隔离性）：并发事务互不干扰
 * D - Durability（持久性）：事务提交后永久保存
 *
 * 【隔离级别】
 * READ UNCOMMITTED > READ COMMITTED > REPEATABLE READ > SERIALIZABLE
 * 并发性能逐渐降低，隔离性逐渐增强
 */
public class TransactionIsolation {

    // ==================== 四种隔离级别 ====================

    /**
     * ┌─────────────────────┬───────┬───────┬───────┬─────────────────────┐
     * │ 隔离级别              │ 脏读   │ 不可重复读│ 幻读   │ 数据库默认           │
     * ├─────────────────────┼───────┼───────┼───────┼─────────────────────┤
     * │ READ UNCOMMITTED    │ 可能   │ 可能   │ 可能   │ -                   │
     * │ READ COMMITTED      │ 不会   │ 可能   │ 可能   │ Oracle, SQL Server  │
     * │ REPEATABLE READ     │ 不会   │ 不会   │ 可能*  │ MySQL（MVCC解决幻读）│
     * │ SERIALIZABLE        │ 不会   │ 不会   │ 不会   │ -                   │
     * └─────────────────────┴───────┴───────┴───────┴─────────────────────┘
     */

    /**
     * 1. 读未提交（READ UNCOMMITTED）
     *
     * 脏读示例：
     *
     * 时刻    事务A                        事务B
     * ─────────────────────────────────────────────────────
     * T1     BEGIN
     * T2     UPDATE user SET name='李四'
     *        (未提交)
     * T3                                  BEGIN
     * T4                                  SELECT name FROM user
     *                                      → 读到'李四'（脏读）
     * T5     ROLLBACK
     *        (数据回滚为'张三')
     *
     * 问题：事务B读到了未提交的数据，这些数据可能被回滚
     */

    /**
     * 2. 读已提交（READ COMMITTED）
     *
     * 不可重复读示例：
     *
     * 时刻    事务A                        事务B
     * ─────────────────────────────────────────────────────
     * T1                                  BEGIN
     * T2                                  SELECT name FROM user
     *                                      → 读到'张三'
     * T3     BEGIN
     * T4     UPDATE user SET name='李四'
     * T5     COMMIT
     * T6                                  SELECT name FROM user
     *                                      → 读到'李四'（不可重复读）
     *
     * 问题：同一事务内两次读取结果不同
     */

    /**
     * 3. 可重复读（REPEATABLE READ）
     *
     * MySQL默认隔离级别，通过MVCC实现
     *
     * 幻读示例（当前读场景）：
     *
     * 时刻    事务A                        事务B
     * ─────────────────────────────────────────────────────
     * T1     BEGIN
     * T2     SELECT * FROM user WHERE id>5
     *        → 2条记录
     * T3                                  BEGIN
     * T4                                  INSERT INTO user VALUES(6,...)
     * T5                                  COMMIT
     * T6     SELECT * FROM user WHERE id>5
     *        → 2条记录（MVCC保证一致性）
     * T7     UPDATE user SET name='x' WHERE id=6
     *        → 更新成功！（幻读）
     * T8     SELECT * FROM user WHERE id>5
     *        → 3条记录
     *
     * 解决：使用SELECT ... FOR UPDATE（加Next-Key Lock）
     */

    /**
     * 4. 串行化（SERIALIZABLE）
     *
     * 最高隔离级别，完全串行执行
     * 读操作加共享锁，写操作加排他锁
     * 性能最差，基本不用
     */

    // ==================== MySQL锁机制 ====================

    /**
     * 锁的分类：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                        MySQL锁分类                           │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 按粒度分：                                                    │
     * │   - 全局锁：锁定整个数据库                                    │
     * │   - 表级锁：锁定整张表                                        │
     * │   - 行级锁：锁定单行数据                                      │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 按类型分：                                                    │
     * │   - 共享锁（S锁）：读锁，可共享                               │
     * │   - 排他锁（X锁）：写锁，独占                                 │
     * │   - 意向锁：表级锁，标识事务意图                              │
     * ├─────────────────────────────────────────────────────────────┤
     * │ InnoDB行锁：                                                  │
     * │   - Record Lock：记录锁                                      │
     * │   - Gap Lock：间隙锁                                         │
     * │   - Next-Key Lock：记录锁+间隙锁                              │
     * └─────────────────────────────────────────────────────────────┘
     */

    /**
     * 行锁详解
     */

    /**
     * Record Lock（记录锁）
     *
     * 锁定单条索引记录
     *
     * 示例：
     * SELECT * FROM user WHERE id = 1 FOR UPDATE;
     * → 锁定id=1这一行
     */

    /**
     * Gap Lock（间隙锁）
     *
     * 锁定索引记录之间的间隙，不包含记录本身
     * 目的：防止幻读
     *
     * 假设有记录 id: 1, 5, 10
     *
     * SELECT * FROM user WHERE id = 7 FOR UPDATE;
     * → Gap Lock锁定 (5, 10) 这个间隙
     * → 其他事务无法在这个间隙插入数据
     *
     * 间隙：(-∞, 1), (1, 5), (5, 10), (10, +∞)
     */

    /**
     * Next-Key Lock（临键锁）
     *
     * = Record Lock + Gap Lock
     * 锁定记录本身 + 前面的间隙
     *
     * SELECT * FROM user WHERE id = 5 FOR UPDATE;
     * → 锁定 (1, 5] 这个区间
     *
     * 这是RR隔离级别下防止幻读的关键
     *
     * 示例：
     * 表数据：id = 1, 5, 10
     *
     * 事务A：SELECT * FROM user WHERE id = 5 FOR UPDATE;
     * → 加Next-Key Lock：(1, 5]
     * → 加Gap Lock：(5, 10)
     *
     * 事务B：INSERT INTO user VALUES(3, ...);  → 阻塞
     * 事务B：INSERT INTO user VALUES(7, ...);  → 阻塞
     * 事务B：INSERT INTO user VALUES(11, ...); → 成功
     */

    /**
     * 不同语句加锁情况
     */
    /*
    -- 唯一索引等值查询，记录存在
    SELECT * FROM user WHERE id = 1 FOR UPDATE;
    → Record Lock on id=1

    -- 唯一索引等值查询，记录不存在
    SELECT * FROM user WHERE id = 3 FOR UPDATE;
    → Gap Lock on (1, 5)

    -- 唯一索引范围查询
    SELECT * FROM user WHERE id >= 5 FOR UPDATE;
    → Record Lock on id=5
    → Next-Key Lock on (5, 10]
    → Gap Lock on (10, +∞)

    -- 非唯一索引等值查询
    -- 假设有普通索引 idx_age，数据：age=20,20,30,30
    SELECT * FROM user WHERE age = 20 FOR UPDATE;
    → Next-Key Lock on (-∞, 20]
    → Gap Lock on (20, 30)

    -- 无索引
    SELECT * FROM user WHERE name = '张三' FOR UPDATE;
    → 锁定所有记录和间隙（相当于表锁）
    */

    // ==================== 死锁 ====================

    /**
     * 死锁产生条件：
     * 1. 互斥条件
     * 2. 请求与保持条件
     * 3. 不可剥夺条件
     * 4. 循环等待条件
     *
     * 死锁示例：
     *
     * 时刻    事务A                        事务B
     * ─────────────────────────────────────────────────────
     * T1     BEGIN
     * T2     UPDATE user SET ... WHERE id=1
     *        (持有id=1的锁)
     * T3                                  BEGIN
     * T4                                  UPDATE user SET ... WHERE id=2
     *                                     (持有id=2的锁)
     * T5     UPDATE user SET ... WHERE id=2
     *        (等待id=2的锁)
     * T6                                  UPDATE user SET ... WHERE id=1
     *                                     (等待id=1的锁)
     *        → 死锁！
     *
     * MySQL死锁检测：
     * - innodb_deadlock_detect = ON（默认）
     * - 检测到死锁后，回滚其中一个事务
     *
     * 避免死锁：
     * 1. 按固定顺序访问表和行
     * 2. 大事务拆小事务
     * 3. 降低隔离级别
     * 4. 合理设计索引
     */

    // ==================== 锁等待与超时 ====================

    /**
     * 锁等待超时配置：
     *
     * innodb_lock_wait_timeout = 50（默认50秒）
     *
     * 查看锁等待：
     *
     * -- 查看正在等待的锁
     * SELECT * FROM information_schema.INNODB_LOCK_WAITS;
     *
     * -- 查看当前锁
     * SELECT * FROM performance_schema.data_locks;
     *
     * -- 查看锁等待关系
     * SELECT * FROM performance_schema.data_lock_waits;
     */

    // ==================== 总结 ====================

    /**
     * 隔离级别选择建议：
     *
     * ┌─────────────────────┬─────────────────────────────────────────┐
     * │ 场景                  │ 建议隔离级别                             │
     * ├─────────────────────┼─────────────────────────────────────────┤
     * │ 一般业务              │ READ COMMITTED（Oracle默认）             │
     * │ 需要一致性读          │ REPEATABLE READ（MySQL默认）             │
     * │ 高安全性要求          │ SERIALIZABLE                            │
     * └─────────────────────┴─────────────────────────────────────────┘
     *
     * 锁优化建议：
     * 1. 合理设计索引，减少锁范围
     * 2. 避免长事务
     * 3. 尽量使用主键更新
     * 4. 避免在事务中执行耗时操作
     */
}
