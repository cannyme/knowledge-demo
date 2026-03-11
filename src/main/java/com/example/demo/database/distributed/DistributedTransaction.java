package com.example.demo.database.distributed;

import java.util.*;

/**
 * 分布式事务解决方案
 *
 * 【CAP理论】
 * C - Consistency（一致性）
 * A - Availability（可用性）
 * P - Partition Tolerance（分区容错性）
 * 分布式系统只能满足其二
 *
 * 【BASE理论】
 * BA - Basically Available（基本可用）
 * S - Soft State（软状态）
 * E - Eventually Consistent（最终一致性）
 *
 * 【常见方案】
 * 1. 2PC/3PC（强一致性）
 * 2. TCC（柔性事务）
 * 3. 本地消息表
 * 4. 事务消息
 * 5. Seata AT模式
 */
public class DistributedTransaction {

    // ==================== 2PC（两阶段提交）====================

    /**
     * 2PC流程：
     *
     * 阶段一：准备阶段（Prepare）
     *
     *     Coordinator               Participant A          Participant B
     *         │                          │                      │
     *         │─────── Prepare ─────────→│                      │
     *         │                          │                      │
     *         │─────── Prepare ─────────────────────────────────→│
     *         │                          │                      │
     *         │←─────── Vote Yes ────────│                      │
     *         │                          │                      │
     *         │←─────── Vote Yes ───────────────────────────────│
     *         │                          │                      │
     *
     * 阶段二：提交阶段（Commit）
     *
     *         │─────── Commit ──────────→│                      │
     *         │                          │                      │
     *         │─────── Commit ──────────────────────────────────→│
     *         │                          │                      │
     *         │←─────── ACK ─────────────│                      │
     *         │                          │                      │
     *         │←─────── ACK ────────────────────────────────────│
     *
     * 问题：
     * 1. 同步阻塞：参与者在等待协调者指令期间锁定资源
     * 2. 单点故障：协调者宕机导致事务无法完成
     * 3. 数据不一致：Commit阶段部分参与者失败
     */

    // ==================== TCC（Try-Confirm-Cancel）====================

    /**
     * TCC是一种补偿型事务，将业务逻辑分为三个阶段：
     *
     * Try：尝试执行，预留资源
     * Confirm：确认执行，真正提交
     * Cancel：取消执行，释放资源
     *
     * 示例：转账（A向B转100元）
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 阶段      │ A账户                    │ B账户               │
     * ├───────────┼──────────────────────────┼─────────────────────┤
     * │ Try       │ 冻结100元（余额-100）      │ 预增100元（待入账）   │
     * │ Confirm   │ 确认扣除（删除冻结记录）    │ 确认入账            │
     * │ Cancel    │ 解冻（恢复余额）           │ 取消预增            │
     * └─────────────────────────────────────────────────────────────┘
     */
    static class TCCAccountService {

        /**
         * Try阶段：预留资源
         */
        /*
        @Transactional
        public boolean tryTransfer(String fromAccount, String toAccount, BigDecimal amount) {
            // 1. 检查余额是否充足
            Account from = accountMapper.selectForUpdate(fromAccount);
            if (from.getBalance().compareTo(amount) < 0) {
                throw new BusinessException("余额不足");
            }

            // 2. 冻结金额
            accountMapper.freeze(fromAccount, amount);
            // UPDATE account SET balance = balance - ?, frozen = frozen + ?
            // WHERE account_no = ?

            // 3. 记录事务日志
            transactionLogMapper.insert(new TransactionLog(
                transactionId, fromAccount, toAccount, amount, "TRY"
            ));

            return true;
        }
        */

        /**
         * Confirm阶段：确认提交
         */
        /*
        @Transactional
        public boolean confirmTransfer(String transactionId) {
            TransactionLog log = transactionLogMapper.selectById(transactionId);

            // 幂等性检查
            if ("CONFIRMED".equals(log.getStatus())) {
                return true;
            }

            // 1. 扣减冻结金额
            accountMapper.deductFrozen(log.getFromAccount(), log.getAmount());
            // UPDATE account SET frozen = frozen - ? WHERE account_no = ?

            // 2. 增加目标账户余额
            accountMapper.addBalance(log.getToAccount(), log.getAmount());
            // UPDATE account SET balance = balance + ? WHERE account_no = ?

            // 3. 更新事务状态
            transactionLogMapper.updateStatus(transactionId, "CONFIRMED");

            return true;
        }
        */

        /**
         * Cancel阶段：回滚
         */
        /*
        @Transactional
        public boolean cancelTransfer(String transactionId) {
            TransactionLog log = transactionLogMapper.selectById(transactionId);

            // 幂等性检查
            if ("CANCELLED".equals(log.getStatus())) {
                return true;
            }

            // 1. 解冻金额
            accountMapper.unfreeze(log.getFromAccount(), log.getAmount());
            // UPDATE account SET balance = balance + ?, frozen = frozen - ?
            // WHERE account_no = ?

            // 2. 更新事务状态
            transactionLogMapper.updateStatus(transactionId, "CANCELLED");

            return true;
        }
        */

        /**
         * TCC框架调用
         */
        /*
        @TccTransaction
        public void transfer(String from, String to, BigDecimal amount) {
            // 业务代码
            // 框架会自动调用 confirm 或 cancel
        }
        */
    }

    /**
     * TCC注意事项：
     *
     * 1. 幂等性：Confirm和Cancel可能被重复调用
     * 2. 空回滚：Try未执行，先收到Cancel请求
     * 3. 悬挂：Cancel先于Try执行
     * 4. 防悬挂：记录事务状态，Try执行前检查
     */

    // ==================== 本地消息表 ====================

    /**
     * 本地消息表：将消息和业务操作放在同一事务中
     *
     * 流程：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 服务A                                                        │
     * │ ┌─────────────┐    ┌─────────────┐                          │
     * │ │ 业务表       │    │ 消息表      │                          │
     * │ │ INSERT      │    │ INSERT      │  ← 同一事务              │
     * │ └─────────────┘    └─────────────┘                          │
     * └─────────────────────────────────────────────────────────────┘
     *                             │
     *                             │ 定时任务扫描未处理消息
     *                             ▼
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 服务B                                                        │
     * │ ┌─────────────┐                                             │
     * │ │ 处理消息     │  ← 幂等处理                                 │
     * │ └─────────────┘                                             │
     * └─────────────────────────────────────────────────────────────┘
     */
    static class LocalMessageTable {

        /**
         * 消息表结构
         */
        /*
        CREATE TABLE local_message (
            id BIGINT PRIMARY KEY AUTO_INCREMENT,
            message_id VARCHAR(64) UNIQUE,     -- 消息唯一ID
            target_service VARCHAR(64),        -- 目标服务
            content TEXT,                      -- 消息内容
            status TINYINT DEFAULT 0,          -- 0待发送 1已发送 2已消费
            retry_count INT DEFAULT 0,         -- 重试次数
            create_time DATETIME,
            update_time DATETIME
        );
        */

        /**
         * 业务操作 + 保存消息
         */
        /*
        @Transactional
        public void doBusinessAndSaveMessage(Order order) {
            // 1. 执行业务操作
            orderMapper.insert(order);

            // 2. 保存消息（同一事务）
            LocalMessage message = new LocalMessage();
            message.setMessageId(UUID.randomUUID().toString());
            message.setTargetService("inventory-service");
            message.setContent(JSON.toJSONString(order));
            message.setStatus(0);
            localMessageMapper.insert(message);
        }
        */

        /**
         * 定时任务发送消息
         */
        /*
        @Scheduled(fixedDelay = 5000)
        public void sendMessage() {
            // 1. 查询待发送消息
            List<LocalMessage> messages = localMessageMapper.selectPending();

            for (LocalMessage message : messages) {
                try {
                    // 2. 发送消息
                    remoteService.process(message.getContent());

                    // 3. 更新状态为已发送
                    localMessageMapper.updateStatus(message.getId(), 1);
                } catch (Exception e) {
                    // 4. 发送失败，增加重试次数
                    localMessageMapper.incrementRetry(message.getId());

                    // 5. 超过最大重试次数，标记失败
                    if (message.getRetryCount() >= 5) {
                        localMessageMapper.updateStatus(message.getId(), 3);
                    }
                }
            }
        }
        */
    }

    // ==================== 事务消息 ====================

    /**
     * RocketMQ事务消息
     *
     * 流程：
     *
     * ┌─────────────┐         ┌─────────────┐         ┌─────────────┐
     * │   生产者    │         │   Broker    │         │   消费者    │
     * │             │         │             │         │             │
     * │ 1.发送半消息 │────────→│ 存储半消息  │         │             │
     * │             │         │ (不可消费)  │         │             │
     * │             │         │             │         │             │
     * │ 2.执行本地事务│         │             │         │             │
     * │             │         │             │         │             │
     * │ 3.提交/回滚 │────────→│ 提交→可消费  │         │             │
     * │             │         │ 回滚→删除   │         │             │
     * │             │         │             │         │             │
     * │             │         │             │────────→│ 4.消费消息  │
     * │             │         │             │         │             │
     * │             │←────────│ 5.回查状态  │         │             │
     * │             │         │ (超时未确认)│         │             │
     * └─────────────┘         └─────────────┘         └─────────────┘
     *
     * 详见 RocketMQ.java 中的事务消息示例
     */

    // ==================== Seata ====================

    /**
     * Seata：阿里巴巴开源的分布式事务框架
     *
     * 三种模式：
     * 1. AT模式：无侵入，自动补偿（推荐）
     * 2. TCC模式：高性能，需要编写补偿代码
     * 3. Saga模式：长事务
     *
     * AT模式原理：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 一阶段：                                                     │
     * │ 1. 解析SQL，记录数据前后镜像                                  │
     * │ 2. 执行业务SQL                                               │
     * │ 3. 记录undo_log                                              │
     * │ 4. 向TC注册分支事务                                           │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 二阶段（提交）：                                              │
     * │ 1. 异步删除undo_log                                          │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 二阶段（回滚）：                                              │
     * │ 1. 根据undo_log反向生成SQL                                    │
     * │ 2. 执行回滚SQL                                               │
     * │ 3. 删除undo_log                                              │
     * └─────────────────────────────────────────────────────────────┘
     */

    /**
     * Seata使用示例
     */
    /*
    # 配置
    seata:
      enabled: true
      application-id: order-service
      tx-service-group: my_tx_group
      service:
        vgroup-mapping:
          my_tx_group: default
      registry:
        type: nacos
        nacos:
          server-addr: localhost:8848

    # 数据库表（每个库都需要）
    CREATE TABLE undo_log (
        id BIGINT PRIMARY KEY AUTO_INCREMENT,
        branch_id BIGINT,
        xid VARCHAR(100),
        context VARCHAR(128),
        rollback_info LONGBLOB,
        log_status INT,
        log_created DATETIME,
        log_modified DATETIME,
        UNIQUE KEY ux_undo_log (xid, branch_id)
    );
    */

    /**
     * 使用@GlobalTransactional注解
     */
    /*
    @Service
    public class OrderService {

        @GlobalTransactional
        public void createOrder(Order order) {
            // 1. 创建订单
            orderMapper.insert(order);

            // 2. 扣减库存（远程调用）
            inventoryClient.deduct(order.getProductId(), order.getCount());

            // 3. 扣减余额（远程调用）
            accountClient.deduct(order.getUserId(), order.getAmount());

            // 任意一步失败，自动回滚
        }
    }
    */

    // ==================== 方案对比 ====================

    /**
     * ┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
     * │ 方案             │ 一致性           │ 性能             │ 适用场景         │
     * ├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
     * │ 2PC/3PC         │ 强一致           │ 差              │ 数据库XA        │
     * │ TCC             │ 最终一致         │ 高              │ 高并发场景      │
     * │ 本地消息表      │ 最终一致         │ 中              │ 简单场景        │
     * │ 事务消息        │ 最终一致         │ 高              │ 异步场景        │
     * │ Seata AT        │ 最终一致         │ 中              │ 一般业务（推荐） │
     * └─────────────────┴─────────────────┴─────────────────┴─────────────────┘
     *
     * 选择建议：
     * - 简单业务：本地消息表
     * - 高并发：TCC、事务消息
     * - 一般业务：Seata AT（最简单）
     */
}
