package com.example.demo.framework.springcloud;

/**
 * 服务注册与发现
 *
 * 【核心概念】
 * 服务注册：服务启动时向注册中心注册自己的地址
 * 服务发现：服务调用时从注册中心获取目标服务地址
 *
 * 【主流注册中心对比】
 * ┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
 * │ 注册中心          │ CAP             │ 特点             │ 适用场景         │
 * ├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
 * │ Eureka          │ AP              │ 简单易用，已停止维护│ 老项目迁移       │
 * │ Nacos           │ AP/CP可切换      │ 功能全面，国产    │ 推荐，阿里生态   │
 * │ Consul          │ CP              │ Go语言，多数据中心│ 异构系统集成     │
 * │ ZooKeeper       │ CP              │ 强一致性，复杂    │ Dubbo生态       │
 * └─────────────────┴─────────────────┴─────────────────┴─────────────────┘
 *
 * 【CAP理论】
 * C - Consistency（一致性）
 * A - Availability（可用性）
 * P - Partition Tolerance（分区容错性）
 *
 * 分布式系统只能同时满足两个：
 * - AP：高可用，允许短暂不一致（Eureka、Nacos默认）
 * - CP：强一致，可能不可用（ZooKeeper、Consul）
 */
public class ServiceDiscovery {

    // ==================== 服务注册流程 ====================

    /**
     * 服务注册核心流程：
     *
     * ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
     * │ 服务提供者    │───→│   注册中心   │←───│ 服务消费者   │
     * │ Provider    │    │  Registry   │    │ Consumer    │
     * └─────────────┘    └─────────────┘    └─────────────┘
     *        │                  │                  │
     *        │ 1.启动注册        │ 2.订阅服务        │
     *        │─────────────────→│←─────────────────│
     *        │                  │                  │
     *        │ 3.心跳保活        │ 4.推送变更        │
     *        │─────────────────→│─────────────────→│
     *        │                  │                  │
     *        │                  │ 5.拉取地址        │
     *        │                  │←─────────────────│
     *
     * 步骤详解：
     * 1. 服务启动时，向注册中心发送注册请求（IP、端口、服务名）
     * 2. 消费者订阅需要调用的服务
     * 3. 服务定时发送心跳，证明自己存活
     * 4. 注册中心服务列表变更时，推送通知消费者
     * 5. 消费者缓存服务地址，调用时直接使用
     */

    // ==================== Nacos服务注册示例 ====================

    /**
     * Nacos配置示例
     *
     * spring:
     *   application:
     *     name: user-service
     *   cloud:
     *     nacos:
     *       discovery:
     *         server-addr: 127.0.0.1:8848
     *         namespace: public
     *         group: DEFAULT_GROUP
     *         service: ${spring.application.name}
     *         weight: 1
     *         metadata:
     *           version: 1.0.0
     *           region: beijing
     */

    /**
     * Nacos服务注册核心代码（伪代码）
     */
    /*
    @Service
    public class NacosServiceRegistry {

        private final NamingService namingService;

        // 服务注册
        public void register(String serviceName, String ip, int port) {
            Instance instance = new Instance();
            instance.setIp(ip);
            instance.setPort(port);
            instance.setServiceName(serviceName);
            instance.setWeight(1.0);
            instance.setHealthy(true);
            instance.setMetadata(Map.of("version", "1.0.0"));

            namingService.registerInstance(serviceName, instance);
        }

        // 服务发现
        public List<Instance> discover(String serviceName) {
            return namingService.getAllInstances(serviceName);
        }

        // 心跳机制
        public void sendHeartbeat(String serviceName, Instance instance) {
            // Nacos客户端自动发送心跳，默认5秒一次
            // 如果超过15秒没有收到心跳，标记为不健康
            // 如果超过30秒没有收到心跳，从列表中移除
        }
    }
    */

    // ==================== 心跳机制 ====================

    /**
     * 心跳机制详解
     *
     * ┌────────────────────────────────────────────────────────┐
     * │                      心跳时间线                          │
     * ├────────────────────────────────────────────────────────┤
     * │ 0s     服务注册成功                                      │
     * │ 5s     发送心跳1  ─────────→  注册中心记录存活            │
     * │ 10s    发送心跳2  ─────────→  注册中心记录存活            │
     * │ ...                                                      │
     * │ 15s    如果未收到心跳，标记为不健康（但仍保留）            │
     * │ ...                                                      │
     * │ 30s    如果仍未收到心跳，从服务列表中移除                  │
     * └────────────────────────────────────────────────────────┘
     *
     * 各注册中心心跳配置：
     * - Nacos：心跳间隔5秒，不健康阈值15秒，移除阈值30秒
     * - Eureka：心跳间隔30秒，移除阈值90秒
     * - Consul：心跳间隔10秒，移除阈值10秒
     */

    // ==================== 服务下线 ====================

    /**
     * 服务下线流程
     *
     * 1. 优雅下线（主动）
     *    - 服务关闭时主动通知注册中心
     *    - 注册中心立即更新服务列表
     *    - 通知订阅的消费者
     *
     * 2. 被动下线（异常）
     *    - 服务崩溃，无法主动通知
     *    - 依靠心跳超时机制移除
     *    - 存在短暂的服务列表不一致
     *
     * 优雅下线实现：
     */
    /*
    @PreDestroy
    public void gracefulShutdown() {
        // 1. 从注册中心注销
        namingService.deregisterInstance(serviceName, ip, port);

        // 2. 等待正在处理的请求完成
        Thread.sleep(5000);

        // 3. 关闭服务
    }
    */

    // ==================== 服务列表缓存 ====================

    /**
     * 本地缓存机制
     *
     * 消费者会缓存服务列表到本地：
     * 1. 减少对注册中心的请求压力
     * 2. 注册中心不可用时，仍可用本地缓存
     * 3. 定时或事件驱动更新缓存
     *
     * Nacos本地缓存文件：
     * {user.home}/nacos/naming/{namespace}/{serviceName}
     *
     * 缓存更新策略：
     * - 定时拉取：每隔一段时间主动拉取
     * - UDP推送：服务变更时注册中心主动推送
     */

    // ==================== 集群模式 ====================

    /**
     * 注册中心集群
     *
     * Nacos集群架构：
     *
     *          ┌──────────────────────────────────┐
     *          │            Nginx/SLB             │
     *          │      (负载均衡入口)               │
     *          └─────────────────┬────────────────┘
     *                            │
     *      ┌─────────────────────┼─────────────────────┐
     *      │                     │                     │
     *      ▼                     ▼                     ▼
     * ┌─────────┐          ┌─────────┐          ┌─────────┐
     * │ Nacos-1 │◄────────►│ Nacos-2 │◄────────►│ Nacos-3 │
     │ │  Leader │          │Follower │          │Follower │
     * └─────────┘          └─────────┘          └─────────┘
     *      │                     │                     │
     *      └─────────────────────┼─────────────────────┘
     *                            │
     *                            ▼
     *                     ┌─────────────┐
     *                     │   MySQL     │
     *                     │ (数据持久化) │
     *                     └─────────────┘
     *
     * 集群选举：Raft协议
     * - 选举Leader处理写请求
     * - Follower同步Leader数据
     * - Leader故障时重新选举
     */

    // ==================== 元数据 ====================

    /**
     * 服务元数据
     *
     * 可用于：
     * 1. 灰度发布：按版本路由
     * 2. 就近访问：按区域路由
     * 3. 环境隔离：按环境路由
     *
     * 示例：
     */
    /*
    Instance instance = new Instance();
    instance.setIp("192.168.1.100");
    instance.setPort(8080);
    instance.setMetadata(Map.of(
        "version", "v2",          // 版本号
        "region", "beijing",      // 区域
        "env", "gray",            // 环境
        "weight", "80"            // 权重
    ));
    */

    /**
     * 基于元数据的路由
     */
    /*
    @Component
    public class MetadataLoadBalancer implements ReactorServiceInstanceLoadBalancer {

        @Override
        public Mono<Response<ServiceInstance>> choose(Request request) {
            // 获取请求中的元数据要求
            String targetVersion = request.getContext().get("version");

            // 筛选匹配的服务实例
            List<ServiceInstance> instances = serviceInstances.stream()
                .filter(instance -> targetVersion.equals(
                    instance.getMetadata().get("version")))
                .collect(Collectors.toList());

            // 负载均衡选择
            ServiceInstance selected = selectInstance(instances);
            return Mono.just(new DefaultResponse(selected));
        }
    }
    */

    // ==================== 健康检查 ====================

    /**
     * 健康检查机制
     *
     * 1. 客户端心跳
     *    - 服务主动发送心跳
     *    - 简单高效
     *    - 但不能完全代表服务健康
     *
     * 2. 服务端主动探测
     *    - 注册中心主动调用健康检查接口
     *    - 更准确但开销大
     *
     * Nacos支持两种模式：
     * - 临时实例：客户端心跳（默认）
     * - 持久实例：服务端探测
     */
    /*
    # 配置为持久实例
    spring.cloud.nacos.discovery.ephemeral=false
    */

    // ==================== 面试要点 ====================

    /**
     * Q1: Eureka和Nacos的区别？
     * A: 1) Eureka已停止维护，Nacos活跃
     *    2) Eureka只有AP模式，Nacos支持AP/CP切换
     *    3) Nacos集成了配置中心
     *    4) Nacos支持持久实例和临时实例
     *
     * Q2: 注册中心挂了怎么办？
     * A: 1) 消费者使用本地缓存，仍可调用
     *    2) 注册中心集群，避免单点故障
     *    3) 服务提供者重启时无法注册新服务
     *
     * Q3: 如何实现灰度发布？
     * A: 1) 服务注册时带版本元数据
     *    2) 网关/负载均衡器根据请求头路由到对应版本
     *    3) 新版本逐步放量，观察后全量
     */
}
