package com.example.demo.framework.springcloud;

/**
 * Dubbo —— 高性能 RPC 框架
 *
 * 【什么是 Dubbo】
 * Dubbo 是阿里巴巴开源的高性能 Java RPC 框架，提供：
 * 1. 远程过程调用（RPC）
 * 2. 智能负载均衡
 * 3. 服务自动注册与发现
 * 4. 高度可扩展能力
 *
 * 【RPC 是什么】
 * RPC（Remote Procedure Call）远程过程调用，让调用远程服务像调用本地方法一样简单。
 *
 *   本地调用：     对象.方法(参数)
 *   RPC 调用：     代理对象.方法(参数)  ← 感觉像本地调用，实际是远程调用
 *
 * 【Dubbo vs Spring Cloud】
 * ┌─────────────────┬─────────────────────────┬─────────────────────────┐
 * │ 对比项           │ Dubbo                   │ Spring Cloud            │
 * ├─────────────────┼─────────────────────────┼─────────────────────────┤
 * │ 通信协议         │ TCP（默认 Dubbo 协议）   │ HTTP（RESTful）         │
 * │ 性能             │ 高（二进制协议）         │ 中（文本协议）           │
 * │ 服务治理         │ 完善                    │ 依赖组件多               │
 * │ 学习成本         │ 中                      │ 低                      │
 * │ 社区活跃度       │ 高（阿里维护）           │ 高（Pivotal 维护）       │
 * │ 生态             │ 阿里系                  │ Spring 全家桶            │
 * └─────────────────┴─────────────────────────┴─────────────────────────┘
 */
public class DubboRPC {

    // ==================== 核心架构 ====================

    /**
     * 【Dubbo 架构图】
     *
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │                           Dubbo 架构                                     │
     * │                                                                         │
     * │    ┌─────────────────┐                          ┌─────────────────┐    │
     * │    │   Consumer      │                          │   Provider      │    │
     * │    │   (服务消费者)   │                          │   (服务提供者)   │    │
     * │    │                 │                          │                 │    │
     * │    │  ┌───────────┐  │                          │  ┌───────────┐  │    │
     * │    │  │  Proxy    │  │                          │  │  Proxy    │  │    │
     * │    │  │  (代理)    │  │                          │  │  (代理)    │  │    │
     * │    │  └─────┬─────┘  │                          │  └─────┬─────┘  │    │
     * │    │        │        │                          │        │        │    │
     * │    │  ┌─────▼─────┐  │                          │  ┌─────▲─────┐  │    │
     * │    │  │  Cluster  │  │                          │  │  Filter   │  │    │
     * │    │  │  (集群)    │  │                          │  │  (过滤器)  │  │    │
     * │    │  └─────┬─────┘  │                          │  └─────▲─────┘  │    │
     * │    │        │        │                          │        │        │    │
     * │    │  ┌─────▼─────┐  │      ┌────────────┐      │  ┌─────▲─────┐  │    │
     * │    │  │  Protocol │  │◄────►│  Registry  │◄────►│  │  Protocol │  │    │
     * │    │  │  (协议)    │  │      │  (注册中心) │      │  │  (协议)    │  │    │
     * │    │  └─────┬─────┘  │      └─────┬──────┘      │  └─────▲─────┘  │    │
     * │    │        │        │            │             │        │        │    │
     * │    │  ┌─────▼─────┐  │            │             │  ┌─────▲─────┐  │    │
     * │    │  │  Exchange │  │            │             │  │  Exchange │  │    │
     * │    │  │  (信息交换) │  │            │             │  │  (信息交换) │  │    │
     * │    │  └─────┬─────┘  │            │             │  └─────▲─────┘  │    │
     * │    │        │        │            │             │        │        │    │
     * │    │  ┌─────▼─────┐  │            │             │  ┌─────▲─────┐  │    │
     * │    │  │  Transport │◄─┼────────────┼─────────────┼─►│  Transport │  │    │
     * │    │  │  (网络传输) │  │            │             │  │  (网络传输) │  │    │
     * │    │  └───────────┘  │            │             │  └───────────┘  │    │
     * │    └─────────────────┘            │             └─────────────────┘    │
     * │                                   │                                    │
     * │                          ┌────────▼────────┐                          │
     * │                          │    Monitor      │                          │
     * │                          │   (监控中心)     │                          │
     * │                          └─────────────────┘                          │
     * └─────────────────────────────────────────────────────────────────────────┘
     *
     * 【角色说明】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 角色             │ 职责                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ Provider        │ 服务提供者，暴露服务接口                              │
     * │ Consumer        │ 服务消费者，调用远程服务                              │
     * │ Registry        │ 注册中心，服务注册与发现（Nacos/ZooKeeper/Redis）      │
     * │ Monitor         │ 监控中心，统计服务调用次数和时间                       │
     * │ Container       │ 服务容器，管理服务生命周期                            │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     *
     * 【调用流程】
     *
     * 1. 服务启动
     *    Provider → Registry: 注册服务
     *    Consumer → Registry: 订阅服务
     *
     * 2. 服务发现
     *    Registry → Consumer: 推送服务列表
     *
     * 3. 服务调用
     *    Consumer → Provider: 直接调用（RPC）
     *    Consumer → Monitor: 上报调用统计
     *    Provider → Monitor: 上报调用统计
     */

    // ==================== 工作原理 ====================

    /**
     * 【服务暴露流程（Provider 端）】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                     服务暴露流程                                     │
     * │                                                                     │
     * │   Spring 容器启动                                                   │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   解析 @Service 注解                                                │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   ServiceConfig                             │  │
     * │   │  - 解析配置（接口、实现类、协议、端口等）                      │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   ProtocolConfig                             │  │
     * │   │  - 创建 Protocol（如 DubboProtocol）                         │  │
     * │   │  - 打开 Server（NettyServer）                                │  │
     * │   │  - 绑定端口（默认 20880）                                    │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   创建 Invoker                               │  │
     * │   │  - 包装服务实现为 Invoker                                    │  │
     * │   │  - Invoker 是 Dubbo 核心模型，代表可执行体                   │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   注册到注册中心                              │  │
     * │   │  - Registry.register(url)                                   │  │
     * │   │  - URL: dubbo://192.168.1.1:20880/com.example.UserService   │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    /**
     * 【服务引用流程（Consumer 端）】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                     服务引用流程                                     │
     * │                                                                     │
     * │   Spring 容器启动                                                   │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   解析 @Reference 注解                                              │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   ReferenceConfig                           │  │
     * │   │  - 解析配置（接口、注册中心、负载均衡等）                      │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   从注册中心订阅服务列表                      │  │
     * │   │  - Registry.subscribe(url, listener)                        │  │
     * │   │  - 获取 Provider 地址列表                                    │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   创建 Invoker                               │  │
     * │   │  - 为每个 Provider 创建 Invoker                              │  │
     * │   │  - 通过 Cluster 包装为集群 Invoker                           │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   创建代理对象                                │  │
     * │   │  - ProxyFactory.getProxy(invoker)                           │  │
     * │   │  - 返回接口代理，注入到 Spring 容器                          │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    /**
     * 【服务调用流程】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                     服务调用流程                                     │
     * │                                                                     │
     * │   consumer.method(params)                                          │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   代理对象拦截                                │  │
     * │   │  - InvokerInvocationHandler.invoke(method, args)            │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   Cluster Invoker                           │  │
     * │   │  - 路由规则过滤（Router）                                    │  │
     * │   │  - 负载均衡选择（LoadBalance）                               │  │
     * │   │  - 集群容错（Cluster）                                       │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   Filter Chain（过滤器链）                   │  │
     * │   │  - 上下文传递                                                │  │
     * │   │  - 超时控制                                                  │  │
     * │   │  - 日志记录                                                  │  │
     * │   │  - 监控统计                                                  │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        ▼                                                            │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                   Protocol 发送请求                          │  │
     * │   │  - 编码请求（Serialization）                                 │  │
     * │   │  - 网络传输（Netty）                                         │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * │        │                                                            │
     * │        │  ──────────────────────────────────────────────────────►  │
     * │        │                    Provider                               │
     * │        │  ◄──────────────────────────────────────────────────────  │
     * │        │                    响应                                   │
     * │        ▼                                                            │
     * │   解码响应，返回结果                                               │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    // ==================== 协议与序列化 ====================

    /**
     * 【支持的协议】
     *
     * ┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
     * │ 协议             │ 说明             │ 特点             │ 适用场景         │
     * ├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
     * │ dubbo           │ 默认协议         │ 单连接、NIO      │ 小数据量、高并发 │
     * │ rmi             │ JDK RMI          │ 标准Java        │ 不推荐          │
     * │ hessian         │ Hessian协议      │ 跨语言          │ 跨语言场景      │
     * │ http            │ HTTP协议         │ RESTful         │ 简单场景        │
     * │ webservice      │ SOAP协议         │ 跨语言          │ 企业集成        │
     * │ thrift          │ Thrift协议       │ 高性能          │ 跨语言          │
     * │ grpc            │ gRPC协议         │ HTTP/2          │ 云原生场景      │
     * │ rest            │ REST协议         │ RESTful         │ 前端对接        │
     * └─────────────────┴─────────────────┴─────────────────┴─────────────────┘
     *
     * 【Dubbo 协议详解】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                    Dubbo 协议报文格式                                │
     * │                                                                     │
     * │   ┌────────┬────────┬────────┬────────┬────────┬────────┐          │
     * │   │ Magic  │  Flag  │ Status │  ID    │ DataLen│  Body  │          │
     * │   │ 2byte  │ 1byte  │ 1byte  │ 8byte │ 4byte  │ N byte │          │
     * │   └────────┴────────┴────────┴────────┴────────┴────────┘          │
     * │                                                                     │
     * │   Magic: 0xdabb（魔法数）                                           │
     * │   Flag: 标志位（请求/响应、序列化方式、单向/双向）                    │
     * │   Status: 状态码（仅响应消息有效）                                   │
     * │   ID: 消息唯一标识（用于请求响应匹配）                               │
     * │   DataLen: 数据长度                                                 │
     * │   Body: 序列化后的数据                                              │
     * └─────────────────────────────────────────────────────────────────────┘
     *
     * 【序列化方式】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 序列化           │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ hessian2        │ 默认序列化，跨语言，性能较好                          │
     * │ fastjson        │ 阿里 FastJSON，JSON 格式                             │
     * │ kryo            │ 高性能序列化，仅 Java                                │
     * │ fst             │ 高性能序列化，仅 Java                                │
     * │ protobuf        │ Google Protocol Buffers，跨语言                      │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     */

    // ==================== 负载均衡 ====================

    /**
     * 【负载均衡策略】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 策略             │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ Random          │ 随机策略（默认），按权重设置随机概率                   │
     * │ RoundRobin      │ 轮询策略，按权重轮询                                  │
     * │ LeastActive     │ 最少活跃调用数，使慢 Provider 少接请求                 │
     * │ ConsistentHash  │ 一致性哈希，相同参数总是发到同一 Provider              │
     * │ ShortestResponse│ 最短响应时间，优先选择响应快的 Provider                │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     *
     * 【一致性哈希】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                      一致性哈希环                                   │
     * │                                                                     │
     * │                      Provider A (150°)                             │
     * │                          ●                                         │
     * │                        /    \                                       │
     * │                      /        \                                     │
     * │       Provider D    /          \    Provider B                      │
     * │       (300°) ●-----            -----● (30°)                        │
     * │                    \          /                                     │
     * │                      \      /                                       │
     * │                        \  /                                         │
     * │                          ●                                         │
     * │                      Provider C (210°)                             │
     * │                                                                     │
     * │   请求参数哈希 → 定位到环上位置 → 顺时针找到第一个 Provider         │
     * │                                                                     │
     * │   优点：Provider 增减时只影响相邻节点                               │
     * │   场景：有状态服务、缓存等                                          │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    /**
     * 【负载均衡配置示例】
     */
    /*
    // 服务端配置权重
    @Service(weight = 100)  // 权重 100
    public class UserServiceImpl implements UserService { }

    // 消费端配置负载均衡策略
    @Reference(loadbalance = "roundrobin")
    private UserService userService;

    // 或在配置文件中
    dubbo:
      consumer:
        loadbalance: leastactive
    */

    // ==================== 集群容错 ====================

    /**
     * 【集群容错策略】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 策略             │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ Failover        │ 失败自动切换（默认），重试其他服务器                  │
     * │ Failfast        │ 快速失败，立即报错，不重试                            │
     * │ Failsafe        │ 失败安全，忽略异常，返回空结果                        │
     * │ Failback        │ 失败自动恢复，后台记录失败请求，定时重试               │
     * │ Forking         │ 并行调用，只要一个成功即返回                          │
     * │ Broadcast       │ 广播调用，调用所有提供者，任意一个报错则报错           │
     * │ Available       │ 调用第一个可用的服务                                  │
     * │ Mergeable       │ 合并结果，从多个 Provider 合并返回值                  │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     *
     * 【容错流程图】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                    Failover（失败重试）                              │
     * │                                                                     │
     * │   Consumer                                                         │
     * │      │                                                              │
     * │      │ 调用 Provider A                                              │
     * │      │───────────────────────► 失败                                │
     * │      │                                                              │
     * │      │ 重试 Provider B（重试 1）                                    │
     * │      │───────────────────────► 失败                                │
     * │      │                                                              │
     * │      │ 重试 Provider C（重试 2）                                    │
     * │      │───────────────────────► 成功                                │
     * │      │◄────────────────────── 返回结果                             │
     * │                                                                     │
     * │   默认重试次数：2 次（不含首次调用）                                 │
     * └─────────────────────────────────────────────────────────────────────┘
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                    Forking（并行调用）                               │
     * │                                                                     │
     * │   Consumer                                                         │
     * │      │                                                              │
     * │      ├───────────────────► Provider A                              │
     * │      │                    (并行)                                    │
     * │      ├───────────────────► Provider B                              │
     * │      │                    (并行)                                    │
     * │      ├───────────────────► Provider C                              │
     * │      │                    (并行)                                    │
     * │      │                                                              │
     * │      │◄─── 最先返回的结果                                          │
     * │      │    (取消其他请求)                                           │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    /**
     * 【集群容错配置示例】
     */
    /*
    // 注解方式
    @Reference(
        cluster = "failover",  // 容错策略
        retries = 3,           // 重试次数
        timeout = 5000         // 超时时间
    )
    private UserService userService;

    // 配置文件方式
    dubbo:
      consumer:
        cluster: failfast
        retries: 2
        timeout: 3000
    */

    // ==================== 服务降级 ====================

    /**
     * 【服务降级策略】
     *
     * 1. Mock 降级
     *    - 服务不可用时返回 Mock 数据
     *    - 可配置为返回空值或抛异常
     *
     * 2. 熔断降级
     *    - 结合 Sentinel/Hystrix 实现
     *    - 错误率达到阈值时触发熔断
     *
     * 【Mock 降级配置】
     */
    /*
    // 方式1：返回 null
    @Reference(mock = "return null")
    private UserService userService;

    // 方式2：返回空对象
    @Reference(mock = "return empty")
    private UserService userService;

    // 方式3：返回自定义值
    @Reference(mock = "return {\"id\":1,\"name\":\"默认用户\"}")
    private UserService userService;

    // 方式4：自定义 Mock 类
    @Reference(mock = "com.example.UserServiceMock")
    private UserService userService;

    // Mock 实现类
    public class UserServiceMock implements UserService {
        @Override
        public User getUser(Long id) {
            return User.builder()
                .id(id)
                .name("降级用户")
                .build();
        }
    }

    // 方式5：强制降级（在控制台配置）
    // 对服务进行屏蔽，直接返回 Mock 结果
    */

    // ==================== 注册中心 ====================

    /**
     * 【支持的注册中心】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 注册中心         │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ ZooKeeper       │ 官方推荐，CP 模型，强一致性                           │
     * │ Nacos           │ 阿里开源，AP 模型，支持配置中心                        │
     * │ Redis           │ 简单场景，基于发布订阅                                │
     * │ Multicast       │ 组播，仅用于开发测试                                  │
     * │ Simple          │ 简单注册中心，仅点对点                                │
     * │ Consul          │ HashiCorp 出品，支持服务发现                          │
     * │ Etcd            │ Kubernetes 使用，强一致性                             │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     *
     * 【注册中心数据结构】
     *
     * ZooKeeper 目录结构：
     *
     *   /dubbo
     *     /com.example.UserService
     *       /providers
     *         /dubbo://192.168.1.1:20880/com.example.UserService?...
     *         /dubbo://192.168.1.2:20880/com.example.UserService?...
     *       /consumers
     *         /consumer://192.168.1.100/com.example.UserService?...
     *       /routers
     *         /condition://... (路由规则)
     *       /configurators
     *         /override://... (动态配置)
     */

    /**
     * 【注册中心配置】
     */
    /*
    # Nacos 注册中心
    dubbo:
      registry:
        address: nacos://127.0.0.1:8848
        parameters:
          namespace: dev
          group: dubbo

    # ZooKeeper 注册中心
    dubbo:
      registry:
        address: zookeeper://127.0.0.1:2181
        timeout: 10000

    # Redis 注册中心
    dubbo:
      registry:
        address: redis://127.0.0.1:6379
    */

    // ==================== 使用示例 ====================

    /**
     * 【Maven 依赖】
     */
    /*
    <dependencies>
        <!-- Dubbo Spring Boot Starter -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
            <version>3.2.7</version>
        </dependency>

        <!-- 注册中心客户端（根据选择的注册中心）-->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-registry-nacos</artifactId>
            <version>3.2.7</version>
        </dependency>

        <!-- 或 ZooKeeper -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-registry-zookeeper</artifactId>
            <version>3.2.7</version>
        </dependency>
    </dependencies>
    */

    /**
     * 【定义服务接口】
     */
    /*
    // api 模块（单独的 jar 包）
    public interface UserService {
        User getUser(Long id);
        List<User> listUsers();
        boolean createUser(User user);
    }
    */

    /**
     * 【服务提供者】
     */
    /*
    // 服务实现
    @DubboService(
        version = "1.0.0",
        group = "user",
        timeout = 5000,
        retries = 2,
        loadbalance = "roundrobin"
    )
    public class UserServiceImpl implements UserService {

        @Autowired
        private UserMapper userMapper;

        @Override
        public User getUser(Long id) {
            return userMapper.selectById(id);
        }

        @Override
        public List<User> listUsers() {
            return userMapper.selectList(null);
        }

        @Override
        public boolean createUser(User user) {
            return userMapper.insert(user) > 0;
        }
    }

    // application.yml
    server:
      port: 8081

    spring:
      application:
        name: user-service-provider

    dubbo:
      application:
        name: user-service-provider
      protocol:
        name: dubbo
        port: 20880
      registry:
        address: nacos://127.0.0.1:8848
      scan:
        base-packages: com.example.service.impl
    */

    /**
     * 【服务消费者】
     */
    /*
    @Service
    public class OrderServiceImpl implements OrderService {

        @DubboReference(
            version = "1.0.0",
            group = "user",
            timeout = 3000,
            retries = 1,
            check = false,           // 启动时不检查服务是否可用
            stub = "com.example.UserServiceStub",  // 本地存根
            mock = "com.example.UserServiceMock"   // 服务降级
        )
        private UserService userService;

        @Override
        public Order createOrder(Long userId, Long productId) {
            // 调用远程服务（像本地方法一样）
            User user = userService.getUser(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 创建订单逻辑...
            Order order = new Order();
            order.setUserId(userId);
            order.setUserName(user.getName());
            return order;
        }
    }

    // application.yml
    server:
      port: 8082

    spring:
      application:
        name: order-service-consumer

    dubbo:
      application:
        name: order-service-consumer
      registry:
        address: nacos://127.0.0.1:8848
      consumer:
        timeout: 3000
        retries: 1
        check: false
    */

    /**
     * 【启动类】
     */
    /*
    @SpringBootApplication
    @EnableDubbo  // 启用 Dubbo
    public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    */

    // ==================== 高级特性 ====================

    /**
     * 【异步调用】
     */
    /*
    // 定义异步接口
    public interface UserService {
        // 同步方法
        User getUser(Long id);

        // 异步方法（返回 CompletableFuture）
        CompletableFuture<User> getUserAsync(Long id);
    }

    // 消费者异步调用
    @DubboReference(async = true)
    private UserService userService;

    public void asyncCall() {
        // 调用会立即返回
        userService.getUser(1L);

        // 通过 RpcContext 获取 Future
        CompletableFuture<User> future = RpcContext.getContext().getCompletableFuture();
        future.thenAccept(user -> {
            System.out.println("异步获取用户：" + user);
        });
    }

    // 或者使用 CompletableFuture 接口
    public void asyncCall2() {
        CompletableFuture<User> future = userService.getUserAsync(1L);
        future.thenAccept(user -> {
            System.out.println("异步获取用户：" + user);
        });
    }
    */

    /**
     * 【泛化调用】
     *
     * 无需接口定义，直接通过 GenericService 调用任意服务
     */
    /*
    @Bean
    public GenericService genericService() {
        ReferenceConfig<GenericService> config = new ReferenceConfig<>();
        config.setInterface("com.example.UserService");
        config.setGeneric(true);
        config.setRegistry(registryConfig);
        return config.get();
    }

    // 使用
    @Autowired
    private GenericService genericService;

    public void genericCall() {
        // $invoke(方法名, 参数类型, 参数值)
        Object result = genericService.$invoke(
            "getUser",
            new String[]{"java.lang.Long"},
            new Object[]{1L}
        );
    }
    */

    /**
     * 【隐式传参】
     */
    /*
    // 消费者设置参数
    public void callWithContext() {
        RpcContext.getContext()
            .setAttachment("traceId", UUID.randomUUID().toString())
            .setAttachment("userId", "1001");

        userService.getUser(1L);
    }

    // 提供者获取参数
    @DubboService
    public class UserServiceImpl implements UserService {
        @Override
        public User getUser(Long id) {
            String traceId = RpcContext.getContext().getAttachment("traceId");
            String userId = RpcContext.getContext().getAttachment("userId");
            // ...
        }
    }
    */

    /**
     * 【本地存根（Stub）】
     *
     * 在客户端执行部分逻辑，如缓存、参数校验等
     */
    /*
    // Stub 实现
    public class UserServiceStub implements UserService {

        private final UserService userService;  // 真实的代理对象
        private final Cache cache;

        // 构造函数由 Dubbo 自动注入
        public UserServiceStub(UserService userService) {
            this.userService = userService;
            this.cache = new ConcurrentHashMap();
        }

        @Override
        public User getUser(Long id) {
            // 1. 先查本地缓存
            User cached = cache.getIfPresent(id);
            if (cached != null) {
                return cached;
            }

            // 2. 调用远程服务
            User user = userService.getUser(id);

            // 3. 写入缓存
            cache.put(id, user);

            return user;
        }
    }

    // 配置使用 Stub
    @DubboReference(stub = "com.example.UserServiceStub")
    private UserService userService;
    */

    // ==================== SPI 扩展机制 ====================

    /**
     * 【Dubbo SPI】
     *
     * Dubbo 的扩展点机制，类似 Java SPI 但更强大：
     * 1. 按需加载（而非一次性加载所有）
     * 2. 支持扩展点自动装配
     * 3. 支持扩展点自动包装
     * 4. 支持扩展点自适应
     *
     * 【扩展点配置文件】
     *
     * 文件位置：META-INF/dubbo/接口全限定名
     *
     * 例：META-INF/dubbo/org.apache.dubbo.rpc.Protocol
     *
     *   dubbo=org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol
     *   http=org.apache.dubbo.rpc.protocol.http.HttpProtocol
     *   hessian=org.apache.dubbo.rpc.protocol.hessian.HessianProtocol
     *
     * 【使用扩展点】
     */
    /*
    // 获取扩展点
    Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class)
        .getExtension("dubbo");

    // 自定义扩展点
    @SPI("myProtocol")  // 默认实现
    public interface MyProtocol {
        void doSomething();
    }

    // 实现类
    public class MyProtocolImpl implements MyProtocol {
        @Override
        public void doSomething() {
            // ...
        }
    }

    // 配置文件：META-INF/dubbo/com.example.MyProtocol
    // myProtocol=com.example.MyProtocolImpl
    */

    // ==================== 最佳实践 ====================

    /**
     * 【包结构设计】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                       推荐包结构                                     │
     * │                                                                     │
     * │   project                                                           │
     * │   ├── user-api           # 接口定义模块                             │
     * │   │   └── UserService.java                                         │
     * │   │                                                                 │
     * │   ├── user-provider       # 服务提供者模块                          │
     * │   │   └── UserServiceImpl.java                                     │
     * │   │                                                                 │
     * │   ├── order-consumer      # 服务消费者模块                          │
     * │   │   └── OrderService.java                                        │
     * │   │                                                                 │
     * │   └── common             # 公共模块                                 │
     * │       └── CommonUtil.java                                          │
     * └─────────────────────────────────────────────────────────────────────┘
     *
     * 【接口设计原则】
     *
     * 1. 接口粒度适中
     *    - 不宜过细（网络开销）
     *    - 不宜过粗（耦合度高）
     *
     * 2. 参数校验
     *    - 提供者校验必须参数
     *    - 消费者校验可选参数
     *
     * 3. 版本控制
     *    - 接口变更使用版本号区分
     *    - 旧版本保留一段时间过渡
     *
     * 4. 异常处理
     *    - 自定义业务异常
     *    - 异常信息清晰明确
     *
     * 【超时设置建议】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 场景             │ 超时建议                                             │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ 简单查询         │ 1000-3000ms                                          │
     * │ 复杂查询         │ 3000-5000ms                                          │
     * │ 写操作          │ 3000-5000ms                                          │
     * │ 批量操作         │ 5000-10000ms                                         │
     * │ 跨服务聚合       │ 根据下游最大超时设置                                  │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     *
     * 【重试策略建议】
     *
     * - 幂等操作：可重试（查询、删除）
     * - 非幂等操作：不重试（新增、扣款）
     * - 重试次数：2-3 次
     * - 重试间隔：指数退避
     */
}
