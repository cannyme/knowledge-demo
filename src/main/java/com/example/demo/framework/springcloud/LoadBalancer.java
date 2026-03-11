package com.example.demo.framework.springcloud;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡策略
 *
 * 【核心作用】
 * 将请求分发到多个服务实例，提高系统吞吐量和可用性。
 *
 * 【负载均衡分类】
 * 1. 服务端负载均衡：Nginx、F5（集中式）
 * 2. 客户端负载均衡：Ribbon、LoadBalancer（进程内）
 *
 * 【主流实现】
 * - Ribbon：Spring Cloud Netflix（已进入维护模式）
 * - Spring Cloud LoadBalancer：官方推荐替代方案
 */
public class LoadBalancer {

    // ==================== 负载均衡算法 ====================

    /**
     * 服务实例
     */
    static class ServiceInstance {
        private final String host;
        private final int port;
        private final int weight;

        public ServiceInstance(String host, int port, int weight) {
            this.host = host;
            this.port = port;
            this.weight = weight;
        }

        public String getHost() { return host; }
        public int getPort() { return port; }
        public int getWeight() { return weight; }

        @Override
        public String toString() {
            return host + ":" + port + "(weight=" + weight + ")";
        }
    }

    /**
     * 1. 轮询（Round Robin）
     *
     * 按顺序依次分发请求
     * 优点：简单公平
     * 缺点：不考虑服务器性能差异
     */
    static class RoundRobinLoadBalancer {
        private final List<ServiceInstance> instances;
        private final AtomicInteger position = new AtomicInteger(0);

        public RoundRobinLoadBalancer(List<ServiceInstance> instances) {
            this.instances = instances;
        }

        public ServiceInstance choose() {
            if (instances.isEmpty()) return null;
            int pos = Math.abs(position.getAndIncrement() % instances.size());
            return instances.get(pos);
        }
    }

    /**
     * 2. 加权轮询（Weighted Round Robin）
     *
     * 按权重比例分发请求
     * 权重高的服务器获得更多请求
     */
    static class WeightedRoundRobinLoadBalancer {
        private final List<ServiceInstance> instances;
        private final AtomicInteger position = new AtomicInteger(0);

        // 展开的实例列表（按权重展开）
        private List<ServiceInstance> expandedList;

        public WeightedRoundRobinLoadBalancer(List<ServiceInstance> instances) {
            this.instances = instances;
            this.expandedList = expandInstances();
        }

        private List<ServiceInstance> expandInstances() {
            List<ServiceInstance> expanded = new ArrayList<>();
            for (ServiceInstance instance : instances) {
                for (int i = 0; i < instance.getWeight(); i++) {
                    expanded.add(instance);
                }
            }
            return expanded;
        }

        public ServiceInstance choose() {
            if (expandedList.isEmpty()) return null;
            int pos = Math.abs(position.getAndIncrement() % expandedList.size());
            return expandedList.get(pos);
        }
    }

    /**
     * 3. 平滑加权轮询（Smooth Weighted Round Robin）
     *
     * 解决加权轮询的请求分布不均问题
     * Nginx默认使用的算法
     *
     * 原理：
     * - 每个实例维护currentWeight，初始为0
     * - 选择时，currentWeight += weight
     * - 选择currentWeight最大的实例
     * - 被选中的实例currentWeight -= totalWeight
     */
    static class SmoothWeightedRoundRobinLoadBalancer {
        private final List<ServiceInstance> instances;
        private final Map<ServiceInstance, Integer> currentWeights = new ConcurrentHashMap<>();

        public SmoothWeightedRoundRobinLoadBalancer(List<ServiceInstance> instances) {
            this.instances = instances;
            for (ServiceInstance instance : instances) {
                currentWeights.put(instance, 0);
            }
        }

        public ServiceInstance choose() {
            if (instances.isEmpty()) return null;

            int totalWeight = instances.stream().mapToInt(ServiceInstance::getWeight).sum();
            ServiceInstance selected = null;
            int maxWeight = Integer.MIN_VALUE;

            // currentWeight += weight，找出最大的
            for (ServiceInstance instance : instances) {
                int weight = instance.getWeight();
                int currentWeight = currentWeights.get(instance) + weight;
                currentWeights.put(instance, currentWeight);

                if (currentWeight > maxWeight) {
                    maxWeight = currentWeight;
                    selected = instance;
                }
            }

            // 被选中的实例currentWeight -= totalWeight
            currentWeights.put(selected, currentWeights.get(selected) - totalWeight);

            return selected;
        }
    }

    /**
     * 4. 随机（Random）
     */
    static class RandomLoadBalancer {
        private final List<ServiceInstance> instances;
        private final Random random = new Random();

        public RandomLoadBalancer(List<ServiceInstance> instances) {
            this.instances = instances;
        }

        public ServiceInstance choose() {
            if (instances.isEmpty()) return null;
            int pos = random.nextInt(instances.size());
            return instances.get(pos);
        }
    }

    /**
     * 5. 加权随机（Weighted Random）
     */
    static class WeightedRandomLoadBalancer {
        private final List<ServiceInstance> instances;
        private final Random random = new Random();
        private final int totalWeight;
        private final List<Integer> weights;

        public WeightedRandomLoadBalancer(List<ServiceInstance> instances) {
            this.instances = instances;
            this.weights = new ArrayList<>();
            int sum = 0;
            for (ServiceInstance instance : instances) {
                sum += instance.getWeight();
                weights.add(sum);
            }
            this.totalWeight = sum;
        }

        public ServiceInstance choose() {
            if (instances.isEmpty()) return null;

            int randomWeight = random.nextInt(totalWeight);
            for (int i = 0; i < weights.size(); i++) {
                if (randomWeight < weights.get(i)) {
                    return instances.get(i);
                }
            }
            return instances.get(0);
        }
    }

    /**
     * 6. 最小连接数（Least Connections）
     *
     * 选择当前连接数最少的服务器
     * 适合长连接场景
     */
    static class LeastConnectionsLoadBalancer {
        private final Map<ServiceInstance, AtomicInteger> connections = new ConcurrentHashMap<>();

        public ServiceInstance choose(List<ServiceInstance> instances) {
            if (instances.isEmpty()) return null;

            ServiceInstance selected = null;
            int minConnections = Integer.MAX_VALUE;

            for (ServiceInstance instance : instances) {
                int conn = connections.computeIfAbsent(instance, k -> new AtomicInteger(0)).get();
                if (conn < minConnections) {
                    minConnections = conn;
                    selected = instance;
                }
            }

            connections.get(selected).incrementAndGet();
            return selected;
        }

        public void release(ServiceInstance instance) {
            connections.get(instance).decrementAndGet();
        }
    }

    /**
     * 7. 一致性哈希（Consistent Hash）
     *
     * 相同请求总是路由到同一服务器
     * 适合有状态的场景（缓存、会话）
     */
    static class ConsistentHashLoadBalancer {
        // 虚拟节点数（解决数据倾斜）
        private static final int VIRTUAL_NODES = 150;
        private final TreeMap<Integer, ServiceInstance> ring = new TreeMap<>();

        public ConsistentHashLoadBalancer(List<ServiceInstance> instances) {
            for (ServiceInstance instance : instances) {
                for (int i = 0; i < VIRTUAL_NODES; i++) {
                    String virtualNode = instance.getHost() + ":" + instance.getPort() + "#" + i;
                    int hash = hash(virtualNode);
                    ring.put(hash, instance);
                }
            }
        }

        public ServiceInstance choose(String key) {
            if (ring.isEmpty()) return null;

            int hash = hash(key);
            // 找到第一个大于等于该hash的节点
            Map.Entry<Integer, ServiceInstance> entry = ring.ceilingEntry(hash);
            if (entry == null) {
                // 环形结构，回到第一个节点
                entry = ring.firstEntry();
            }
            return entry.getValue();
        }

        private int hash(String key) {
            // 使用MurmurHash等更好的哈希算法
            return key.hashCode() & Integer.MAX_VALUE;
        }
    }

    // ==================== 算法对比 ====================

    /**
     * ┌──────────────────┬─────────────────┬─────────────────────────┐
     * │ 算法              │ 适用场景         │ 特点                     │
     * ├──────────────────┼─────────────────┼─────────────────────────┤
     * │ 轮询              │ 服务器性能相近   │ 简单公平，不考虑差异       │
     * │ 加权轮询          │ 服务器性能不同   │ 考虑权重，但分布可能不均   │
     * │ 平滑加权轮询      │ 服务器性能不同   │ 分布均匀，推荐使用        │
     * │ 随机              │ 简单场景        │ 简单，大数据量下均匀      │
     * │ 最小连接数        │ 长连接场景      │ 动态感知，效果好          │
     * │ 一致性哈希        │ 有状态场景      │ 相同请求同一服务器        │
     * └──────────────────┴─────────────────┴─────────────────────────┘
     */

    // ==================== Ribbon使用示例 ====================

    /**
     * Ribbon配置示例
     *
     * # 配置文件
     * user-service:
     *   ribbon:
     *     NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
     *
     * 自定义规则：
     */
    /*
    @Configuration
    public class RibbonConfig {

        @Bean
        public IRule ribbonRule() {
            // 轮询
            // return new RoundRobinRule();

            // 随机
            // return new RandomRule();

            // 加权响应时间
            // return new WeightedResponseTimeRule();

            // 自定义：基于元数据的灰度路由
            return new MetadataAwareRule();
        }
    }
    */

    /**
     * 灰度路由规则
     */
    /*
    public class MetadataAwareRule extends PredicateBasedRule {

        @Override
        public Server choose(Object key) {
            // 从请求上下文获取灰度标识
            String version = GrayContextHolder.getVersion();

            List<Server> servers = getLoadBalancer().getAllServers();

            // 筛选匹配版本的服务
            List<Server> matched = servers.stream()
                .filter(server -> {
                    Map<String, String> metadata = ((NacosServer) server).getMetadata();
                    return version.equals(metadata.get("version"));
                })
                .collect(Collectors.toList());

            if (matched.isEmpty()) {
                // 降级到默认版本
                matched = servers;
            }

            // 随机选择
            return matched.get(ThreadLocalRandom.current().nextInt(matched.size()));
        }
    }
    */

    // ==================== Spring Cloud LoadBalancer ====================

    /**
     * Spring Cloud LoadBalancer是官方推荐的替代方案
     *
     * 配置：
     * spring:
     *   cloud:
     *     loadbalancer:
     *       ribbon:
     *         enabled: false  # 禁用Ribbon
     *
     * 自定义负载均衡策略：
     */
    /*
    @Configuration
    public class LoadBalancerConfig {

        @Bean
        ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
                Environment environment, LoadBalancerClientFactory factory) {
            String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
            return new RandomLoadBalancer(
                factory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                serviceId);
        }
    }
    */

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // 准备服务实例
        List<ServiceInstance> instances = Arrays.asList(
            new ServiceInstance("192.168.1.1", 8080, 3),
            new ServiceInstance("192.168.1.2", 8080, 2),
            new ServiceInstance("192.168.1.3", 8080, 1)
        );

        System.out.println("=== 轮询 ===");
        RoundRobinLoadBalancer roundRobin = new RoundRobinLoadBalancer(instances);
        for (int i = 0; i < 6; i++) {
            System.out.println("请求" + (i+1) + " → " + roundRobin.choose());
        }

        System.out.println("\n=== 平滑加权轮询 ===");
        SmoothWeightedRoundRobinLoadBalancer smooth = new SmoothWeightedRoundRobinLoadBalancer(instances);
        for (int i = 0; i < 6; i++) {
            System.out.println("请求" + (i+1) + " → " + smooth.choose());
        }

        System.out.println("\n=== 一致性哈希 ===");
        ConsistentHashLoadBalancer hash = new ConsistentHashLoadBalancer(instances);
        String[] keys = {"user:1", "user:2", "user:3", "user:1"};
        for (String key : keys) {
            System.out.println("Key: " + key + " → " + hash.choose(key));
        }
    }
}
