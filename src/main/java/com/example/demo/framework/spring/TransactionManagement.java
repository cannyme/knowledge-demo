package com.example.demo.framework.spring;

import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.*;

/**
 * Spring事务管理
 *
 * 【事务ACID特性】
 * A - Atomicity（原子性）：事务是不可分割的最小单位
 * C - Consistency（一致性）：事务前后数据完整性约束不变
 * I - Isolation（隔离性）：并发事务之间互不干扰
 * D - Durability（持久性）：事务提交后永久保存
 *
 * 【Spring事务两种方式】
 * 1. 编程式事务：TransactionTemplate
 * 2. 声明式事务：@Transactional（推荐）
 */
public class TransactionManagement {

    // ==================== 声明式事务 @Transactional ====================

    /**
     * @Transactional 注解属性
     */
    /*
    @Transactional(
        // 事务管理器名称（多数据源时指定）
        transactionManager = "transactionManager",

        // 传播行为（默认REQUIRED）
        propagation = Propagation.REQUIRED,

        // 隔离级别（默认使用数据库默认）
        isolation = Isolation.READ_COMMITTED,

        // 超时时间（秒）
        timeout = 30,

        // 是否只读（可优化）
        readOnly = false,

        // 回滚异常（默认RuntimeException）
        rollbackFor = Exception.class,

        // 不回滚异常
        noRollbackFor = BusinessException.class
    )
    */
    public void transactionalDemo() {
    }

    // ==================== 事务传播行为 ====================
    /**
     * Propagation 定义事务如何传播
     *
     * ┌─────────────────────┬─────────────────────────────────────────────┐
     * │ 传播行为              │ 说明                                         │
     * ├─────────────────────┼─────────────────────────────────────────────┤
     * │ REQUIRED（默认）      │ 有事务就加入，没有就新建                        │
     * │ SUPPORTS             │ 有事务就加入，没有就以非事务运行                  │
     * │ MANDATORY            │ 必须在事务中调用，否则抛异常                     │
     * │ REQUIRES_NEW         │ 总是新建事务，挂起当前事务                       │
     * │ NOT_SUPPORTED        │ 以非事务运行，挂起当前事务                       │
     * │ NEVER                │ 以非事务运行，有事务则抛异常                     │
     * │ NESTED               │ 有事务就嵌套执行，没有就新建                     │
     * └─────────────────────┴─────────────────────────────────────────────┘
     */

    // @Service
    static class OrderService {

        // @Transactional
        public void createOrder(Order order) {
            // 保存订单
            // 扣减库存
            // 扣减余额
        }

        /**
         * REQUIRED 示例
         *
         * 如果 createOrder 有事务，deductStock 加入该事务
         * 如果 createOrder 没有事务，deductStock 新建事务
         * 所有操作在同一个事务中，任何失败都会全部回滚
         */
        // @Transactional(propagation = Propagation.REQUIRED)
        public void deductStock(Long productId, int count) {
            // 扣减库存
        }

        /**
         * REQUIRES_NEW 示例
         *
         * 场景：记录操作日志，即使主事务失败也要记录
         *
         * 原理：
         * 1. 挂起当前事务
         * 2. 新建事务执行
         * 3. 完成后恢复原事务
         */
        // @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void saveLog(String operation) {
            // 保存日志，独立事务
        }

        /**
         * NESTED 示例
         *
         * 嵌套事务：内部事务是外部事务的子事务
         *
         * 与 REQUIRES_NEW 的区别：
         * - REQUIRES_NEW：两个独立事务
         * - NESTED：内部事务回滚不会影响外部，但外部回滚会影响内部
         *
         * 实现：基于数据库的 Savepoint
         */
        // @Transactional(propagation = Propagation.NESTED)
        public void processItem(Item item) {
            // 处理明细，可以独立回滚
        }
    }

    // 记录类（伪代码）
    static class Order {}
    static class Item {}

    // ==================== 事务隔离级别 ====================
    /**
     * Isolation 隔离级别
     *
     * ┌─────────────────────┬───────┬───────┬───────┬───────────────────┐
     * │ 隔离级别              │ 脏读   │ 不可重复读│ 幻读   │ 说明               │
     * ├─────────────────────┼───────┼───────┼───────┼───────────────────┤
     * │ READ_UNCOMMITTED    │ ✗     │ ✗     │ ✗     │ 可能读到未提交数据    │
     * │ READ_COMMITTED      │ ✓     │ ✗     │ ✗     │ Oracle默认          │
     * │ REPEATABLE_READ     │ ✓     │ ✓     │ ✗     │ MySQL默认（MVCC解决幻读）│
     * │ SERIALIZABLE        │ ✓     │ ✓     │ ✓     │ 完全串行，性能差      │
     * └─────────────────────┴───────┴───────┴───────┴───────────────────┘
     *
     * 问题说明：
     * - 脏读：读到其他事务未提交的数据
     * - 不可重复读：同一事务两次读取结果不同（被其他事务修改了）
     * - 幻读：同一事务两次读取记录数不同（被其他事务插入/删除了）
     */

    // ==================== 事务失效场景 ====================
    /**
     * 1. 方法不是 public
     *    @Transactional 只对 public 方法有效
     *
     * 2. 自调用问题
     *    同一个类中，A方法调用B方法，B方法的@Transactional失效
     *    原因：Spring AOP基于代理，自调用不走代理
     *
     *    解决方案：
     *    a. 注入自己：@Autowired private OrderService self;
     *    b. 使用 AopContext.currentProxy()
     *    c. 拆分到另一个类
     */
    // @Service
    static class SelfInvocationService {

        // @Transactional
        public void methodA() {
            // 自调用，事务失效！
            // this.methodB();

            // 解决方案1：注入自己
            // self.methodB();

            // 解决方案2：AopContext
            // ((SelfInvocationService) AopContext.currentProxy()).methodB();
        }

        // @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void methodB() {
            // 新事务
        }
    }

    /**
     * 3. 异常被捕获
     *
     * 默认只对 RuntimeException 回滚
     * 如果捕获异常后没有抛出，事务不会回滚
     */
    // @Transactional
    public void wrongCatch() {
        try {
            // 业务操作
        } catch (Exception e) {
            // 捕获后未抛出，事务不回滚！
            e.printStackTrace();
        }
    }

    // 正确做法
    // @Transactional(rollbackFor = Exception.class)
    public void correctCatch() {
        try {
            // 业务操作
        } catch (Exception e) {
            // 手动标记回滚
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    /**
     * 4. 异常类型不对
     *    默认只回滚 RuntimeException 和 Error
     *    checked 异常（如 IOException）不会回滚
     *
     *    解决：rollbackFor = Exception.class
     */

    /**
     * 5. 数据库引擎不支持事务
     *    MySQL 的 MyISAM 不支持事务，必须用 InnoDB
     */

    /**
     * 6. 事务传播行为设置错误
     *    如 NOT_SUPPORTED、NEVER 会导致不在事务中执行
     */

    // ==================== 编程式事务 ====================
    /**
     * 使用 TransactionTemplate
     */
    // @Service
    static class ProgrammaticTransactionService {

        // @Autowired
        // private TransactionTemplate transactionTemplate;

        public void doSomething() {
            /*
            transactionTemplate.execute(status -> {
                try {
                    // 业务操作
                } catch (Exception e) {
                    status.setRollbackOnly();
                }
                return null;
            });
            */

            // 带返回值
            /*
            String result = transactionTemplate.execute(status -> {
                // 业务操作
                return "success";
            });
            */
        }
    }

    // ==================== 长事务问题 ====================
    /**
     * 长事务的危害：
     * 1. 数据库连接占用时间长
     * 2. 锁资源占用时间长，影响并发
     * 3. 回滚代价大
     * 4. 主从延迟增加
     *
     * 优化方案：
     * 1. 缩小事务范围，非必要操作移到事务外
     * 2. 使用 REQUIRES_NEW 让独立操作单独事务
     * 3. 批量操作分批提交
     * 4. 异步处理非核心逻辑
     */
    // @Transactional
    public void createOrderOptimized(Order order) {
        // 事务内：核心业务
        // saveOrder();
        // deductStock();

        // 事务外：非核心操作（可以用事件驱动）
        // sendNotification();  // 发送通知
        // updateStatistics();  // 更新统计
    }

    /**
     * 使用 @TransactionalEventListener 实现事务后执行
     */
    /*
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterOrderCreated(OrderCreatedEvent event) {
        // 事务提交后执行
        sendNotification(event.getOrder());
    }
    */
}
