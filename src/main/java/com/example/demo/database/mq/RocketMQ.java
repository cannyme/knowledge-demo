package com.example.demo.database.mq;

/**
 * RocketMQ核心概念与使用
 *
 * 【RocketMQ架构】
 *
 *     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
 *     │  Producer   │     │  NameServer │     │  Consumer   │
 *     │  生产者      │     │  路由注册中心│     │  消费者      │
 *     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
 *            │                   │                   │
 *            │                   │                   │
 *            ▼                   ▼                   ▼
 *     ┌─────────────────────────────────────────────────────┐
 *     │                    Broker                           │
 *     │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
 *     │  │   Topic A   │  │   Topic B   │  │   Topic C   │  │
 *     │  │  Queue 0-3  │  │  Queue 0-3  │  │  Queue 0-3  │  │
 *     │  └─────────────┘  └─────────────┘  └─────────────┘  │
 *     │                    消息存储                          │
 *     └─────────────────────────────────────────────────────┘
 *
 * 【核心组件】
 * NameServer：路由注册中心，类似注册中心
 * Broker：消息服务器，负责存储和转发
 * Producer：消息生产者
 * Consumer：消息消费者
 * Topic：消息主题，消息第一级分类
 * Tag：消息标签，消息第二级分类
 */
public class RocketMQ {

    // ==================== 消息模型 ====================

    /**
     * 1. 普通消息
     *    最常用的消息类型，适合大部分场景
     *
     * 2. 顺序消息
     *    同一队列内消息按FIFO顺序消费
     *
     * 3. 延迟消息
     *    消息发送后延迟一定时间才能消费
     *
     * 4. 事务消息
     *    保证本地事务和消息发送的原子性
     *
     * 5. 批量消息
     *    多条消息合并发送，提高效率
     */

    // ==================== 生产者使用示例 ====================

    /**
     * 发送普通消息
     */
    /*
    @Service
    public class OrderProducer {

        @Autowired
        private RocketMQTemplate rocketMQTemplate;

        // 同步发送
        public void sendSync(Order order) {
            SendResult result = rocketMQTemplate.syncSend(
                "order-topic",
                MessageBuilder.withPayload(order).build()
            );
            System.out.println("发送结果：" + result.getSendStatus());
        }

        // 异步发送
        public void sendAsync(Order order) {
            rocketMQTemplate.asyncSend(
                "order-topic",
                MessageBuilder.withPayload(order).build(),
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult result) {
                        System.out.println("发送成功");
                    }

                    @Override
                    public void onException(Throwable e) {
                        System.err.println("发送失败：" + e.getMessage());
                    }
                }
            );
        }

        // 单向发送（不关心结果，性能最高）
        public void sendOneWay(Order order) {
            rocketMQTemplate.sendOneWay("order-topic",
                MessageBuilder.withPayload(order).build());
        }
    }
    */

    /**
     * 发送延迟消息
     *
     * 延迟级别：
     * 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     * 1  2  3   4   5  6  7  8  9  10 11 12 13  14  15  16 17 18
     */
    /*
    public void sendDelayMessage(Order order) {
        Message<Order> message = MessageBuilder.withPayload(order).build();

        // 延迟级别16，即30分钟后消费
        SendResult result = rocketMQTemplate.syncSend(
            "order-cancel-topic",
            message,
            3000,  // 超时时间
            16     // 延迟级别
        );
    }
    */

    /**
     * 发送顺序消息
     */
    /*
    public void sendOrderly(Order order) {
        // 根据订单ID选择队列，保证同一订单的消息顺序
        rocketMQTemplate.syncSendOrderly(
            "order-topic",
            MessageBuilder.withPayload(order).build(),
            order.getOrderId()  // 分区键，相同值的消息进入同一队列
        );
    }
    */

    /**
     * 发送事务消息
     */
    /*
    @Service
    public class TransactionProducer {

        @Autowired
        private RocketMQTemplate rocketMQTemplate;

        public void sendTransactionMessage(Order order) {
            // 发送半消息
            TransactionSendResult result = rocketMQTemplate.sendMessageInTransaction(
                "order-topic",
                MessageBuilder.withPayload(order).build(),
                order  // 本地事务参数
            );

            System.out.println("事务消息发送结果：" + result.getSendStatus());
        }
    }

    @RocketMQTransactionListener
    @Component
    public class OrderTransactionListener implements RocketMQLocalTransactionListener {

        @Autowired
        private OrderService orderService;

        // 执行本地事务
        @Override
        public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
            try {
                Order order = (Order) arg;
                orderService.createOrder(order);
                return RocketMQLocalTransactionState.COMMIT;
            } catch (Exception e) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        }

        // 回查本地事务状态
        @Override
        public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
            // 查询订单是否存在
            String orderId = (String) msg.getHeaders().get("orderId");
            Order order = orderService.getById(orderId);

            return order != null
                ? RocketMQLocalTransactionState.COMMIT
                : RocketMQLocalTransactionState.ROLLBACK;
        }
    }
    */

    // ==================== 消费者使用示例 ====================

    /**
     * 消费消息
     */
    /*
    @Service
    @RocketMQMessageListener(
        topic = "order-topic",
        consumerGroup = "order-consumer-group",
        messageModel = MessageModel.CLUSTERING  // 集群模式（默认）
        // messageModel = MessageModel.BROADCASTING  // 广播模式
    )
    public class OrderConsumer implements RocketMQListener<Order> {

        @Override
        public void onMessage(Order order) {
            System.out.println("收到订单消息：" + order.getOrderId());
            // 处理订单...
        }
    }
    */

    /**
     * 顺序消费
     */
    /*
    @RocketMQMessageListener(
        topic = "order-topic",
        consumerGroup = "order-consumer-group",
        consumeMode = ConsumeMode.ORDERLY  // 顺序消费
    )
    */

    /**
     * 消费者配置
     */
    /*
    @RocketMQMessageListener(
        topic = "order-topic",
        consumerGroup = "order-consumer-group",
        consumeMode = ConsumeMode.CONCURRENTLY,  // 并发消费
        messageModel = MessageModel.CLUSTERING,   // 集群模式
        consumeThreadMax = 20,                     // 最大消费线程数
        maxReconsumeTimes = 3,                     // 最大重试次数
        delayLevelWhenNextConsume = 3              // 重试延迟级别
    )
    */

    // ==================== 消费幂等 ====================

    /**
     * 消息重复消费原因：
     * 1. 生产者重复发送（网络重试）
     * 2. 消费者处理成功但ACK失败
     * 3. Rebalance导致重复消费
     *
     * 解决方案：
     */
    /*
    @Service
    public class IdempotentConsumer {

        @Autowired
        private RedisTemplate<String, String> redisTemplate;

        public void consume(String messageId, Consumer<Void> businessLogic) {
            // 使用Redis实现幂等
            String key = "mq:consumed:" + messageId;

            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", 1, TimeUnit.DAYS);

            if (Boolean.TRUE.equals(success)) {
                // 首次消费，执行业务逻辑
                businessLogic.accept(null);
            } else {
                // 重复消费，直接返回
                log.info("消息已消费，跳过：{}", messageId);
            }
        }
    }
    */

    // ==================== 消息可靠性 ====================

    /**
     * 消息丢失场景与解决方案：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │ 阶段        │ 可能丢失点          │ 解决方案                 │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 生产者发送  │ 网络异常            │ 使用同步发送+重试         │
     * │            │ Broker宕机          │ 发送到多个Broker          │
     * ├─────────────────────────────────────────────────────────────┤
     * │ Broker存储  │ 写入失败            │ 同步刷盘                 │
     * │            │ 主从同步失败        │ 同步复制                 │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 消费者消费  │ 处理异常            │ 重试机制                 │
     * │            │ ACK失败             │ 消费确认                 │
     * └─────────────────────────────────────────────────────────────┘
     *
     * 生产者配置：
     * rocketmq:
     *   producer:
     *     retry-times-when-send-failed: 3  # 发送失败重试次数
     *     retry-times-when-send-async-failed: 3
     *     sync: true  # 同步刷盘
     *
     * Broker配置：
     * flushDiskType = SYNC_FLUSH  # 同步刷盘
     * brokerRole = SYNC_MASTER    # 同步复制
     */

    // ==================== 顺序消息 ====================

    /**
     * 全局顺序 vs 分区顺序
     *
     * 全局顺序：所有消息严格按顺序消费（单队列，性能差）
     * 分区顺序：同一分区的消息按顺序消费（推荐）
     *
     * 实现原理：
     * - Producer根据分区键选择队列
     * - Consumer对每个队列单线程消费
     *
     * 场景示例：订单状态变更
     * 订单A的消息：创建 → 支付 → 发货 → 完成
     * 订单B的消息：创建 → 取消
     *
     * 要求：
     * - 订单A的消息必须按顺序处理
     * - 订单A和订单B可以并行处理
     */

    // ==================== 消息积压处理 ====================

    /**
     * 消息积压原因：
     * 1. 消费速度 < 生产速度
     * 2. 消费者处理耗时
     * 3. 消费者异常
     *
     * 解决方案：
     *
     * 1. 增加消费者数量（需要先增加队列数）
     * 2. 提高消费线程数
     * 3. 批量消费
     * 4. 异步处理（先入库，再异步处理）
     *
     * 监控指标：
     * - 消费延迟（Diff）
     * - 消费TPS
     * - 队列深度
     */

    // ==================== RocketMQ vs Kafka ====================

    /**
     * ┌─────────────────┬─────────────────────┬─────────────────────┐
     * │ 特性             │ RocketMQ            │ Kafka               │
     * ├─────────────────┼─────────────────────┼─────────────────────┤
     * │ 单机吞吐量       │ 10万级              │ 100万级             │
     * │ 消息延迟         │ 毫秒级              │ 毫秒级              │
     * │ 可用性           │ 主从架构            │ 分布式架构          │
     * │ 消息可靠性       │ 高（同步刷盘）      │ 可能丢失            │
     * │ 功能特性         │ 丰富（事务、延迟）  │ 简单                │
     * │ 顺序消息         │ 支持                │ 支持                │
     * │ 事务消息         │ 支持                │ 不支持              │
     * │ 延迟消息         │ 支持（固定级别）    │ 不支持              │
     * │ 消息过滤         │ 支持（Tag、SQL）    │ 不支持              │
     * │ 消息回溯         │ 支持（按时间）      │ 支持（按Offset）    │
     * └─────────────────┴─────────────────────┴─────────────────────┘
     *
     * 选型建议：
     * - RocketMQ：业务场景、事务消息、延迟消息
     * - Kafka：大数据场景、日志收集、流处理
     */
}
