package com.example.demo.architecture;

/**
 * 分布式链路追踪
 *
 * 【为什么需要链路追踪】
 * 微服务架构下，一个请求经过多个服务，需要追踪完整调用链
 *
 * 【核心概念】
 * 1. Trace：一次请求的完整调用链
 * 2. Span：一次调用（包含开始时间、持续时间、操作名）
 * 3. SpanContext：Span上下文（traceId、spanId、parentId）
 *
 * 【主流方案】
 * 1. SkyWalking：国产，无侵入，性能好
 * 2. Zipkin：Twitter开源，简单易用
 * 3. Jaeger：Uber开源，CNCF项目
 * 4. Spring Cloud Sleuth：Spring生态
 */
public class DistributedTracing {

    // ==================== 核心概念 ====================

    /**
     * 调用链示例：
     *
     * TraceId: abc123
     *
     * 时间线：
     * ┌─────────────────────────────────────────────────────────────┐
     * │ Service A         │ [Span 1]                              │
     * │                   │ traceId=abc123, spanId=1, parentId=null│
     * ├───────────────────┼───────────────────────────────────────┤
     * │ Service B         │    [Span 2]                           │
     * │                   │ traceId=abc123, spanId=2, parentId=1   │
     * ├───────────────────┼───────────────────────────────────────┤
     * │ Service C         │        [Span 3]                       │
     * │                   │ traceId=abc123, spanId=3, parentId=2   │
     * ├───────────────────┼───────────────────────────────────────┤
     * │ Service D         │        [Span 4]                       │
     * │                   │ traceId=abc123, spanId=4, parentId=2   │
     * └───────────────────┴───────────────────────────────────────┘
     *
     * Span数据结构：
     * {
     *   "traceId": "abc123",
     *   "spanId": "2",
     *   "parentId": "1",
     *   "operationName": "UserService.getUser",
     *   "startTime": 1640000000000,
     *   "duration": 100,
     *   "tags": {
     *     "http.method": "GET",
     *     "http.url": "/api/user/1",
     *     "http.status": 200
     *   },
     *   "logs": [
     *     {"timestamp": 1640000000050, "event": "DB Query"},
     *     {"timestamp": 1640000000080, "event": "Cache Hit"}
     *   ]
     * }
     */

    // ==================== TraceId传递 ====================

    /**
     * TraceId传递方式：
     *
     * 1. HTTP Header传递
     *
     *    Request Headers:
     *    X-Trace-Id: abc123
     *    X-Span-Id: 2
     *    X-Parent-Span-Id: 1
     *
     * 2. RPC调用传递
     *
     *    在RPC Context中传递
     *
     * 3. 消息队列传递
     *
     *    在消息Header中携带
     */

    /**
     * MDC（Mapped Diagnostic Context）
     *
     * 在日志中自动打印TraceId
     */
    static class MDCHelper {
        public static final String TRACE_ID = "traceId";
        public static final String SPAN_ID = "spanId";

        public static void setTraceId(String traceId) {
            org.slf4j.MDC.put(TRACE_ID, traceId);
        }

        public static String getTraceId() {
            return org.slf4j.MDC.get(TRACE_ID);
        }

        public static void clear() {
            org.slf4j.MDC.clear();
        }
    }

    /**
     * Logback配置
     *
     * <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
     *   <encoder>
     *     <pattern>%d{yyyy-MM-dd HH:mm:ss} [%X{traceId}] [%thread] %-5level %logger{36} - %msg%n</pattern>
     *   </encoder>
     * </appender>
     *
     * 输出示例：
     * 2024-01-01 10:00:00 [abc123] [http-nio-8080-exec-1] INFO  c.e.demo.UserService - 查询用户
     */

    // ==================== SkyWalking ====================

    /**
     * SkyWalking架构：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                      SkyWalking                             │
     * │                                                             │
     * │   ┌─────────┐   ┌─────────┐   ┌─────────┐                 │
     * │   │  Agent  │   │  Agent  │   │  Agent  │  ← 无侵入探针   │
     * │   │ ServiceA│   │ ServiceB│   │ ServiceC│                 │
     * │   └────┬────┘   └────┬────┘   └────┬────┘                 │
     * │        │              │              │                      │
     * │        └──────────────┼──────────────┘                      │
     * │                       ▼                                     │
     * │               ┌─────────────┐                              │
     * │               │   OAP Server │  ← 数据收集、分析            │
     * │               └──────┬──────┘                              │
     * │                      │                                      │
     * │               ┌──────▼──────┐                              │
     * │               │    Storage   │  ← 数据存储（ES/H2/MySQL）   │
     * │               └──────┬──────┘                              │
     * │                      │                                      │
     * │               ┌──────▼──────┐                              │
     * │               │      UI     │  ← 可视化界面                │
     * │               └─────────────┘                              │
     * └─────────────────────────────────────────────────────────────┘
     *
     * Agent使用：
     * java -javaagent:skywalking-agent.jar
     *      -Dskywalking.agent.service_name=my-service
     *      -Dskywalking.collector.backend_service=localhost:11800
     *      -jar my-app.jar
     */

    // ==================== Sleuth + Zipkin ====================

    /**
     * Spring Cloud Sleuth配置
     *
     * spring:
     *   sleuth:
     *     sampler:
     *       probability: 1.0  # 采样率100%
     *     web:
     *       skipPattern: /actuator.*,/health
     *     zipkin:
     *       base-url: http://localhost:9411
     *
     * 自动功能：
     * 1. 自动生成TraceId、SpanId
     * 2. 自动注入HTTP Header
     * 3. 自动集成RestTemplate、Feign、WebClient
     * 4. 自动集成MQ
     */

    /**
     * 自定义Span
     */
    /*
    @Service
    public class OrderService {

        @Autowired
        private Tracer tracer;

        public Order getOrder(Long orderId) {
            // 创建新Span
            Span span = tracer.nextSpan().name("db-query");
            try (Tracer.SpanInScope ws = tracer.withSpan(span.start())) {
                // 执行数据库查询
                return orderRepository.findById(orderId);
            } finally {
                span.end();
            }
        }

        // 使用注解
        @NewSpan("process-order")
        public void processOrder(@SpanTag("order.id") Long orderId) {
            // 自动创建Span，参数作为Tag
        }
    }
    */

    // ==================== 自定义TraceFilter ====================

    /**
     * 手动实现TraceId传递
     */
    /*
    @Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public class TraceFilter implements Filter {

        private static final String TRACE_ID_HEADER = "X-Trace-Id";

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                            FilterChain chain) throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // 从Header获取或生成TraceId
            String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }

            // 设置到MDC
            MDCHelper.setTraceId(traceId);

            // 设置响应Header
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);

            try {
                chain.doFilter(request, response);
            } finally {
                MDCHelper.clear();
            }
        }
    }
    */

    // ==================== Feign传递TraceId ====================

    /**
     * Feign请求拦截器
     */
    /*
    @Configuration
    public class FeignConfig {

        @Bean
        public RequestInterceptor traceIdInterceptor() {
            return template -> {
                String traceId = MDCHelper.getTraceId();
                if (traceId != null) {
                    template.header("X-Trace-Id", traceId);
                }
            };
        }
    }
    */

    // ==================== MQ传递TraceId ====================

    /**
     * RocketMQ消费者传递TraceId
     */
    /*
    @Service
    @RocketMQMessageListener(topic = "order-topic", consumerGroup = "order-group")
    public class OrderConsumer implements RocketMQListener<MessageExt> {

        @Override
        public void onMessage(MessageExt message) {
            // 从消息Header获取TraceId
            String traceId = message.getProperty("X-Trace-Id");
            if (traceId != null) {
                MDCHelper.setTraceId(traceId);
            }

            try {
                // 处理消息
                processMessage(message);
            } finally {
                MDCHelper.clear();
            }
        }
    }
    */

    // ==================== 日志聚合 ====================

    /**
     * ELK（Elasticsearch + Logstash + Kibana）
     *
     * ┌─────────┐    ┌─────────┐    ┌─────────────┐    ┌─────────┐
     * │ 应用日志 │───→│Logstash │───→│Elasticsearch│───→│ Kibana  │
     * │ (JSON)  │    │ (收集)  │    │  (存储)     │    │ (可视化) │
     * └─────────┘    └─────────┘    └─────────────┘    └─────────┘
     *
     * Logback JSON格式配置：
     *
     * <appender name="JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
     *   <file>logs/application.json</file>
     *   <encoder class="net.logstash.logback.encoder.LogstashEncoder">
     *     <includeMdcKeyName>traceId</includeMdcKeyName>
     *     <includeMdcKeyName>spanId</includeMdcKeyName>
     *   </encoder>
     * </appender>
     *
     * 日志格式：
     * {
     *   "@timestamp": "2024-01-01T10:00:00.000+08:00",
     *   "level": "INFO",
     *   "logger": "com.example.demo.UserService",
     *   "message": "查询用户",
     *   "traceId": "abc123",
     *   "spanId": "1"
     * }
     */

    // ==================== 最佳实践 ====================

    /**
     * 1. 采样策略
     *    - 开发环境：100%采样
     *    - 生产环境：根据流量动态采样
     *    - 错误请求：100%采样
     *
     * 2. 性能优化
     *    - 异步上报
     *    - 批量发送
     *    - 合理设置Span数量
     *
     * 3. Tag规范
     *    - http.method, http.url, http.status
     *    - db.type, db.instance, db.statement
     *    - mq.topic, mq.partition
     *
     * 4. 告警配置
     *    - 慢调用告警
     *    - 错误率告警
     *    - 服务不可用告警
     */
}
