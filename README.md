# Java 后端知识复习 Demo

Java 后端开发核心知识点示例代码，涵盖 Java 基础、设计模式、框架、数据库、系统架构等领域。

## 项目特点

- **50 个知识点文件**，每个文件专注于一个核心知识点
- **详细中文注释**，配合 ASCII 图解便于理解
- **可运行的示例代码**，大部分文件包含 main 方法
- **面试题汇总**，每文件末尾包含常见面试问题

## 知识体系

### Java 基础

| 主题 | 文件 | 核心内容 |
|------|------|----------|
| 设计模式 | `javabase/designpatterns/` | 单例、工厂、策略、观察者、装饰器、代理、模板方法、适配器、责任链、建造者 |
| 并发编程 | `javabase/concurrency/` | 线程安全、锁机制、线程池 |
| 集合框架 | `javabase/collections/` | List、Map、Set 原理与使用 |
| JVM | `javabase/jvm/` | 内存模型、类加载机制、GC |
| Java 8+ | `javabase/java8/` | Lambda、Stream、Optional、CompletableFuture |

### 框架

| 主题 | 文件 | 核心内容 |
|------|------|----------|
| Spring | `framework/spring/` | IoC/DI、AOP、事务管理、Spring Security |
| Spring Boot | `framework/springboot/` | 自动装配原理 |
| Spring Cloud | `framework/springcloud/` | 服务发现、负载均衡、熔断限流、API 网关、Feign、Nacos、Apollo、Sentinel、Dubbo |
| MyBatis | `framework/mybatis/` | 核心概念、缓存、插件 |

### 数据库

| 主题 | 文件 | 核心内容 |
|------|------|----------|
| MySQL | `database/mysql/` | 索引优化、MVCC、事务隔离、分库分表、连接池 |
| Redis | `database/redis/` | 数据结构、持久化、高可用 |
| 分布式 | `database/distributed/` | 分布式锁、分布式事务 |
| 消息队列 | `database/mq/` | RocketMQ、Kafka、消息可靠性 |

### 计算机基础

| 主题 | 文件 | 核心内容 |
|------|------|----------|
| 算法 | `csbasics/algorithm/` | 排序算法实现与对比 |
| 数据结构 | `csbasics/datastructure/` | 链表、二叉树、B+树、跳表、HashMap 源码 |
| 网络 | `csbasics/network/` | TCP/HTTP 协议、Netty、NIO |

### 系统架构

| 主题 | 文件 | 核心内容 |
|------|------|----------|
| 系统设计 | `architecture/SystemDesign.java` | 缓存穿透/击穿/雪崩、分布式 ID、秒杀系统 |
| 链路追踪 | `architecture/DistributedTracing.java` | TraceId 传递、SkyWalking、Zipkin |
| 容器化 | `architecture/DockerKubernetes.java` | Docker、Kubernetes 核心概念 |

## 快速开始

### 环境要求

- JDK 8+
- Maven 3.6+

### 编译运行

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包
mvn package
```

### 运行示例

每个知识点文件都包含可运行的示例代码，例如：

```bash
# 运行某个类的 main 方法（IDE 中直接运行）
# 例如：com.example.demo.javabase.designpatterns.SingletonPattern
```

## 目录结构

```
src/main/java/com/example/demo/
├── javabase/              # Java 基础
│   ├── designpatterns/    # 设计模式
│   ├── concurrency/       # 并发编程
│   ├── collections/       # 集合框架
│   ├── jvm/              # JVM
│   └── java8/            # Java 8+ 特性
│
├── framework/            # 框架
│   ├── spring/           # Spring 核心
│   ├── springboot/       # Spring Boot
│   ├── springcloud/      # Spring Cloud
│   └── mybatis/          # MyBatis
│
├── database/             # 数据库
│   ├── mysql/            # MySQL
│   ├── redis/            # Redis
│   ├── distributed/      # 分布式
│   └── mq/               # 消息队列
│
├── csbasics/             # 计算机基础
│   ├── algorithm/        # 算法
│   ├── datastructure/    # 数据结构
│   └── network/          # 网络
│
└── architecture/         # 系统架构
```

## 学习建议

1. **入门阶段**：Java 基础 → 设计模式 → 集合框架
2. **进阶阶段**：Spring 框架 → MyBatis → 并发编程
3. **高级阶段**：数据库优化 → 分布式系统 → 架构设计

## 适用场景

- 面试复习
- 知识查漏补缺
- 技术分享
- 代码参考

## 许可证

MIT License
