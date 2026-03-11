package com.example.demo.framework.springcloud;

/**
 * 声明式服务调用 (Feign/OpenFeign)
 *
 * 【核心概念】
 * Feign是声明式HTTP客户端，通过注解定义接口，自动生成实现类。
 * 类似于MyBatis的Mapper接口方式，让调用远程服务像调用本地方法一样简单。
 *
 * 【Feign vs RestTemplate】
 * RestTemplate：手动拼接URL，代码冗余
 * Feign：声明式接口，简洁优雅
 *
 * 【核心组件】
 * 1. @FeignClient：声明远程服务接口
 * 2. Encoder：请求参数编码
 * 3. Decoder：响应结果解码
 * 4. Contract：注解解析
 * 5. Client：HTTP客户端
 */
public class FeignClient {

    // ==================== 基本使用 ====================

    /**
     * 启用Feign
     */
    /*
    @EnableFeignClients
    @SpringBootApplication
    public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    */

    /**
     * 定义Feign客户端
     */
    /*
    @FeignClient(
        name = "user-service",           // 服务名
        url = "http://localhost:8081",   // 指定URL（可选）
        fallback = UserServiceFallback.class,  // 降级处理
        configuration = FeignConfig.class      // 自定义配置
    )
    public interface UserFeignClient {

        @GetMapping("/user/{id}")
        User getUser(@PathVariable("id") Long id);

        @PostMapping("/user")
        User createUser(@RequestBody User user);

        @GetMapping("/users")
        List<User> listUsers(@RequestParam("name") String name);

        // 支持Spring MVC注解
        @RequestMapping(method = RequestMethod.GET, value = "/search")
        User search(@RequestHeader("X-Token") String token,
                    @RequestParam("keyword") String keyword);
    }
    */

    /**
     * 使用Feign客户端
     */
    /*
    @Service
    @RequiredArgsConstructor
    public class OrderService {

        private final UserFeignClient userFeignClient;

        public Order createOrder(Long userId) {
            // 像调用本地方法一样调用远程服务
            User user = userFeignClient.getUser(userId);

            Order order = new Order();
            order.setUserId(userId);
            order.setUserName(user.getName());
            return order;
        }
    }
    */

    // ==================== 降级处理 ====================

    /**
     * Fallback实现
     */
    /*
    @Component
    public class UserServiceFallback implements UserFeignClient {

        @Override
        public User getUser(Long id) {
            // 返回默认值
            User user = new User();
            user.setId(id);
            user.setName("默认用户");
            return user;
        }

        @Override
        public User createUser(User user) {
            throw new RuntimeException("用户服务不可用");
        }

        @Override
        public List<User> listUsers(String name) {
            return Collections.emptyList();
        }
    }
    */

    /**
     * Fallback Factory（可获取异常信息）
     */
    /*
    @Component
    public class UserServiceFallbackFactory implements FallbackFactory<UserFeignClient> {

        @Override
        public UserFeignClient create(Throwable cause) {
            return new UserFeignClient() {
                @Override
                public User getUser(Long id) {
                    log.error("调用用户服务失败", cause);
                    return new User(id, "降级用户", null);
                }

                // ... 其他方法
            };
        }
    }

    // 使用
    @FeignClient(name = "user-service", fallbackFactory = UserServiceFallbackFactory.class)
    */

    // ==================== Feign配置 ====================

    /**
     * 全局配置
     *
     * feign:
     *   client:
     *     config:
     *       default:
     *         connectTimeout: 5000
     *         readTimeout: 5000
     *         loggerLevel: full
     *
     *   # 启用压缩
     *   compression:
     *     request:
     *       enabled: true
     *       mime-types: text/xml,application/xml,application/json
     *     response:
     *       enabled: true
     *
     *   # 开启Hystrix支持（已废弃，建议使用Sentinel）
     *   hystrix:
     *     enabled: true
     */

    /**
     * 单独服务配置
     */
    /*
    feign:
      client:
        config:
          user-service:
            connectTimeout: 3000
            readTimeout: 3000
            loggerLevel: headers
    */

    /**
     * 自定义配置类
     */
    /*
    @Configuration
    public class FeignConfig {

        // 日志级别
        @Bean
        public Logger.Level feignLoggerLevel() {
            return Logger.Level.FULL;
        }

        // 契约（支持Feign原生注解）
        @Bean
        public Contract feignContract() {
            return new SpringMvcContract();
        }

        // 编码器
        @Bean
        public Encoder feignEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
            return new SpringEncoder(messageConverters);
        }

        // 解码器
        @Bean
        public Decoder feignDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {
            return new SpringDecoder(messageConverters);
        }

        // 超时配置
        @Bean
        public Request.Options options() {
            return new Request.Options(5000, TimeUnit.MILLISECONDS, 10000, TimeUnit.MILLISECONDS, true);
        }

        // 重试策略
        @Bean
        public Retryer feignRetryer() {
            // 最大重试次数，初始间隔，最大间隔
            return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
        }
    }
    */

    // ==================== 日志配置 ====================

    /**
     * 日志级别：
     * NONE：无日志（默认）
     * BASIC：请求方法、URL、响应状态码、执行时间
     * HEADERS：BASIC + 请求头和响应头
     * FULL：HEADERS + 请求体和响应体
     */
    /*
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    */

    /**
     * 配置日志输出（需要在配置文件中指定接口的日志级别）
     *
     * logging:
     *   level:
     *     com.example.demo.feign.UserFeignClient: DEBUG
     */

    // ==================== 拦截器 ====================

    /**
     * 请求拦截器：统一添加请求头等
     */
    /*
    @Component
    public class AuthFeignInterceptor implements RequestInterceptor {

        @Override
        public void apply(RequestTemplate template) {
            // 从上下文获取Token
            String token = SecurityContextHolder.getContext().getToken();

            // 添加请求头
            template.header("Authorization", "Bearer " + token);

            // 添加查询参数
            // template.query("source", "feign");
        }
    }
    */

    /**
     * 响应拦截器
     */
    /*
    @Component
    public class ResponseInterceptor implements ResponseInterceptor {

        @Override
        public Object aroundDecode(InvocationContext invocationContext) {
            Response response = invocationContext.response();

            // 处理响应头
            String traceId = response.headers().get("X-Trace-Id").iterator().next();
            MDC.put("traceId", traceId);

            return invocationContext.proceed();
        }
    }
    */

    // ==================== 自定义HTTP客户端 ====================

    /**
     * 默认：JDK HttpURLConnection（功能有限）
     * 推荐：Apache HttpClient 或 OkHttp
     *
     * 配置OkHttp：
     */
    /*
    <!-- 依赖 -->
    <dependency>
        <groupId>io.github.openfeign</groupId>
        <artifactId>feign-okhttp</artifactId>
    </dependency>

    // 配置
    @Configuration
    public class OkHttpFeignConfig {

        @Bean
        public okhttp3.OkHttpClient okHttpClient() {
            return new okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
                .build();
        }
    }

    # 启用OkHttp
    feign.okhttp.enabled=true
    */

    // ==================== 继承支持 ====================

    /**
     * 接口继承：服务端和客户端共享接口定义
     */
    /*
    // 公共API模块
    public interface UserAPI {
        @GetMapping("/user/{id}")
        User getUser(@PathVariable Long id);
    }

    // 服务端实现
    @RestController
    public class UserController implements UserAPI {
        @Override
        public User getUser(Long id) {
            return userService.getById(id);
        }
    }

    // 客户端调用
    @FeignClient("user-service")
    public interface UserFeignClient extends UserAPI {
        // 自动继承接口定义
    }
    */

    // ==================== Feign调用流程 ====================

    /**
     * Feign调用原理：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                     Feign调用流程                            │
     * ├─────────────────────────────────────────────────────────────┤
     * │ 1. Spring启动时扫描@FeignClient注解                          │
     * │                           ↓                                 │
     * │ 2. 为每个接口创建动态代理（JDK动态代理）                       │
     * │                           ↓                                 │
     * │ 3. 方法调用时，拦截器解析注解信息                              │
     * │                           ↓                                 │
     * │ 4. RequestInterceptor处理请求                                │
     * │                           ↓                                 │
     * │ 5. Encoder编码请求参数                                        │
     * │                           ↓                                 │
     * │ 6. Client发送HTTP请求                                        │
     * │                           ↓                                 │
     * │ 7. Decoder解码响应结果                                        │
     * │                           ↓                                 │
     * │ 8. 返回结果给调用方                                           │
     * └─────────────────────────────────────────────────────────────┘
     */

    // ==================== 最佳实践 ====================

    /**
     * 1. 统一Feign配置
     *    - 抽取公共配置到配置类
     *    - 使用fallback处理降级
     *
     * 2. 超时设置
     *    - connectTimeout：连接超时
     *    - readTimeout：读取超时
     *    - 根据服务特点合理设置
     *
     * 3. 日志级别
     *    - 开发环境：FULL
     *    - 生产环境：BASIC
     *
     * 4. 重试策略
     *    - 幂等接口可重试
     *    - 非幂等接口慎用重试
     *
     * 5. 线程池隔离
     *    - 使用Sentinel或Hystrix进行线程隔离
     *    - 防止一个慢服务拖垮所有请求
     */

    // ==================== 常见问题 ====================

    /**
     * Q1: Feign调用超时？
     * A: 检查connectTimeout和readTimeout配置
     *    确认服务提供者响应是否正常
     *
     * Q2: Feign调用404？
     * A: 检查URL路径是否正确
     *    确认服务是否注册到注册中心
     *
     * Q3: 请求头丢失？
     * A: 使用RequestInterceptor传递请求头
     *    注意在异步场景下使用RequestContextHolder
     *
     * Q4: 性能问题？
     * A: 使用连接池（OkHttp/HttpClient）
     *    开启GZIP压缩
     *    合理设置超时时间
     */
}
