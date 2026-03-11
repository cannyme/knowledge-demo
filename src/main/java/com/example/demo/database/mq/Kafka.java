package com.example.demo.database.mq;

/**
 * Kafka核心概念与使用
 *
 * 【Kafka架构】
 *
 *     ┌─────────────┐                    ┌─────────────┐
 *     │  Producer   │                    │  Consumer   │
 *     │  生产者      │                    │  消费者      │
 *     └──────┬──────┘                    └──────┬──────┘
 *            │                                  │
 *            ▼                                  ▼
 *     ┌─────────────────────────────────────────────────────┐
 *     │                    Kafka Cluster                    │
 *     │  ┌─────────────────────────────────────────────┐    │
 *     │  │                  Broker                      │    │
 *     │  │  ┌─────────┐ ┌─────────┐ ┌─────────┐       │    │
 *     │  │  │Topic A  │ │Topic B  │ │Topic C  │       │    │
 *     │  │  │P0 P1 P2 │ │P0 P1    │ │P0 P1 P2 │       │    │
 *     │  │  └─────────┘ └─────────┘ └─────────┘       │    │
 *     │  └─────────────────────────────────────────────┘    │
 *     │                       │                             │
 *     │                       ▼                             │
 *     │              ┌─────────────┐                        │
 *     │              │    ZooKeeper │                        │
 *     │              │  (集群协调)   │                        │
 *     │              └─────────────┘                        │
 *     └─────────────────────────────────────────────────────┘
 *
 * 【核心概念】
 * Topic：消息主题，逻辑分类
 * Partition：分区，并行处理单元
 * Replication：副本，数据冗余
 * Consumer Group：消费者组，实现负载均衡
 */
public class Kafka {

    // ==================== 核心概念详解 ====================

    /**
     * Topic与Partition
     *
     * Topic是逻辑概念，Partition是物理概念
     *
     * Topic: orders
     * ├── Partition 0: [消息0, 消息1, 消息2, ...]  Offset: 0,1,2...
     * ├── Partition 1: [消息0, 消息1, 消息2, ...]
     * └── Partition 2: [消息0, 消息1, 消息2, ...]
     *
     * 分区作用：
     * 1. 提高并行度（多分区可并行消费）
     * 2. 提高吞吐量
     * 3. 分区内的消息有序
     *
     * 分区策略：
     * 1. 指定分区：直接发送到指定分区
     * 2. Key哈希：key.hash % 分区数
     * 3. 轮询：Round Robin
     * 4. 自定义：实现Partitioner接口
     */

    /**
     * 副本机制
     *
     * ISR（In-Sync Replicas）：同步副本集合
     *
     * Topic: orders (3分区，3副本)
     *
     * Partition 0:
     * ├── Leader    (Broker 1) ← 读写入口
     * ├── Follower  (Broker 2) ← 同步Leader数据
     * └── Follower  (Broker 3) ← 同步Leader数据
     *
     * ISR = [Leader, Follower1, Follower2]
     *
     * 生产者发送消息：
     * acks=0：不等待确认
     * acks=1：等待Leader确认（默认）
     * acks=all：等待所有ISR确认（最可靠）
     */

    /**
     * 消费者组
     *
     * 同一消费者组内的消费者分担消费
     * 不同消费者组独立消费
     *
     * Topic: orders (3分区)
     *
     * 消费者组A：
     * ├── Consumer A-1 → Partition 0
     * ├── Consumer A-2 → Partition 1
     * └── Consumer A-3 → Partition 2
     *
     * 消费者组B：
     * └── Consumer B-1 → Partition 0,1,2 (独占所有分区)
     *
     * 规则：
     * 1. 一个分区只能被同组一个消费者消费
     * 2. 消费者数 > 分区数时，有消费者空闲
     * 3. 消费者故障时触发Rebalance
     */

    // ==================== 生产者使用 ====================

    /**
     * 生产者配置
     */
    /*
    @Configuration
    public class KafkaProducerConfig {

        @Bean
        public ProducerFactory<String, String> producerFactory() {
            Map<String, Object> config = new HashMap<>();
            config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            config.put(ProducerConfig.ACKS_CONFIG, "all");
            config.put(ProducerConfig.RETRIES_CONFIG, 3);
            config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
            config.put(ProducerConfig.LINGER_MS_CONFIG, 5);
            config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
            return new DefaultKafkaProducerFactory<>(config);
        }

        @Bean
        public KafkaTemplate<String, String> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }
    }
    */

    /**
     * 发送消息
     */
    /*
    @Service
    public class OrderProducer {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        // 同步发送
        public void sendSync(String topic, String key, String message) {
            try {
                SendResult<String, String> result = kafkaTemplate.send(topic, key, message).get();
                RecordMetadata metadata = result.getRecordMetadata();
                System.out.println("发送成功: " + metadata.partition() + "-" + metadata.offset());
            } catch (Exception e) {
                System.err.println("发送失败: " + e.getMessage());
            }
        }

        // 异步发送
        public void sendAsync(String topic, String message) {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("发送成功");
                } else {
                    System.err.println("发送失败: " + ex.getMessage());
                }
            });
        }

        // 发送到指定分区
        public void sendToPartition(String topic, int partition, String key, String message) {
            kafkaTemplate.send(topic, partition, key, message);
        }
    }
    */

    // ==================== 消费者使用 ====================

    /**
     * 消费者配置
     */
    /*
    @Configuration
    @EnableKafka
    public class KafkaConsumerConfig {

        @Bean
        public ConsumerFactory<String, String> consumerFactory() {
            Map<String, Object> config = new HashMap<>();
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            config.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
            config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            return new DefaultKafkaConsumerFactory<>(config);
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
            ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory());
            factory.setConcurrency(3);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
            return factory;
        }
    }
    */

    /**
     * 消费消息
     */
    /*
    @Service
    public class OrderConsumer {

        // 简单消费
        @KafkaListener(topics = "orders", groupId = "order-group")
        public void consume(String message) {
            System.out.println("收到消息: " + message);
        }

        // 带分区信息
        @KafkaListener(topics = "orders", groupId = "order-group")
        public void consumeWithMetadata(ConsumerRecord<String, String> record) {
            System.out.println("收到消息: " + record.value());
            System.out.println("分区: " + record.partition());
            System.out.println("偏移: " + record.offset());
        }

        // 手动提交offset
        @KafkaListener(topics = "orders", groupId = "order-group")
        public void consumeWithAck(ConsumerRecord<String, String> record, Acknowledgment ack) {
            try {
                // 处理业务
                processMessage(record.value());
                // 手动提交
                ack.acknowledge();
            } catch (Exception e) {
                // 不提交，消息会重新投递
                log.error("处理失败", e);
            }
        }

        // 批量消费
        @KafkaListener(topics = "orders", groupId = "order-group",
                       containerFactory = "batchFactory")
        public void consumeBatch(List<ConsumerRecord<String, String>> records) {
            for (ConsumerRecord<String, String> record : records) {
                processMessage(record.value());
            }
        }
    }
    */

    // ==================== Offset管理 ====================

    /**
     * Offset提交策略：
     *
     * 1. 自动提交（不推荐）
     *    enable.auto.commit=true
     *    auto.commit.interval.ms=5000
     *    问题：可能丢失消息
     *
     * 2. 手动同步提交
     *    ack.acknowledge()
     *    问题：阻塞，影响性能
     *
     * 3. 手动异步提交
     *    ack.acknowledge() // Spring封装的异步
     *
     * 4. 指定Offset消费
     */
    /*
    @KafkaListener(topics = "orders", groupId = "order-group")
    public void consumeWithSeek(ConsumerRecord<String, String> record,
                                @Header(KafkaHeaders.OFFSET) long offset) {
        // 可以根据offset做特殊处理
    }
    */

    // ==================== Rebalance问题 ====================

    /**
     * Rebalance触发条件：
     * 1. 消费者加入/离开消费者组
     * 2. 消费者超时
     * 3. Topic分区数变化
     *
     * Rebalance影响：
     * - 消费暂停
     * - Offset可能丢失
     * - 重复消费
     *
     * 避免Rebalance：
     * 1. 合理设置session.timeout.ms
     * 2. 合理设置heartbeat.interval.ms
     * 3. 使用静态成员（group.instance.id）
     */

    // ==================== Kafka vs RocketMQ ====================

    /**
     * ┌─────────────────┬─────────────────────┬─────────────────────┐
     * │ 特性             │ Kafka               │ RocketMQ            │
     * ├─────────────────┼─────────────────────┼─────────────────────┤
     * │ 定位             │ 日志/大数据         │ 业务消息            │
     * │ 吞吐量           │ 100万/秒            │ 10万/秒             │
     * │ 延迟             │ 毫秒级              │ 毫秒级              │
     * │ 消息可靠性       │ 可能丢失            │ 高                  │
     * │ 事务消息         │ 支持（有限）        │ 支持                │
     * │ 延迟消息         │ 不支持              │ 支持                │
     * │ 顺序消息         │ 分区内有序          │ 队列内有序          │
     * │ 消息过滤         │ 不支持              │ 支持                │
     * │ 消息回溯         │ 支持                │ 支持                │
     * │ 消息堆积能力     │ 极强                │ 较强                │
     * └─────────────────┴─────────────────────┴─────────────────────┘
     *
     * 选型建议：
     * - Kafka：日志收集、大数据流处理、高吞吐场景
     * - RocketMQ：业务系统、事务消息、延迟消息
     */

    // ==================== 最佳实践 ====================

    /**
     * 生产者最佳实践：
     * 1. 合理设置acks（可靠性与性能平衡）
     * 2. 启用重试
     * 3. 使用批量发送提高吞吐
     * 4. 异步发送提高性能
     *
     * 消费者最佳实践：
     * 1. 关闭自动提交
     * 2. 业务处理后再提交offset
     * 3. 实现幂等性
     * 4. 合理设置消费线程数
     *
     * 集群最佳实践：
     * 1. 分区数 >= 消费者数
     * 2. 副本数 >= 3
     * 3. 监控消费延迟
     */
}
