package com.example.demo.framework.springcloud;

/**
 * Sentinel —— 流量控制与熔断降级
 *
 * 【什么是 Sentinel】
 * Sentinel 是阿里巴巴开源的流量控制组件，提供：
 * 1. 流量控制（限流）
 * 2. 熔断降级
 * 3. 系统负载保护
 * 4. 热点参数限流
 *
 * 【核心概念】
 * - 资源（Resource）：可以是方法、代码块、HTTP接口等
 * - 规则（Rule）：对资源的限流/熔断策略
 * - 入口（Entry）：资源访问的入口点
 *
 * 【Sentinel vs Hystrix】
 * ┌─────────────────┬─────────────────┬─────────────────┐
 * │ 对比项           │ Sentinel        │ Hystrix         │
 * ├─────────────────┼─────────────────┼─────────────────┤
 * │ 限流             │ ✅ 支持          │ ❌ 不支持        │
 * │ 熔断             │ ✅ 支持          │ ✅ 支持          │
 * │ 实时监控         │ ✅ 控制台        │ ❌ 需集成        │
 * │ 动态规则         │ ✅ 支持          │ 较弱            │
 * │ 热点限流         │ ✅ 支持          │ ❌ 不支持        │
 * │ 系统保护         │ ✅ 支持          │ ❌ 不支持        │
 * │ 维护状态         │ 活跃            │ 停止维护         │
 * └─────────────────┴─────────────────┴─────────────────┘
 */
public class SentinelFlowControl {

    // ==================== 核心架构 ====================

    /**
     * 【Sentinel 架构】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                       Sentinel 架构                                 │
     * │                                                                     │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                     核心库（Core）                           │  │
     * │   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │  │
     * │   │  │  Flow Slot  │  │ Degrade Slot│  │ System Slot │         │  │
     * │   │  │  (流量控制)  │  │  (熔断降级)  │  │ (系统保护)   │         │  │
     * │   │  └─────────────┘  └─────────────┘  └─────────────┘         │  │
     * │   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │  │
     * │   │  │Authority Slot│ │  Param Slot │  │Cluster Slot │         │  │
     * │   │  │ (授权控制)   │  │ (热点限流)   │  │ (集群限流)   │         │  │
     * │   │  └─────────────┘  └─────────────┘  └─────────────┘         │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │                              │                                      │
     * │                              ▼                                      │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   Slot Chain（责任链）                       │  │
     * │   │                                                              │  │
     * │   │   NodeSelectorSlot → ClusterBuilderSlot → LogSlot          │  │
     * │   │        ↓                                                     │  │
     * │   │   StatisticSlot → FlowSlot → DegradeSlot → SystemSlot      │  │
     * │   │        ↓                                                     │  │
     * │   │   AuthoritySlot → ParamFlowSlot → ClusterFlowSlot          │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │                              │                                      │
     * │                              ▼                                      │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   Dashboard（控制台）                        │  │
     * │   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │  │
     * │   │  │  规则配置   │  │  实时监控   │  │  集群管理   │         │  │
     * │   │  └─────────────┘  └─────────────┘  └─────────────┘         │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * └─────────────────────────────────────────────────────────────────────┘
     *
     * 【Slot 职责链】
     *
     * 请求进入时，依次经过各个 Slot：
     *
     * 1. NodeSelectorSlot：构建资源调用树
     * 2. ClusterBuilderSlot：构建集群节点
     * 3. LogSlot：记录日志
     * 4. StatisticSlot：实时统计数据（QPS、RT、线程数）
     * 5. FlowSlot：流量控制检查
     * 6. DegradeSlot：熔断降级检查
     * 7. SystemSlot：系统负载保护检查
     * 8. AuthoritySlot：黑白名单控制
     * 9. ParamFlowSlot：热点参数限流
     */

    // ==================== 流量控制（限流）====================

    /**
     * 【限流算法】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │ 1. 滑动窗口算法（Sentinel 默认）                                     │
     * │                                                                     │
     * │    将时间窗口划分为多个小格子，统计每个格子的请求数                    │
     * │                                                                     │
     * │    时间窗口：1秒，格子数：10个                                        │
     * │                                                                     │
     * │    ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐                        │
     * │    │ 5 │ 3 │ 6 │ 4 │ 2 │ 7 │ 5 │ 8 │ 4 │ 6 │ ← 每格请求数            │
     * │    └───┴───┴───┴───┴───┴───┴───┴───┴───┴───┘                        │
     * │      ↑                                           ↑                  │
     * │    旧窗口滑动                                 当前时间              │
     * │                                                                     │
     * │    总请求数 = 所有格子请求数之和 = 50                                │
     * │    QPS = 50 / 1秒 = 50                                              │
     * │                                                                     │
     * │    优点：统计精确                                                    │
     * │    缺点：占用内存                                                    │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ 2. 漏桶算法                                                         │
     * │                                                                     │
     * │    请求像水一样流入桶中，以固定速率流出                                │
     * │                                                                     │
     * │           请求（水）                                                 │
     * │              ↓                                                       │
     * │    ┌──────────────────┐                                             │
     * │    │     漏桶         │                                             │
     * │    │  ════════════    │ ← 桶容量（缓存队列）                         │
     * │    │                  │                                             │
     * │    │    ────────      │ ← 固定速率流出                               │
     * │    └──────────────────┘                                             │
     * │              ↓                                                       │
     * │           处理请求                                                   │
     * │                                                                     │
     * │    优点：流量平滑                                                    │
     * │    缺点：无法应对突发流量                                             │
     * ├─────────────────────────────────────────────────────────────────────┤
     * │ 3. 令牌桶算法                                                        │
     * │                                                                     │
     * │    以固定速率生成令牌，请求获取令牌才能通过                            │
     * │                                                                     │
     * │           令牌生成器                                                 │
     * │              ↓                                                       │
     * │    ┌──────────────────┐                                             │
     * │    │  ● ● ● ○ ○ ○    │ ← 桶中令牌（有空位）                          │
     * │    │  令牌桶          │                                             │
     * │    └──────────────────┘                                             │
     * │              ↓                                                       │
     * │         请求取令牌                                                   │
     * │              ↓                                                       │
     * │    有令牌 → 通过                                                     │
     * │    无令牌 → 拒绝                                                     │
     * │                                                                     │
     * │    优点：允许一定程度的突发流量                                        │
     * │    缺点：实现相对复杂                                                 │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    /**
     * 【限流规则配置】
     *
     * FlowRule 属性：
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 属性             │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ resource        │ 资源名称                                             │
     * │ count           │ 限流阈值（QPS 或线程数）                              │
     * │ grade           │ 限流模式：QPS模式 / 线程数模式                        │
     * │ limitApp        │ 来源应用（default 表示所有来源）                      │
     * │ strategy        │ 流控策略：直接拒绝 / 关联 / 链路                      │
     * │ controlBehavior │ 流控效果：快速失败 / Warm Up / 排队等待               │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     *
     * 【流控策略】
     *
     * 1. 直接拒绝（默认）
     *    - QPS 超过阈值直接拒绝
     *
     * 2. 关联限流
     *    - 当关联资源 B 的 QPS 超过阈值时，限流资源 A
     *    - 场景：支付接口繁忙时，限流下单接口
     *
     * 3. 链路限流
     *    - 只记录从入口资源进来的流量
     *    - 场景：同一个接口被多个入口调用，只限流某个入口
     *
     * 【流控效果】
     *
     * 1. 快速失败（默认）
     *    - 直接抛出 FlowException
     *
     * 2. Warm Up（预热）
     *    - 冷启动，阈值从 threshold/3 逐渐增加到 threshold
     *    - 适用于：缓存预热、秒杀场景
     *
     *    ┌─────────────────────────────────────────────────────────────────┐
     * │    QPS                                                            │
     * │      ↑                                                            │
     * │  100 ├─────────────────────────────                               │
     * │      │                            ╲                               │
     * │      │                             ╲                              │
     * │   33 ├──────────────────────────────╲────────                     │
     * │      │                                                              │
     * │      └───────────────────────────────────────────→ 时间            │
     * │           0        5s       10s      15s                           │
     * │           (warmupPeriod)                                            │
     * └─────────────────────────────────────────────────────────────────────┘
     *
     * 3. 排队等待（匀速排队）
     *    - 请求在队列中排队，按照间隔时间依次通过
     *    - 适用于：削峰填谷
     */

    /**
     * 【限流代码示例】
     */
    /*
    // 方式1：代码定义规则
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule rule = new FlowRule();
        rule.setResource("getUser");           // 资源名
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);  // QPS 模式
        rule.setCount(100);                    // 每秒最多 100 次
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);  // 直接拒绝
        rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);  // 快速失败

        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    // 方式2：使用 @SentinelResource 注解
    @SentinelResource(
        value = "getUser",                    // 资源名
        blockHandler = "getUserBlockHandler", // 限流处理方法
        fallback = "getUserFallback"          // 降级处理方法
    )
    public User getUser(Long userId) {
        return userRepository.findById(userId);
    }

    // 限流处理方法（必须与原方法签名一致，最后加 BlockException 参数）
    public User getUserBlockHandler(Long userId, BlockException ex) {
        log.warn("getUser 被限流: {}", userId);
        return User.builder()
            .id(userId)
            .name("限流用户")
            .build();
    }

    // 降级处理方法（处理业务异常）
    public User getUserFallback(Long userId, Throwable ex) {
        log.error("getUser 异常: {}", userId, ex);
        return User.builder()
            .id(userId)
            .name("降级用户")
            .build();
    }

    // 方式3：SphU 手动控制
    public User getUserManual(Long userId) {
        Entry entry = null;
        try {
            entry = SphU.entry("getUser");
            // 业务逻辑
            return userRepository.findById(userId);
        } catch (BlockException e) {
            // 限流处理
            return User.builder().id(userId).name("限流").build();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
    */

    // ==================== 熔断降级 ====================

    /**
     * 【熔断降级概述】
     *
     * 当下游服务出现故障（响应慢、错误率高）时，触发熔断，
     * 后续请求直接返回降级数据，避免级联故障。
     *
     * 【熔断状态机】
     *
     *                     失败率/慢调用比例 < 阈值
     *                  ┌─────────────────────────────┐
     *                  │                             │
     *                  ▼                             │
     *            ┌───────────┐                       │
     *            │  CLOSED   │◄──────────────────────┘
     *            │  (关闭)    │
     *            └─────┬─────┘
     *                  │
     *                  │ 失败率/慢调用比例 ≥ 阈值
     *                  │ 且超过最小请求数
     *                  ▼
     *            ┌───────────┐
     *            │   OPEN    │───────► 直接拒绝请求
     *            │  (打开)    │         返回降级数据
     *            └─────┬─────┘
     *                  │
     *                  │ 等待熔断时长结束
     *                  │
     *                  ▼
     *            ┌───────────┐
     *            │HALF-OPEN  │◄─── 探测请求
     *            │ (半开)     │
     *            └─────┬─────┘
     *                  │
     *    ┌─────────────┴─────────────┐
     *    │                           │
     *    │ 探测成功                   │ 探测失败
     *    ▼                           ▼
     * 回到 CLOSED               回到 OPEN
     */

    /**
     * 【熔断策略】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 策略             │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ 慢调用比例       │ 响应时间超过阈值的调用为慢调用                          │
     * │ (SLOW_REQUEST)  │ 当慢调用比例超过阈值时触发熔断                          │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ 异常比例         │ 异常数 / 总请求数 比例超过阈值时触发熔断                │
     * │ (ERROR_RATIO)   │                                                      │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ 异常数           │ 异常数超过阈值时触发熔断                               │
     * │ (ERROR_COUNT)   │                                                      │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     *
     * 【熔断规则配置】
     *
     * DegradeRule 属性：
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 属性             │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ resource        │ 资源名称                                             │
     * │ grade           │ 熔断策略：慢调用比例 / 异常比例 / 异常数               │
     * │ count           │ 阈值（慢调用 RT / 异常比例 / 异常数）                  │
     * │ timeWindow      │ 熔断时长（秒）                                        │
     * │ minRequestAmount│ 最小请求数（触发熔断的最小请求数）                      │
     * │ statIntervalMs  │ 统计时长（毫秒）                                      │
     * │ slowRatioThreshold │ 慢调用比例阈值（0.0-1.0）                          │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     */

    /**
     * 【熔断代码示例】
     */
    /*
    // 定义熔断规则
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        DegradeRule rule = new DegradeRule();
        rule.setResource("callRemoteService");
        rule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());
        rule.setCount(500);                      // 慢调用阈值：500ms
        rule.setSlowRatioThreshold(0.5);         // 慢调用比例：50%
        rule.setMinRequestAmount(10);            // 最小请求数：10
        rule.setStatIntervalMs(10000);           // 统计时长：10秒
        rule.setTimeWindow(30);                  // 熔断时长：30秒

        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
    }

    // 使用注解
    @SentinelResource(
        value = "callRemoteService",
        blockHandler = "handleBlock",
        fallback = "handleFallback"
    )
    public String callRemoteService(String param) {
        // 调用远程服务
        return remoteClient.call(param);
    }

    // 熔断降级处理
    public String handleBlock(String param, BlockException ex) {
        if (ex instanceof DegradeException) {
            return "服务熔断降级中，请稍后重试";
        } else if (ex instanceof FlowException) {
            return "服务繁忙，请稍后重试";
        }
        return "系统繁忙";
    }

    // 异常降级处理
    public String handleFallback(String param, Throwable ex) {
        log.error("远程服务调用异常", ex);
        return "服务异常，返回默认值";
    }
    */

    // ==================== 热点参数限流 ====================

    /**
     * 【热点参数限流】
     *
     * 针对请求中的热点参数进行限流，如：
     * - 商品ID：热门商品限流，普通商品不限流
     * - 用户ID：VIP用户不限流，普通用户限流
     *
     * 【原理】
     *
     * 使用 LRU Map 统计每个参数值的 QPS，对热点参数值进行限流。
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                        请求流量                                     │
     * │                           ↓                                         │
     * │   ┌───────────────────────────────────────────────────────────┐    │
     * │   │                    参数值统计                              │    │
     * │   │  ┌────────────────────────────────────────────────────┐  │    │
     * │   │  │  参数值      │  QPS  │  是否热点  │  限流？         │  │    │
     * │   │  ├─────────────┼───────┼───────────┼────────────────┤  │    │
     * │   │  │  商品A      │  1000 │   是      │  限流（超过100）│  │    │
     * │   │  │  商品B      │   50  │   否      │  不限流        │  │    │
     * │   │  │  商品C      │  800  │   是      │  限流（超过100）│  │    │
     * │   │  │  商品D      │   30  │   否      │  不限流        │  │    │
     * │   │  └─────────────┴───────┴───────────┴────────────────┘  │    │
     * │   └───────────────────────────────────────────────────────────┘    │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    /**
     * 【热点参数限流规则】
     */
    /*
    private void initParamFlowRules() {
        ParamFlowRule rule = new ParamFlowRule();
        rule.setResource("getProduct");
        rule.setCount(100);                         // 默认阈值
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setDurationInSec(1);                   // 统计窗口：1秒

        // 特定参数值单独设置阈值
        List<ParamFlowItem> items = new ArrayList<>();

        // 热门商品限流阈值更高
        ParamFlowItem item1 = new ParamFlowItem();
        item1.setObject("1001");                    // 商品ID
        item1.setClassType(String.class.getName());
        item1.setCount(500);                        // 阈值 500
        items.add(item1);

        // VIP 用户不限流
        ParamFlowItem item2 = new ParamFlowItem();
        item2.setObject("vip_user");
        item2.setClassType(String.class.getName());
        item2.setCount(10000);                      // 阈值 10000
        items.add(item2);

        rule.setParamFlowItemList(items);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    // 使用注解（必须是 SphU.entry 或 @SentinelResource）
    @SentinelResource(
        value = "getProduct",
        blockHandler = "getProductBlock"
    )
    public Product getProduct(@SentinelResource("productId") String productId) {
        return productRepository.findById(productId);
    }

    public Product getProductBlock(String productId, BlockException ex) {
        return Product.builder()
            .id(productId)
            .name("商品信息加载中，请稍后重试")
            .build();
    }
    */

    // ==================== 系统自适应保护 ====================

    /**
     * 【系统自适应保护】
     *
     * 从系统整体维度（CPU、内存、QPS、RT）进行保护，
     * 避免系统过载导致整体不可用。
     *
     * 【自适应算法】
     *
     * Sentinel 使用 BBR（Bottleneck Bandwidth and RTT）思想的变种：
     *
     *   系统容量 = min(CPU使用率, 平均响应时间) 相关因子
     *
     * 当系统负载过高时，自动触发限流。
     *
     * 【系统规则】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 类型             │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ Load            │ 系统负载（Linux 1分钟平均负载）                       │
     * │ CPU使用率        │ CPU 使用率超过阈值触发限流                           │
     * │ 平均RT          │ 所有入口流量的平均响应时间                           │
     * │ 并发线程数       │ 并发线程数超过阈值触发限流                           │
     * │ 入口QPS         │ 所有入口流量的 QPS                                   │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     */
    /*
    private void initSystemRules() {
        List<SystemRule> rules = new ArrayList<>();

        SystemRule rule = new SystemRule();
        rule.setHighestSystemLoad(10);    // 系统负载阈值
        rule.setHighestCpuUsage(0.8);     // CPU 使用率 80%
        rule.setAvgRt(500);               // 平均响应时间 500ms
        rule.setMaxThread(500);           // 最大并发线程
        rule.setQps(10000);               // 入口 QPS 阈值

        rules.add(rule);
        SystemRuleManager.loadRules(rules);
    }
    */

    // ==================== 控制台使用 ====================

    /**
     * 【Sentinel Dashboard】
     *
     * 功能：
     * 1. 实时监控：QPS、RT、并发线程数
     * 2. 规则管理：动态配置限流/熔断规则
     * 3. 集群管理：集群限流配置
     * 4. 机器管理：应用实例管理
     *
     * 【控制台部署】
     *
     * 下载 sentinel-dashboard.jar：
     *   https://github.com/alibaba/Sentinel/releases
     *
     * 启动：
     *   java -Dserver.port=8080 \
     *        -Dcsp.sentinel.dashboard.server=localhost:8080 \
     *        -Dproject.name=sentinel-dashboard \
     *        -jar sentinel-dashboard-1.8.6.jar
     *
     * 访问：http://localhost:8080
     * 默认账号：sentinel / sentinel
     *
     * 【应用接入控制台】
     *
     * application.yml:
     *   spring:
     *     cloud:
     *       sentinel:
     *         transport:
     *           dashboard: localhost:8080
     *           port: 8719              # 与控制台通信端口
     *         eager: true               # 应用启动时立即连接
     *         datasource:               # 规则持久化
     *           flow:
     *             nacos:
     *               server-addr: localhost:8848
     *               data-id: ${spring.application.name}-flow-rules
     *               group-id: SENTINEL_GROUP
     *               rule-type: flow
     */

    // ==================== 规则持久化 ====================

    /**
     * 【为什么需要持久化】
     *
     * 默认情况下，Sentinel 规则存储在内存中，应用重启后丢失。
     * 需要将规则持久化到外部存储（Nacos、Apollo、ZooKeeper 等）。
     *
     * 【持久化方案】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                     Sentinel 规则持久化                              │
     * │                                                                     │
     * │   ┌─────────────┐         ┌─────────────┐         ┌─────────────┐  │
     * │   │  Dashboard  │◄───────►│    Nacos    │◄───────►│   App       │  │
     * │   │  (控制台)   │         │  (配置中心)  │         │  (应用)     │  │
     * │   └─────────────┘         └─────────────┘         └─────────────┘  │
     * │          │                       │                       │         │
     * │          │      规则变更          │      规则推送          │         │
     * │          │──────────────────────►│──────────────────────►│         │
     * │          │                       │                       │         │
     * │          │                       │      规则同步          │         │
     * │          │                       │◄──────────────────────│         │
     * └─────────────────────────────────────────────────────────────────────┘
     */
    /*
    // Nacos 持久化配置
    spring:
      cloud:
        sentinel:
          datasource:
            # 限流规则
            flow:
              nacos:
                server-addr: localhost:8848
                data-id: ${spring.application.name}-flow-rules
                group-id: SENTINEL_GROUP
                rule-type: flow
            # 熔断规则
            degrade:
              nacos:
                server-addr: localhost:8848
                data-id: ${spring.application.name}-degrade-rules
                group-id: SENTINEL_GROUP
                rule-type: degrade
            # 热点参数规则
            param-flow:
              nacos:
                server-addr: localhost:8848
                data-id: ${spring.application.name}-param-rules
                group-id: SENTINEL_GROUP
                rule-type: param-flow
            # 系统规则
            system:
              nacos:
                server-addr: localhost:8848
                data-id: ${spring.application.name}-system-rules
                group-id: SENTINEL_GROUP
                rule-type: system
    */

    // ==================== 集群限流 ====================

    /**
     * 【集群限流】
     *
     * 单机限流无法精确控制整个集群的总 QPS。
     * 集群限流通过 Token Server 统一分配令牌。
     *
     * 【架构】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                       集群限流架构                                   │
     * │                                                                     │
     * │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐               │
     * │   │   Client    │  │   Client    │  │   Client    │               │
     * │   │  (应用实例)  │  │  (应用实例)  │  │  (应用实例)  │               │
     * │   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘               │
     * │          │                │                │                       │
     * │          │    请求令牌     │                │                       │
     * │          └────────────────┴────────────────┘                       │
     * │                           │                                         │
     * │                           ▼                                         │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   Token Server                               │  │
     * │   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │  │
     * │   │  │ 规则管理    │  │ 令牌分配    │  │ 统计计数    │         │  │
     * │   │  └─────────────┘  └─────────────┘  └─────────────┘         │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * └─────────────────────────────────────────────────────────────────────┘
     *
     * 【配置示例】
     */
    /*
    // Token Server 配置
    @Configuration
    public class SentinelClusterConfig {

        @Bean
        public ClusterTokenServer clusterTokenServer() {
            ClusterFlowRuleManager.loadRules(Collections.singletonList(
                new ClusterFlowRule()
                    .setResourceId("cluster-getUser")
                    .setCount(1000)          // 集群总阈值
                    .setGrade(RuleConstant.FLOW_GRADE_QPS)
            ));

            return new DefaultClusterTokenServer(
                new ClusterTokenServerConfig()
                    .setPort(11111)
            );
        }
    }

    // Token Client 配置
    spring:
      cloud:
        sentinel:
          cluster:
            server:
              port: 11111
            client:
              mode: client
              server-addr: 192.168.1.100:11111
    */

    // ==================== 最佳实践 ====================

    /**
     * 【限流阈值设置】
     *
     * 1. 根据压测结果设置
     *    - 接口最大承受 QPS
     *    - 留 20%-30% 余量
     *
     * 2. 分级限流
     *    - 网关层：全局限流
     *    - 应用层：接口限流
     *    - 方法层：核心方法限流
     *
     * 3. 限流响应
     *    - 返回友好提示
     *    - HTTP 429 状态码
     *    - 提供重试建议
     *
     * 【熔断阈值设置】
     *
     * 1. 慢调用比例
     *    - RT 阈值：根据 SLA 设置
     *    - 比例阈值：50%-70%
     *    - 熔断时长：30秒-60秒
     *
     * 2. 异常比例
     *    - 比例阈值：50%
     *    - 最小请求数：10
     *
     * 【降级策略】
     *
     * 1. 返回默认值
     *    - 适用于查询类接口
     *
     * 2. 返回缓存数据
     *    - 适用于数据一致性要求不高场景
     *
     * 3. 调用备用服务
     *    - 适用于有备用服务场景
     *
     * 4. 返回错误提示
     *    - 适用于无法降级场景
     */

    /**
     * 【完整配置示例】
     */
    /*
    @Configuration
    public class SentinelConfig {

        @PostConstruct
        public void initRules() {
            initFlowRules();
            initDegradeRules();
            initSystemRules();
        }

        private void initFlowRules() {
            List<FlowRule> rules = new ArrayList<>();

            // API 接口限流
            FlowRule apiRule = new FlowRule();
            apiRule.setResource("api-getUser");
            apiRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
            apiRule.setCount(100);
            apiRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
            rules.add(apiRule);

            // 数据库查询限流
            FlowRule dbRule = new FlowRule();
            dbRule.setResource("db-query");
            dbRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);  // 线程数限流
            dbRule.setCount(50);
            rules.add(dbRule);

            FlowRuleManager.loadRules(rules);
        }

        private void initDegradeRules() {
            List<DegradeRule> rules = new ArrayList<>();

            DegradeRule rule = new DegradeRule("remote-service");
            rule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
            rule.setCount(0.5);            // 异常比例 50%
            rule.setMinRequestAmount(10);
            rule.setStatIntervalMs(10000);
            rule.setTimeWindow(30);

            rules.add(rule);
            DegradeRuleManager.loadRules(rules);
        }

        private void initSystemRules() {
            List<SystemRule> rules = new ArrayList<>();

            SystemRule rule = new SystemRule();
            rule.setHighestCpuUsage(0.8);
            rule.setMaxThread(500);
            rule.setAvgRt(1000);

            rules.add(rule);
            SystemRuleManager.loadRules(rules);
        }
    }
    */
}
