package com.example.demo.framework.springcloud;

/**
 * API网关
 *
 * 【核心作用】
 * 1. 路由转发：请求转发到后端服务
 * 2. 过滤器：认证、限流、日志等
 * 3. 负载均衡：选择后端实例
 * 4. 协议转换：HTTP转RPC等
 *
 * 【主流实现】
 * - Spring Cloud Gateway：Spring官方，基于WebFlux
 * - Netflix Zuul：Netflix开源，基于Servlet
 * - Kong：基于Nginx，高性能
 * - Nginx：反向代理，配置简单
 */
public class ApiGateway {

    // ==================== 网关架构 ====================

    /**
     * 网关在微服务架构中的位置：
     *
     *        ┌─────────────┐
     *        │   客户端     │
     *        └──────┬──────┘
     *               │
     *               ▼
     *        ┌─────────────┐
     *        │   API网关   │ ← 统一入口
     *        │  Gateway    │
     *        └──────┬──────┘
     *               │
     *       ┌───────┼───────┐
     *       │       │       │
     *       ▼       ▼       ▼
     *   ┌───────┐ ┌───────┐ ┌───────┐
     *   │用户服务│ │订单服务│ │商品服务│
     *   └───────┘ └───────┘ └───────┘
     *
     * 网关职责：
     * ┌─────────────────────────────────────────────────────┐
     * │ 请求处理流程                                          │
     * ├─────────────────────────────────────────────────────┤
     * │ 1. 请求接收                                           │
     * │ 2. 认证鉴权                                           │
     * │ 3. 限流熔断                                           │
     * │ 4. 请求路由                                           │
     * │ 5. 负载均衡                                           │
     * │ 6. 协议转换                                           │
     * │ 7. 响应处理                                           │
     * │ 8. 日志记录                                           │
     * └─────────────────────────────────────────────────────┘
     */

    // ==================== Spring Cloud Gateway配置 ====================

    /**
     * 路由配置示例
     *
     * spring:
     *   cloud:
     *     gateway:
     *       routes:
     *         # 用户服务路由
     *         - id: user-service
     *           uri: lb://user-service
     *           predicates:
     *             - Path=/api/user/**
     *           filters:
     *             - StripPrefix=1
     *             - name: RequestRateLimiter
     *               args:
     *                 redis-rate-limiter.replenishRate: 100
     *                 redis-rate-limiter.burstCapacity: 200
     *
     *         # 订单服务路由
     *         - id: order-service
     *           uri: lb://order-service
     *           predicates:
     *             - Path=/api/order/**
     *             - Method=GET,POST
     *           filters:
     *             - AuthFilter
     *
     *       # 全局跨域配置
     *       globalcors:
     *         cors-configurations:
     *           '[/**]':
     *             allowedOrigins: "*"
     *             allowedMethods: "*"
     *             allowedHeaders: "*"
     */

    // ==================== 路由断言工厂 ====================

    /**
     * 内置断言工厂：
     *
     * ┌──────────────────────┬─────────────────────────────────────────┐
     * │ 断言                  │ 说明                                     │
     * ├──────────────────────┼─────────────────────────────────────────┤
     * │ Path                 │ 路径匹配                                 │
     * │ Method               │ 请求方法匹配                             │
     * │ Header               │ 请求头匹配                               │
     * │ Query                │ 查询参数匹配                             │
     * │ Cookie               │ Cookie匹配                              │
     * │ Host                 │ 主机名匹配                               │
     * │ After/Before/Between │ 时间匹配                                 │
     * │ RemoteAddr           │ IP地址匹配                               │
     * │ Weight               │ 权重路由                                 │
     * └──────────────────────┴─────────────────────────────────────────┘
     *
     * 示例：
     * # 路径匹配
     * - Path=/api/user/**
     *
     * # 方法匹配
     * - Method=GET,POST
     *
     * # 请求头匹配
     * - Header=X-Request-Id, \d+
     *
     * # 查询参数匹配
     * - Query=token
     *
     * # 时间匹配（维护模式）
     * - After=2024-01-01T00:00:00+08:00[Asia/Shanghai]
     *
     * # 权重路由（灰度发布）
     * - Weight=group1, 80
     */

    // ==================== 过滤器工厂 ====================

    /**
     * 内置过滤器工厂：
     *
     * ┌──────────────────────┬─────────────────────────────────────────┐
     * │ 过滤器                │ 说明                                     │
     * ├──────────────────────┼─────────────────────────────────────────┤
     * │ AddRequestHeader     │ 添加请求头                               │
     * │ AddRequestParameter  │ 添加请求参数                             │
     * │ AddResponseHeader    │ 添加响应头                               │
     * │ StripPrefix          │ 去除路径前缀                             │
     * │ PrefixPath           │ 添加路径前缀                             │
     * │ RewritePath          │ 重写路径                                 │
     * │ SetPath              │ 设置路径                                 │
     * │ RequestRateLimiter   │ 限流                                     │
     * │ Hystrix              │ 熔断（已废弃）                           │
     * │ CircuitBreaker       │ 熔断（Resilience4j）                     │
     * │ Retry                │ 重试                                     │
     * │ RequestSize          │ 请求大小限制                             │
     * └──────────────────────┴─────────────────────────────────────────┘
     *
     * 示例：
     * # 添加请求头
     * - AddRequestHeader=X-Request-Foo, Bar
     *
     * # 去除路径前缀（/api/user/1 → /user/1）
     * - StripPrefix=1
     *
     * # 重写路径
     * - RewritePath=/api/(?<segment>.*), /$\{segment}
     *
     * # 限流
     * - name: RequestRateLimiter
     *   args:
     *     key-resolver: "#{@userKeyResolver}"
     *     redis-rate-limiter.replenishRate: 10
     *     redis-rate-limiter.burstCapacity: 20
     */

    // ==================== 自定义过滤器 ====================

    /**
     * 全局过滤器示例
     */
    /*
    @Component
    public class AuthGlobalFilter implements GlobalFilter, Ordered {

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            // 1. 获取请求路径
            String path = exchange.getRequest().getPath().value();

            // 2. 白名单放行
            if (isWhiteList(path)) {
                return chain.filter(exchange);
            }

            // 3. 获取Token
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (StringUtils.isEmpty(token)) {
                return unauthorized(exchange);
            }

            // 4. 验证Token
            try {
                Claims claims = JwtUtil.parseToken(token);
                // 将用户信息放入请求头
                ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.get("userId", String.class))
                    .header("X-User-Name", claims.get("userName", String.class))
                    .build();

                return chain.filter(exchange.mutate().request(request).build());
            } catch (Exception e) {
                return unauthorized(exchange);
            }
        }

        private boolean isWhiteList(String path) {
            return path.startsWith("/auth/")
                || path.startsWith("/public/");
        }

        private Mono<Void> unauthorized(ServerWebExchange exchange) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"code\":401,\"message\":\"未授权\"}";
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
            return response.writeWith(Mono.just(buffer));
        }

        @Override
        public int getOrder() {
            return -100; // 优先级高
        }
    }
    */

    /**
     * 局部过滤器示例
     */
    /*
    @Component
    public class TimeLogGatewayFilterFactory
            extends AbstractGatewayFilterFactory<TimeLogGatewayFilterFactory.Config> {

        public TimeLogGatewayFilterFactory() {
            super(Config.class);
        }

        @Override
        public GatewayFilter apply(Config config) {
            return (exchange, chain) -> {
                long startTime = System.currentTimeMillis();

                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    ServerHttpResponse response = exchange.getResponse();
                    response.getHeaders().add("X-Response-Time", duration + "ms");

                    if (config.isLogEnabled()) {
                        log.info("请求耗时: {}ms", duration);
                    }
                }));
            };
        }

        public static class Config {
            private boolean logEnabled = true;
            // getter/setter
        }
    }
    */

    // ==================== 限流配置 ====================

    /**
     * 基于Redis的限流
     *
     * 配置：
     * spring:
     *   redis:
     *     host: localhost
     *     port: 6379
     *
     * 自定义KeyResolver：
     */
    /*
    @Bean
    public KeyResolver ipKeyResolver() {
        // 基于IP限流
        return exchange -> Mono.just(
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    @Bean
    public KeyResolver userKeyResolver() {
        // 基于用户限流
        return exchange -> Mono.just(
            exchange.getRequest().getHeaders().getFirst("X-User-Id")
        );
    }

    @Bean
    public KeyResolver apiKeyResolver() {
        // 基于API路径限流
        return exchange -> Mono.just(
            exchange.getRequest().getPath().value()
        );
    }
    */

    // ==================== 熔断降级 ====================

    /**
     * 基于Resilience4j的熔断配置
     */
    /*
    spring:
     cloud:
       gateway:
         routes:
           - id: user-service
             uri: lb://user-service
             predicates:
               - Path=/api/user/**
             filters:
               - name: CircuitBreaker
                 args:
                   name: userServiceCircuitBreaker
                   fallbackUri: forward:/fallback/user

    # Resilience4j配置
    resilience4j:
      circuitbreaker:
        configs:
          default:
            slidingWindowSize: 10
            failureRateThreshold: 50
            waitDurationInOpenState: 10s
        instances:
          userServiceCircuitBreaker:
            baseConfig: default
    */

    /**
     * 降级处理
     */
    /*
    @RestController
    @RequestMapping("/fallback")
    public class FallbackController {

        @GetMapping("/user")
        public Result<?> userFallback() {
            return Result.fail("用户服务暂时不可用，请稍后重试");
        }

        @GetMapping("/order")
        public Result<?> orderFallback() {
            return Result.fail("订单服务暂时不可用，请稍后重试");
        }
    }
    */

    // ==================== 跨域配置 ====================

    /**
     * 全局跨域配置
     */
    /*
    @Configuration
    public class CorsConfig {

        @Bean
        public CorsWebFilter corsWebFilter() {
            CorsConfiguration config = new CorsConfiguration();
            config.addAllowedOriginPattern("*");
            config.addAllowedMethod("*");
            config.addAllowedHeader("*");
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);

            return new CorsWebFilter(source);
        }
    }
    */

    // ==================== 网关安全 ====================

    /**
     * 网关安全最佳实践：
     *
     * 1. 认证鉴权
     *    - JWT Token验证
     *    - OAuth2.0集成
     *    - 白名单机制
     *
     * 2. 限流熔断
     *    - 全局限流
     *    - 接口级别限流
     *    - 熔断降级
     *
     * 3. 安全防护
     *    - SQL注入过滤
     *    - XSS过滤
     *    - 请求大小限制
     *    - IP黑白名单
     *
     * 4. 日志审计
     *    - 请求日志
     *    - 响应日志
     *    - 异常日志
     */

    // ==================== 性能优化 ====================

    /**
     * 网关性能优化建议：
     *
     * 1. 连接池优化
     *    spring:
     *      cloud:
     *        gateway:
     *          httpclient:
     *            pool:
     *              type: elastic
     *              max-connections: 500
     *              acquire-timeout: 1000
     *
     * 2. 超时配置
     *    spring:
     *      cloud:
     *        gateway:
     *          httpclient:
     *            connect-timeout: 3000
     *            response-timeout: 10s
     *
     * 3. 缓存
     *    - 开启响应缓存
     *    - 配置缓存策略
     *
     * 4. 异步处理
     *    - 使用WebFlux异步非阻塞
     *    - 避免阻塞操作
     */
}
