package com.example.demo.database.mq;

/**
 * 消息队列核心问题与解决方案
 *
 * 【三大核心问题】
 * 1. 消息丢失：消息在传输过程中丢失
 * 2. 消息重复：同一条消息被消费多次
 * 3. 消息顺序：消息消费顺序与发送顺序不一致
 *
 * 【其他重要问题】
 * 4. 消息积压：消息堆积无法及时消费
 * 5. 消息过期：消息存储时间过长被清理
 */
public class MessageReliability {

    // ==================== 问题一：消息丢失 ====================

    /**
     * 消息丢失可能发生的三个阶段：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    消息传输路径                              │
     * │                                                             │
     * │   Producer ──→ Broker ──→ Consumer                          │
     * │       │          │           │                              │
     * │    发送阶段    存储阶段     消费阶段                          │
     * └─────────────────────────────────────────────────────────────┘
     */

    /**
     * 阶段1：发送阶段防丢失
     */
    static class ProducerSolution {

        /**
         * 方案1：同步发送 + 确认机制
         */
        /*
        public void sendWithConfirm(Message message) {
            // 同步发送，等待Broker确认
            SendResult result = producer.send(message);

            if (result.getSendStatus() != SendStatus.SEND_OK) {
                // 发送失败，重试或记录日志
                log.error("消息发送失败：{}", result);
                // 重试逻辑...
            }
        }
        */

        /**
         * 方案2：异步发送 + 回调
         */
        /*
        public void sendAsync(Message message) {
            producer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    log.info("发送成功");
                }

                @Override
                public void onException(Throwable e) {
                    log.error("发送失败，重试", e);
                    // 重试逻辑...
                }
            });
        }
        */

        /**
         * 方案3：事务消息
         */
        /*
        // 1. 发送半消息（Half Message）
        // 2. 执行本地事务
        // 3. 提交/回滚消息
        // 4. 如果长时间没有确认，Broker回查事务状态
        */
    }

    /**
     * 阶段2：存储阶段防丢失
     */
    static class BrokerSolution {

        /**
         * 刷盘策略：
         *
         * ┌──────────────────┬─────────────────────────────────────────┐
         * │ 策略              │ 说明                                     │
         * ├──────────────────┼─────────────────────────────────────────┤
         * │ 异步刷盘（ASYNC）  │ 性能高，可能丢失（默认）                   │
         * │ 同步刷盘（SYNC）   │ 性能低，不丢失                           │
         * └──────────────────┴─────────────────────────────────────────┘
         *
         * 配置：flushDiskType = SYNC_FLUSH
         */

        /**
         * 主从复制策略：
         *
         * ┌──────────────────┬─────────────────────────────────────────┐
         * │ 策略              │ 说明                                     │
         * ├──────────────────┼─────────────────────────────────────────┤
         * │ 异步复制（ASYNC）  │ 性能高，主从切换可能丢失                   │
         * │ 同步复制（SYNC）   │ 性能低，主从切换不丢失                     │
         * └──────────────────┴─────────────────────────────────────────┘
         *
         * 配置：brokerRole = SYNC_MASTER
         */
    }

    /**
     * 阶段3：消费阶段防丢失
     */
    static class ConsumerSolution {

        /**
         * 关键：先执行业务逻辑，再发送ACK
         */
        /*
        // ❌ 错误做法：先ACK再处理
        @RocketMQMessageListener(topic = "order-topic", consumerGroup = "order-group")
        public class WrongConsumer implements RocketMQListener<Order> {
            @Override
            public void onMessage(Order order) {
                // 手动ACK后再处理，如果处理失败消息就丢了
            }
        }

        // ✅ 正确做法：处理成功后再ACK
        @RocketMQMessageListener(topic = "order-topic", consumerGroup = "order-group")
        public class RightConsumer implements RocketMQListener<Order> {
            @Override
            public void onMessage(Order order) {
                try {
                    // 处理业务逻辑
                    orderService.process(order);
                    // 方法正常返回，自动ACK
                } catch (Exception e) {
                    // 抛出异常，不ACK，消息会重新投递
                    throw new RuntimeException("处理失败，等待重试", e);
                }
            }
        }
        */
    }

    // ==================== 问题二：消息重复 ====================

    /**
     * 消息重复原因：
     *
     * 1. 生产者重复发送
     *    - 网络超时，生产者重试
     *    - Broker已接收但ACK丢失
     *
     * 2. 消费者重复消费
     *    - 消费成功但ACK失败
     *    - Rebalance导致offset未提交
     *
     * 解决方案：幂等性设计
     */
    static class IdempotentSolution {

        /**
         * 方案1：唯一ID + 去重表
         */
        /*
        @Service
        public class IdempotentConsumer {

            @Autowired
            private JdbcTemplate jdbcTemplate;

            public void consume(Message message) {
                String messageId = message.getKeys();

                // 使用数据库唯一索引实现幂等
                try {
                    jdbcTemplate.update(
                        "INSERT INTO msg_log (msg_id, status) VALUES (?, 1)",
                        messageId
                    );
                } catch (DuplicateKeyException e) {
                    // 消息已处理，直接返回
                    log.info("消息已处理：{}", messageId);
                    return;
                }

                // 执行业务逻辑
                processBusiness(message);
            }
        }
        */

        /**
         * 方案2：Redis SetNX
         */
        /*
        public boolean isProcessed(String messageId) {
            String key = "msg:processed:" + messageId;
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", 24, TimeUnit.HOURS);
            return !Boolean.TRUE.equals(success);
        }
        */

        /**
         * 方案3：业务状态机
         */
        /*
        // 订单状态流转：待支付 → 已支付 → 已发货
        // 利用状态流转的单向性实现幂等

        public void processOrder(Order order) {
            Order existing = orderMapper.selectById(order.getId());

            // 只有待支付状态才能改为已支付
            if (existing.getStatus() == OrderStatus.WAIT_PAY) {
                orderMapper.updateStatus(order.getId(), OrderStatus.PAID);
            }
            // 如果已经是已支付状态，直接返回（幂等）
        }
        */
    }

    // ==================== 问题三：消息顺序 ====================

    /**
     * 顺序消息场景：
     * - 订单状态变更：创建 → 支付 → 发货
     * - 数据同步：增 → 改 → 删
     *
     * 全局顺序 vs 分区顺序
     */
    static class OrderSolution {

        /**
         * 分区顺序实现：
         *
         * 1. 发送端：根据业务ID选择队列
         */
        /*
        // RocketMQ
        producer.send(message, new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                Long orderId = (Long) arg;
                int index = (int) (orderId % mqs.size());
                return mqs.get(index);
            }
        }, order.getId());

        // Kafka
        ProducerRecord<String, String> record = new ProducerRecord<>(
            "topic",
            order.getId().toString(),  // key，相同key进入同一分区
            message
        );
        */

        /**
         * 2. 消费端：单线程消费同一队列
         */
        /*
        @RocketMQMessageListener(
            topic = "order-topic",
            consumerGroup = "order-group",
            consumeMode = ConsumeMode.ORDERLY  // 顺序消费
        )
        public class OrderConsumer implements RocketMQListener<Order> {
            @Override
            public void onMessage(Order order) {
                // 单线程处理，保证顺序
                processOrder(order);
            }
        }
        */
    }

    // ==================== 问题四：消息积压 ====================

    /**
     * 消息积压原因：
     * - 消费速度 < 生产速度
     * - 消费者故障
     * - 消费逻辑慢
     *
     * 解决方案：
     */
    static class BacklogSolution {

        /**
         * 方案1：增加消费者（需要增加队列数）
         *
         * 消费者数量 ≤ 队列数量
         * 如果队列数不足，需要先扩容队列
         */

        /**
         * 方案2：批量消费
         */
        /*
        @RocketMQMessageListener(
            topic = "order-topic",
            consumerGroup = "order-group",
            consumeMode = ConsumeMode.CONCURRENTLY,
            consumeThreadMax = 64,
            maxReconsumeTimes = 3
        )
        public class BatchConsumer implements RocketMQListener<List<Order>> {
            @Override
            public void onMessage(List<Order> orders) {
                // 批量处理
                orderService.batchProcess(orders);
            }
        }
        */

        /**
         * 方案3：临时扩容方案
         *
         * 1. 新建临时Topic，队列数扩容
         * 2. 编写临时消费者，只负责转发
         * 3. 临时消费者将消息转发到新Topic
         * 4. 部署大量消费者消费新Topic
         * 5. 积压解决后恢复原架构
         */
    }

    // ==================== 问题五：消息过期 ====================

    /**
     * 消息过期机制：
     * - RocketMQ：默认72小时
     * - Kafka：默认7天
     * - RabbitMQ：可设置TTL
     *
     * 过期消息处理：
     * 1. 死信队列（DLQ）
     * 2. 补偿机制
     */

    // ==================== 最佳实践总结 ====================

    /**
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    消息可靠性最佳实践                         │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 1. 发送端                                                   │
     * │    - 使用同步发送或带回调的异步发送                           │
     * │    - 实现重试机制                                           │
     * │    - 关键业务使用事务消息                                    │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 2. 存储端                                                   │
     * │    - 根据业务需求选择刷盘策略                                │
     * │    - 配置主从复制保证高可用                                  │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 3. 消费端                                                   │
     * │    - 先处理业务，再ACK                                       │
     * │    - 实现幂等性                                             │
     * │    - 设置合理的重试次数和间隔                                │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 4. 监控告警                                                 │
     * │    - 监控消费延迟                                           │
     * │    - 监控积压情况                                           │
     * │    - 监控消费失败率                                         │
     * └─────────────────────────────────────────────────────────────┘
     */
}
