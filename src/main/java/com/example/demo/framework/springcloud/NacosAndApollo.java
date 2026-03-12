package com.example.demo.framework.springcloud;

/**
 * Nacos 与 Apollo —— 注册中心与配置中心
 *
 * 【为什么需要注册中心】
 * 1. 服务实例动态变化（扩缩容、故障恢复）
 * 2. 服务实例网络地址不固定
 * 3. 需要自动发现服务实例
 *
 * 【为什么需要配置中心】
 * 1. 配置集中管理
 * 2. 配置动态更新（无需重启）
 * 3. 多环境配置隔离
 * 4. 配置变更审计
 *
 * 【主流方案】
 * ┌─────────────────┬─────────────────┬─────────────────┐
 * │ 产品             │ 注册中心         │ 配置中心         │
 * ├─────────────────┼─────────────────┼─────────────────┤
 * │ Nacos           │ ✅ 支持          │ ✅ 支持          │
 * │ Apollo          │ ❌ 不支持        │ ✅ 支持          │
 * │ Eureka          │ ✅ 支持          │ ❌ 不支持        │
 * │ Consul          │ ✅ 支持          │ ✅ 支持（较弱）   │
 * │ ZooKeeper       │ ✅ 支持          │ ✅ 支持（较弱）   │
 * └─────────────────┴─────────────────┴─────────────────┘
 */
public class NacosAndApollo {

    // ==================== Nacos 概述 ====================

    /**
     * Nacos（Dynamic Naming and Configuration Service）
     * 阿里巴巴开源的服务发现和配置管理平台
     *
     * 【核心功能】
     * 1. 服务注册与发现
     * 2. 动态配置管理
     * 3. 动态 DNS 服务
     * 4. 服务及其元数据管理
     *
     * 【架构设计】
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                           Nacos 架构                                 │
     * │                                                                     │
     * │   ┌─────────────┐     ┌─────────────┐     ┌─────────────┐         │
     * │   │  服务提供者  │     │  服务消费者  │     │  配置管理台  │         │
     * │   │  Provider   │     │  Consumer   │     │   Console   │         │
     * │   └──────┬──────┘     └──────┬──────┘     └──────┬──────┘         │
     * │          │                   │                   │                 │
     * │          │     注册/发现      │     获取配置       │                 │
     * │          │◄─────────────────►│◄─────────────────►│                 │
     * │          │                   │                   │                 │
     * │          └───────────────────┴───────────────────┘                 │
     * │                              │                                      │
     * │                              ▼                                      │
     * │   ┌─────────────────────────────────────────────────────────────┐  │
     * │   │                    Nacos Server                              │  │
     * │   │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │  │
     * │   │  │ Naming Service│  │ Config Service│  │  Core Module  │   │  │
     * │   │  │  (注册中心)    │  │  (配置中心)    │  │  (核心模块)    │   │  │
     * │   │  └───────────────┘  └───────────────┘  └───────────────┘   │  │
     * │   │                                                              │  │
     * │   │  ┌───────────────────────────────────────────────────────┐  │  │
     * │   │  │              存储（MySQL / 内置存储）                   │  │  │
     * │   │  └───────────────────────────────────────────────────────┘  │  │
     * │   └─────────────────────────────────────────────────────────────┘  │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    // ==================== Nacos 注册中心原理 ====================

    /**
     * 【服务注册发现流程】
     *
     * 服务注册：
     *
     *   Provider                    Nacos Server
     *      │                             │
     *      │───── 注册请求 ─────────────→│
     *      │     (IP, Port, ServiceName) │
     *      │                             │
     *      │←──── 注册成功 ─────────────│
     *      │                             │
     *      │───── 心跳 (5s) ────────────→│  ← 健康检查
     *      │                             │
     *      │───── 心跳 (5s) ────────────→│
     *      │                             │
     *
     * 服务发现：
     *
     *   Consumer                    Nacos Server
     *      │                             │
     *      │───── 订阅服务列表 ──────────→│
     *      │                             │
     *      │←──── 返回实例列表 ──────────│
     *      │     [IP1, IP2, IP3]         │
     *      │                             │
     *      │←──── 推送变更 ─────────────│  ← 实例变化时主动推送
     *      │     (UDP/Push)              │
     *
     * 【健康检查机制】
     *
     * ┌─────────────────┬─────────────────────────────────────────────┐
     * │ 模式             │ 说明                                         │
     * ├─────────────────┼─────────────────────────────────────────────┤
     * │ 临时实例         │ 客户端主动心跳，15秒无心跳标记不健康           │
     * │ (ephemeral)     │ 30秒无心跳剔除，适合微服务                    │
     * ├─────────────────┼─────────────────────────────────────────────┤
     * │ 持久实例         │ 服务端主动探测（TCP/HTTP）                    │
     * │ (persistent)    │ 适合传统应用、数据库等基础设施                │
     * └─────────────────┴─────────────────────────────────────────────┘
     *
     * 【数据一致性模型】
     *
     * Nacos 1.x：AP 模式（优先可用性）+ CP 模式（Raft 协议）
     * Nacos 2.x：
     *   - 临时实例：AP 模式（Distro 协议，最终一致性）
     *   - 持久实例：CP 模式（Raft 协议，强一致性）
     *
     * Distro 协议（AP）：
     *   - 每个节点负责一部分数据
     *   - 节点间异步同步
     *   - 读操作可从任意节点读取
     *   - 写操作路由到负责节点
     */

    // ==================== Nacos 配置中心原理 ====================

    /**
     * 【配置管理模型】
     *
     * ┌─────────────────────────────────────────────────────────────────┐
     * │                      Namespace（命名空间）                       │
     * │  ┌───────────────────────────────────────────────────────────┐  │
     * │  │                    Group（分组）                           │  │
     * │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │  │
     * │  │  │ Data ID     │  │ Data ID     │  │ Data ID     │       │  │
     * │  │  │ (配置文件)   │  │ (配置文件)   │  │ (配置文件)   │       │  │
     * │  │  │             │  │             │  │             │       │  │
     * │  │  │ application │  │ redis.yml   │  │ mysql.yml   │       │  │
     * │  │  │ .yml        │  │             │  │             │       │  │
     * │  │  └─────────────┘  └─────────────┘  └─────────────┘       │  │
     * │  └───────────────────────────────────────────────────────────┘  │
     * └─────────────────────────────────────────────────────────────────┘
     *
     * Data ID 命名规范：
     *   ${spring.application.name}-${profile}.${file-extension}
     *   例：order-service-dev.yaml
     *
     * 【配置发布流程】
     *
     *   Console                     Nacos Server                 Client
     *      │                             │                          │
     *      │───── 发布配置 ─────────────→│                          │
     *      │                             │                          │
     *      │                             │───── 推送变更 ──────────→│
     *      │                             │      (长轮询/UDP)        │
     *      │                             │                          │
     *      │                             │←───── 确认收到 ─────────│
     *      │                             │                          │
     *      │                             │         ┌───────────────┤
     *      │                             │         │ @RefreshScope │
     *      │                             │         │ Bean 重新绑定  │
     *      │                             │         └───────────────┤
     *
     * 【配置读取长轮询机制】
     *
     * Client                                    Nacos Server
     *    │                                           │
     *    │───── 长轮询请求 ─────────────────────────→│
     *    │      (Last-Modified-Time)                 │
     *    │                                           │
     *    │              ┌───── 30秒内有变化 ──────┐   │
     *    │              │ 立即返回新配置           │   │
     *    │              └────────────────────────┘   │
     *    │                                           │
     *    │              ┌───── 30秒内无变化 ──────┐   │
     *    │              │ 挂起请求，等待变更       │   │
     *    │              │ 超时后返回304           │   │
     *    │              └────────────────────────┘   │
     *    │                                           │
     *    │←───── 返回结果 ───────────────────────────│
     *    │                                           │
     *    │───── 发起新的长轮询 ─────────────────────→│
     */

    // ==================== Nacos 使用示例 ====================

    /**
     * 【Maven 依赖】
     */
    /*
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        <version>2021.0.5.0</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        <version>2021.0.5.0</version>
    </dependency>
    */

    /**
     * 【配置文件 - bootstrap.yml】
     */
    /*
    spring:
      application:
        name: order-service
      cloud:
        nacos:
          # 注册中心配置
          discovery:
            server-addr: 127.0.0.1:8848
            namespace: dev
            group: DEFAULT_GROUP
            # 服务元数据
            metadata:
              version: 1.0.0
              region: cn-hangzhou
            # 心跳配置
            heart-beat-interval: 5000
            heart-beat-timeout: 15000
            # 健康检查
            healthy-threshold: 1
            unhealthy-threshold: 3

          # 配置中心配置
          config:
            server-addr: 127.0.0.1:8848
            namespace: dev
            group: DEFAULT_GROUP
            file-extension: yaml
            # 扩展配置
            extension-configs:
              - data-id: common.yaml
                group: DEFAULT_GROUP
                refresh: true
              - data-id: redis.yaml
                group: DEFAULT_GROUP
                refresh: true
            # 共享配置
            shared-configs:
              - data-id: shared-db.yaml
                refresh: true
    */

    /**
     * 【服务注册发现使用】
     */
    /*
    @Service
    public class OrderService {

        // 方式1：使用 @LoadBalanced RestTemplate
        @Autowired
        private RestTemplate restTemplate;

        public User getUser(Long userId) {
            // 自动通过服务名发现实例
            return restTemplate.getForObject(
                "http://user-service/api/users/" + userId,
                User.class
            );
        }

        // 方式2：使用 Feign
        @Autowired
        private UserClient userClient;

        public User getUserByFeign(Long userId) {
            return userClient.getById(userId);
        }

        // 方式3：使用 DiscoveryClient 直接获取实例
        @Autowired
        private DiscoveryClient discoveryClient;

        public List<ServiceInstance> getInstances() {
            return discoveryClient.getInstances("user-service");
        }
    }
    */

    /**
     * 【动态配置使用】
     */
    /*
    @RefreshScope  // 支持配置动态刷新
    @Service
    public class ConfigService {

        // 方式1：@Value 注解
        @Value("${app.config.timeout:3000}")
        private int timeout;

        @Value("${app.config.feature.enabled:false}")
        private boolean featureEnabled;

        // 方式2：@ConfigurationProperties
        @ConfigurationProperties(prefix = "app.config")
        @Data
        public static class AppConfig {
            private int timeout;
            private boolean featureEnabled;
            private List<String> servers;
        }

        public void printConfig() {
            System.out.println("timeout: " + timeout);
            System.out.println("featureEnabled: " + featureEnabled);
        }
    }
    */

    /**
     * 【配置变更监听】
     */
    /*
    @Component
    public class ConfigChangeListener {

        @NacosConfigListener(dataId = "order-service.yaml", groupId = "DEFAULT_GROUP")
        public void onConfigChange(String newConfig) {
            System.out.println("配置变更：" + newConfig);
            // 处理配置变更逻辑
        }

        @NacosConfigListener(dataId = "order-service.yaml")
        public void onConfigChange(ConfigChangeEvent event) {
            // 获取变更的配置项
            for (ConfigChangeItem item : event.getChangeItems().values()) {
                System.out.println(item.getKey() + ": " +
                    item.getOldValue() + " -> " + item.getNewValue());
            }
        }
    }
    */

    // ==================== Apollo 概述 ====================

    /**
     * Apollo（阿波罗）—— 携程开源的配置中心
     *
     * 【设计理念】
     * 1. 配置独立于应用，解耦配置与代码
     * 2. 配置修改实时生效
     * 3. 支持多环境、多集群、多命名空间
     * 4. 提供完善的权限管理和审计功能
     *
     * 【架构设计】
     *
     * ┌─────────────────────────────────────────────────────────────────────────┐
     * │                           Apollo 架构                                    │
     * │                                                                         │
     * │   ┌──────────────┐                                                       │
     * │   │   Portal     │  ← 配置管理界面（Web控制台）                           │
     * │   │  (管理后台)   │                                                       │
     * │   └──────┬───────┘                                                       │
     * │          │                                                               │
     * │          ▼                                                               │
     * │   ┌──────────────────────────────────────────────────────────────────┐  │
     * │   │                    Config Service                                 │  │
     * │   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │  │
     * │   │  │ Config DB   │  │ Release Mgr │  │ Config Mgr  │              │  │
     * │   │  │ (配置存储)   │  │ (发布管理)   │  │ (配置管理)   │              │  │
     * │   │  └─────────────┘  └─────────────┘  └─────────────┘              │  │
     * │   └──────────────────────────────────────────────────────────────────┘  │
     * │          │                                                               │
     * │          ▼                                                               │
     * │   ┌──────────────────────────────────────────────────────────────────┐  │
     * │   │                    Admin Service                                  │  │
     * │   │  ┌─────────────┐  ┌─────────────┐                                │  │
     * │   │  │ 项目管理     │  │ 权限管理     │                                │  │
     * │   │  └─────────────┘  └─────────────┘                                │  │
     * │   └──────────────────────────────────────────────────────────────────┘  │
     * │          │                                                               │
     * │          ▼                                                               │
     * │   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐          │
     * │   │  App Client  │     │  App Client  │     │  App Client  │          │
     * │   │  (应用客户端)  │     │  (应用客户端)  │     │  (应用客户端)  │          │
     * │   └──────────────┘     └──────────────┘     └──────────────┘          │
     * └─────────────────────────────────────────────────────────────────────────┘
     *
     * 【核心概念】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 概念             │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ AppId           │ 应用唯一标识                                          │
     * │ Environment     │ 环境（DEV/FAT/UAT/PRO）                               │
     * │ Cluster         │ 集群（支持多数据中心）                                 │
     * │ Namespace       │ 命名空间（配置文件），如 application, application.yml   │
     * │ Item            │ 配置项（KV对）                                        │
     * │ Release         │ 配置发布版本                                          │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     */

    // ==================== Apollo 配置模型 ====================

    /**
     * 【命名空间类型】
     *
     * ┌─────────────────┬─────────────────────────────────────────────────────┐
     * │ 类型             │ 说明                                                 │
     * ├─────────────────┼─────────────────────────────────────────────────────┤
     * │ 私有 Namespace   │ 只有当前应用可使用                                    │
     * │ 公共 Namespace   │ 多应用共享，如公共数据库配置                           │
     * │ 关联 Namespace   │ 继承公共 Namespace，可覆盖配置                         │
     * └─────────────────┴─────────────────────────────────────────────────────┘
     *
     * 【配置格式】
     *
     * application（properties 格式）:
     *   server.port=8080
     *   spring.datasource.url=jdbc:mysql://localhost:3306/mydb
     *
     * application.yml（YAML 格式）:
     *   server:
     *     port: 8080
     *   spring:
     *     datasource:
     *       url: jdbc:mysql://localhost:3306/mydb
     *
     * application.json（JSON 格式）:
     *   {"server.port":8080,"spring.datasource.url":"jdbc:mysql://..."}
     *
     * 【配置加载优先级】
     *
     *   Portal 发布配置
     *        ↓
     *   Config Service 存储
     *        ↓
     *   Client 长轮询获取
     *        ↓
     *   本地缓存 fallback（默认位置：/opt/data/{appId}/config-cache/）
     *        ↓
     *   应用默认配置
     *
     * 【配置继承示例】
     *
     *   公共 Namespace: common-db.properties
     *     ├─ db.url=jdbc:mysql://master:3306/db
     *     └─ db.username=root
     *
     *   应用关联并覆盖: order-service.properties
     *     └─ db.url=jdbc:mysql://slave:3306/order_db  (覆盖)
     *
     *   最终生效配置:
     *     ├─ db.url=jdbc:mysql://slave:3306/order_db  (应用覆盖)
     *     └─ db.username=root                          (继承公共)
     */

    // ==================== Apollo 使用示例 ====================

    /**
     * 【Maven 依赖】
     */
    /*
    <dependency>
        <groupId>com.ctrip.framework.apollo</groupId>
        <artifactId>apollo-client</artifactId>
        <version>2.1.0</version>
    </dependency>
    */

    /**
     * 【配置文件 - application.properties】
     */
    /*
    # Apollo 应用ID
    app.id=order-service
    # Apollo Meta Server 地址
    apollo.meta=http://127.0.0.1:8080
    # 启用的命名空间
    apollo.bootstrap.enabled=true
    apollo.bootstrap.namespaces=application,common-db
    # 集群名称
    apollo.cluster=default
    # 本地缓存路径
    apollo.cacheDir=/opt/data/apollo/cache
    */

    /**
     * 【基本使用】
     */
    /*
    @Service
    public class ApolloConfigService {

        // 方式1：@Value 注解（自动刷新）
        @Value("${config.timeout:3000}")
        private int timeout;

        // 方式2：注入 Config 对象
        @Autowired
        private Config config;

        public void getConfig() {
            String value = config.getProperty("key", "default");
            int timeout = config.getIntProperty("timeout", 3000);
        }

        // 方式3：@ConfigurationProperties
        @ConfigurationProperties(prefix = "config")
        @Data
        public static class MyConfig {
            private int timeout;
            private String feature;
        }
    }
    */

    /**
     * 【配置变更监听】
     */
    /*
    @Component
    public class ApolloConfigChangeListener {

        @ApolloConfigChangeListener
        public void onChange(ConfigChangeEvent changeEvent) {
            // 遍历变更的配置项
            for (String key : changeEvent.changedKeys()) {
                ConfigChange change = changeEvent.getChange(key);

                System.out.println(String.format(
                    "配置变更: %s, 旧值: %s, 新值: %s, 变更类型: %s",
                    key,
                    change.getOldValue(),
                    change.getNewValue(),
                    change.getChangeType()
                ));
            }

            // 特定配置项处理
            if (changeEvent.isChanged("config.timeout")) {
                ConfigChange change = changeEvent.getChange("config.timeout");
                // 更新内存中的配置
                updateTimeout(Integer.parseInt(change.getNewValue()));
            }
        }

        // 监听特定 Namespace
        @ApolloConfigChangeListener("application")
        public void onApplicationChange(ConfigChangeEvent changeEvent) {
            // 处理 application namespace 的变更
        }
    }
    */

    /**
     * 【@RefreshScope 实现配置刷新】
     */
    /*
    @RefreshScope
    @Service
    public class RefreshableService {

        @Value("${dynamic.config.value:default}")
        private String configValue;

        // 配置变更时，Bean 会重新创建
        public String getConfigValue() {
            return configValue;
        }
    }
    */

    /**
     * 【灰度发布】
     *
     * Apollo 支持配置的灰度发布：
     *
     * 1. 创建灰度发布规则
     *    - 按 IP 列表灰度
     *    - 按应用实例灰度
     *
     * 2. 灰度流程
     *
     *    ┌─────────────┐
     *    │  修改配置   │
     *    └──────┬──────┘
     *           │
     *           ▼
     *    ┌─────────────┐     ┌─────────────┐
     *    │  创建灰度   │────→│  选择灰度IP  │
     *    └──────┬──────┘     └─────────────┘
     *           │
     *           ▼
     *    ┌─────────────┐
     *    │  灰度发布   │  ← 仅灰度IP收到新配置
     *    └──────┬──────┘
     *           │
     *           ▼
     *    ┌─────────────┐
     *    │  全量发布   │  ← 所有实例收到新配置
     *    └─────────────┘
     */

    // ==================== Nacos vs Apollo 对比 ====================

    /**
     * ┌─────────────────────┬─────────────────────────┬─────────────────────────┐
     * │ 对比项               │ Nacos                    │ Apollo                   │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 定位                 │ 注册中心 + 配置中心       │ 专业配置中心              │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 服务发现             │ ✅ 支持                   │ ❌ 不支持                │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 配置管理             │ ✅ 支持                   │ ✅ 支持（功能更强）        │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 灰度发布             │ ❌ 不支持                 │ ✅ 支持                  │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 配置回滚             │ ✅ 支持                   │ ✅ 支持                  │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 权限管理             │ 较弱                     │ ✅ 完善                  │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 审计日志             │ 基础                     │ ✅ 完善                  │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 配置格式             │ properties/yaml/json     │ properties/yaml/json/xml │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 一致性模型           │ AP + CP 可选              │ AP                       │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 部署复杂度           │ 简单                     │ 较复杂（多模块）          │
     * ├─────────────────────┼─────────────────────────┼─────────────────────────┤
     * │ 社区活跃度           │ 高（阿里维护）             │ 中（携程维护）            │
     * └─────────────────────┴─────────────────────────┴─────────────────────────┘
     *
     * 【选型建议】
     *
     * 选择 Nacos：
     * - 需要同时使用注册中心和配置中心
     * - 希望部署简单，运维成本低
     * - 使用 Spring Cloud Alibaba 体系
     * - 对灰度发布、权限管理要求不高
     *
     * 选择 Apollo：
     * - 需要专业的配置中心（已有 Eureka/Nacos 做注册中心）
     * - 需要灰度发布能力
     * - 需要完善的权限管理和审计
     * - 配置管理需求复杂（多环境、多集群、多命名空间）
     *
     * 组合使用：
     * - Nacos 做注册中心 + Apollo 做配置中心
     * - 各取所长
     */

    // ==================== 最佳实践 ====================

    /**
     * 【配置分类建议】
     *
     * 1. 按环境隔离
     *    - DEV: 开发环境
     *    - FAT: 功能验收测试环境
     *    - UAT: 用户验收测试环境
     *    - PRO: 生产环境
     *
     * 2. 按应用隔离
     *    - 每个微服务有自己的配置
     *    - 公共配置抽取到公共 Namespace
     *
     * 3. 按模块隔离
     *    - application.yml: 应用主配置
     *    - db.yml: 数据库配置
     *    - redis.yml: Redis配置
     *    - mq.yml: 消息队列配置
     *
     * 【敏感配置处理】
     *
     * 1. 加密存储
     *    - 数据库密码、API密钥等
     *    - Nacos: 支持配置加密插件
     *    - Apollo: 支持配置加密
     *
     * 2. 权限控制
     *    - 生产环境配置只允许特定人员修改
     *    - 敏感配置对开发人员只读
     *
     * 3. 审计日志
     *    - 记录所有配置变更
     *    - 追踪问题来源
     *
     * 【配置变更规范】
     *
     * 1. 变更前：通知相关人员
     * 2. 变更中：灰度发布（Apollo）
     * 3. 变更后：验证配置生效
     * 4. 异常时：快速回滚
     */
}
