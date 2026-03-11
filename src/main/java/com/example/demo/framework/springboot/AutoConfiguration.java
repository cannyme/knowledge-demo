package com.example.demo.framework.springboot;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Spring Boot 自动装配原理
 *
 * 【什么是自动装配】
 * Spring Boot 通过约定优于配置的方式，自动将所需的Bean装配到容器中。
 * 开发者只需引入starter依赖，无需手动配置。
 *
 * 【核心注解】
 * @SpringBootApplication = @SpringBootConfiguration
 *                       + @EnableAutoConfiguration
 *                       + @ComponentScan
 *
 * 【自动装配流程】
 * 1. 启动类上的 @EnableAutoConfiguration
 * 2. 通过 @Import 导入 AutoConfigurationImportSelector
 * 3. 读取 META-INF/spring.factories（或新版 org.springframework.boot.autoconfigure.AutoConfiguration.imports）
 * 4. 根据条件注解筛选符合条件的配置类
 * 5. 注册Bean到容器
 */
public class AutoConfiguration {

    // ==================== @EnableAutoConfiguration 源码解析 ====================

    /**
     * @EnableAutoConfiguration 注解定义
     *
     * @Target(ElementType.TYPE)
     * @Retention(RetentionPolicy.RUNTIME)
     * @Documented
     * @Inherited
     * @AutoConfigurationPackage          // 将主类所在包注册为自动配置包
     * @Import(AutoConfigurationImportSelector.class)  // 核心：导入自动配置选择器
     * public @interface EnableAutoConfiguration {
     *     String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";
     *     Class<?>[] exclude() default {};
     *     String[] excludeName() default {};
     * }
     *
     * 关键：@Import(AutoConfigurationImportSelector.class)
     * 这个类负责加载所有自动配置类
     */

    /**
     * AutoConfigurationImportSelector 核心逻辑
     *
     * protected List<String> getCandidateConfigurations(AnnotationMetadata metadata,
     *                                                   AnnotationAttributes attributes) {
     *     // 1. 从 META-INF/spring.factories 加载
     *     List<String> configurations = SpringFactoriesLoader.loadFactoryNames(
     *         getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader());
     *
     *     // 2. 从 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 加载（Spring Boot 2.7+）
     *     ImportCandidates.load(AutoConfiguration.class, getBeanClassLoader())
     *         .forEach(configurations::add);
     *
     *     return configurations;
     * }
     *
     * 加载位置：
     * - spring-boot-autoconfigure-xxx.jar 中的配置文件
     * - 各个 starter 中的配置文件
     */

    // ==================== spring.factories 文件格式 ====================

    /**
     * META-INF/spring.factories 示例（旧版，Spring Boot 2.7前）
     *
     * # Auto Configure
     * org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
     * org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration,\
     * org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
     * org.springframework.boot.autoconfigure.redis.RedisAutoConfiguration,\
     * org.springframework.boot.autoconfigure.mybatis.MybatisAutoConfiguration
     *
     * Spring Boot 2.7+ 新格式（META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports）：
     * org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
     * org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
     * org.springframework.boot.autoconfigure.redis.RedisAutoConfiguration
     */

    // ==================== 条件注解 ====================

    /**
     * Spring Boot 提供的条件注解，用于控制自动配置类是否生效
     *
     * ┌───────────────────────────────┬─────────────────────────────────────────┐
     * │ 条件注解                        │ 触发条件                                 │
     * ├───────────────────────────────┼─────────────────────────────────────────┤
     * │ @ConditionalOnClass           │ 类路径存在指定类                          │
     * │ @ConditionalOnMissingClass    │ 类路径不存在指定类                        │
     * │ @ConditionalOnBean            │ 容器中存在指定Bean                        │
     * │ @ConditionalOnMissingBean     │ 容器中不存在指定Bean                      │
     * │ @ConditionalOnProperty        │ 配置属性满足条件                          │
     * │ @ConditionalOnWebApplication  │ 是Web应用                                │
     * │ @ConditionalOnNotWebApplication│ 不是Web应用                             │
     * │ @ConditionalOnExpression      │ SpEL表达式为true                         │
     * │ @ConditionalOnResource        │ 资源存在                                 │
     * │ @ConditionalOnJndi            │ JNDI存在                                │
     * │ @ConditionalOnJava            │ Java版本匹配                             │
     * │ @ConditionalOnSingleCandidate │ 容器中只有一个指定Bean或一个@Primary      │
     * └───────────────────────────────┴─────────────────────────────────────────┘
     */

    // ==================== 自动配置类示例 ====================

    /**
     * 以 RedisAutoConfiguration 为例（简化版）
     */
    // @Configuration(proxyBeanMethods = false)
    // @ConditionalOnClass(RedisOperations.class)  // 类路径有Redis依赖
    // @EnableConfigurationProperties(RedisProperties.class)  // 启用配置属性
    // @Import({ LettuceConnectionConfiguration.class, JedisConnectionConfiguration.class })
    static class RedisAutoConfigurationExample {

        // @Bean
        // @ConditionalOnMissingBean(name = "redisTemplate")  // 用户没自定义时才创建
        // @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
        public Object redisTemplate(/* RedisConnectionFactory redisConnectionFactory */) {
            // return new RedisTemplate<Object, Object>();
            return null;
        }

        // @Bean
        // @ConditionalOnMissingBean
        // @ConditionalOnSingleCandidate(RedisConnectionFactory.class)
        public Object stringRedisTemplate() {
            // return new StringRedisTemplate();
            return null;
        }
    }

    /**
     * 数据源自动配置示例
     */
    // @Configuration(proxyBeanMethods = false)
    // @ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
    // @ConditionalOnMissingBean(type = "io.r2dbc.spi.ConnectionFactory")
    // @EnableConfigurationProperties(DataSourceProperties.class)
    // @Import({ DataSourcePoolMetadataProvidersConfiguration.class,
    //          DataSourceInitializationConfiguration.InitializationSpecificCredentialsDataSourceInitializationConfiguration.class,
    //          DataSourceInitializationConfiguration.SharedCredentialsDataSourceInitializationConfiguration.class })
    static class DataSourceAutoConfigurationExample {

        // @Bean
        // @ConditionalOnMissingBean
        public Object dataSource(/* DataSourceProperties properties */) {
            // 根据配置创建数据源
            return null;
        }
    }

    // ==================== 配置属性绑定 ====================

    /**
     * @ConfigurationProperties 将配置文件绑定到Bean
     *
     * application.yml:
     * my:
     *   service:
     *     enabled: true
     *     timeout: 5000
     *     hosts:
     *       - host1
     *       - host2
     */
    // @Component
    // @ConfigurationProperties(prefix = "my.service")
    static class MyServiceProperties {
        private boolean enabled = true;
        private long timeout = 3000;
        private List<String> hosts = new ArrayList<>();

        // getter/setter
    }

    /**
     * 在自动配置类中使用
     */
    // @Configuration
    // @EnableConfigurationProperties(MyServiceProperties.class)
    // @ConditionalOnProperty(prefix = "my.service", name = "enabled", havingValue = "true")
    static class MyServiceAutoConfiguration {

        // @Bean
        // @ConditionalOnMissingBean
        public Object myService(/* MyServiceProperties properties */) {
            // return new MyService(properties);
            return null;
        }
    }

    // ==================== 条件注解执行顺序 ====================

    /**
     * AutoConfigurationSorter 排序规则：
     *
     * 1. @AutoConfigureOrder 指定顺序（数值越小越先）
     * 2. @AutoConfigureBefore / @AutoConfigureAfter 指定相对顺序
     * 3. 字母顺序
     *
     * 示例：
     * @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
     * @AutoConfigureBefore(DataSourceAutoConfiguration.class)
     * @AutoConfigureAfter(RedisAutoConfiguration.class)
     */
    // @Configuration
    // @AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
    // @AutoConfigureBefore(DataSourceAutoConfiguration.class)
    static class MyAutoConfiguration {
    }

    // ==================== 自定义 Starter ====================

    /**
     * 自定义 Starter 步骤
     *
     * 1. 创建 starter 模块（命名规则：xxx-spring-boot-starter）
     * 2. 创建自动配置类
     * 3. 创建配置属性类
     * 4. 创建 spring.factories 或 imports 文件
     * 5. 添加 spring-boot-autoconfigure 依赖
     */

    /**
     * 完整 Starter 示例
     *
     * 项目结构：
     * my-spring-boot-starter/
     * ├── pom.xml
     * └── src/main/
     *     ├── java/com/example/
     *     │   ├── MyService.java              # 核心服务
     *     │   ├── MyServiceProperties.java    # 配置属性
     *     │   └── MyServiceAutoConfiguration.java  # 自动配置
     *     └── resources/
     *         └── META-INF/
     *             └── spring/
     *                 └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
     */

    /**
     * 核心服务类
     */
    static class MyService {
        private final String prefix;
        private final boolean enabled;

        public MyService(String prefix, boolean enabled) {
            this.prefix = prefix;
            this.enabled = enabled;
        }

        public String process(String message) {
            if (!enabled) {
                return "Service disabled";
            }
            return prefix + ": " + message;
        }
    }

    /**
     * 配置属性类
     */
    // @ConfigurationProperties(prefix = "my.service")
    static class MyServiceProperties2 {
        private String prefix = "[MyService]";
        private boolean enabled = true;

        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /**
     * 自动配置类
     */
    // @Configuration(proxyBeanMethods = false)
    // @ConditionalOnClass(MyService.class)
    // @EnableConfigurationProperties(MyServiceProperties2.class)
    // @ConditionalOnProperty(prefix = "my.service", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class MyServiceAutoConfiguration2 {

        // @Bean
        // @ConditionalOnMissingBean
        public MyService myService(/* MyServiceProperties2 properties */) {
            // return new MyService(properties.getPrefix(), properties.isEnabled());
            return null;
        }
    }

    // ==================== 排除自动配置 ====================

    /**
     * 方式1：@SpringBootApplication 注解排除
     *
     * @SpringBootApplication(exclude = {
     *     DataSourceAutoConfiguration.class,
     *     RedisAutoConfiguration.class
     * })
     *
     * 方式2：配置文件排除
     * spring.autoconfigure.exclude=\
     *   org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
     *
     * 方式3：@EnableAutoConfiguration 排除
     * @EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
     */

    // ==================== 自动装配流程图 ====================

    /**
     * 完整流程：
     *
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │                        Spring Boot 启动                              │
     * └───────────────────────────┬─────────────────────────────────────────┘
     *                             │
     *                             ▼
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │  @EnableAutoConfiguration                                            │
     * │  └── @Import(AutoConfigurationImportSelector.class)                 │
     * └───────────────────────────┬─────────────────────────────────────────┘
     *                             │
     *                             ▼
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │  AutoConfigurationImportSelector.getCandidateConfigurations()      │
     * │  ├── 读取 META-INF/spring.factories                                 │
     * │  └── 读取 META-INF/spring/...AutoConfiguration.imports              │
     * └───────────────────────────┬─────────────────────────────────────────┘
     *                             │
     *                             ▼
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │  获得所有候选自动配置类（约100+个）                                    │
     * │  WebMvcAutoConfiguration, DataSourceAutoConfiguration, ...         │
     * └───────────────────────────┬─────────────────────────────────────────┘
     *                             │
     *                             ▼
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │  条件注解过滤                                                        │
     * │  @ConditionalOnClass → 类路径是否有依赖                              │
     * │  @ConditionalOnBean → 容器中是否有Bean                               │
     * │  @ConditionalOnProperty → 配置是否满足                               │
     * │  ...                                                                │
     * └───────────────────────────┬─────────────────────────────────────────┘
     *                             │
     *                             ▼
     * ┌─────────────────────────────────────────────────────────────────────┐
     * │  注册有效的配置类中的Bean到容器                                        │
     * └─────────────────────────────────────────────────────────────────────┘
     */

    // ==================== 调试自动配置 ====================

    /**
     * 1. 启动时打印自动配置报告
     *    java -jar app.jar --debug
     *    或
     *    application.yml: debug: true
     *
     * 报告内容：
     * ============================
     * CONDITIONS EVALUATION REPORT
     * ============================
     *
     * Positive matches:（匹配成功的配置）
     * -----------------
     *    WebMvcAutoConfiguration matched:
     *       - @ConditionalOnClass found required classes...
     *
     * Negative matches:（未匹配的配置）
     * -----------------
     *    RedisAutoConfiguration:
     *       Did not match:
     *          - @ConditionalOnClass did not find required class...
     *
     * 2. 使用 Actuator 端点
     *    /actuator/conditions
     *    /actuator/beans
     *    /actuator/configprops
     */

    /**
     * 3. 编程方式查看
     */
    public static void showAutoConfigurationReport(/* ConfigurableApplicationContext context */) {
        // ConditionEvaluationReport report = ConditionEvaluationReport
        //     .get(context.getBeanFactory());
        //
        // System.out.println("已匹配的自动配置：");
        // report.getMatches().forEach(System.out::println);
        //
        // System.out.println("\n未匹配的自动配置：");
        // report.getExclusions().forEach(System.out::println);
    }

    // ==================== 常见自动配置类 ====================

    /**
     * ┌─────────────────────────────────────┬───────────────────────────┐
     * │ 自动配置类                            │ 功能                       │
     * ├─────────────────────────────────────┼───────────────────────────┤
     * │ WebMvcAutoConfiguration             │ Spring MVC                │
     * │ DataSourceAutoConfiguration         │ 数据源                     │
     * │ RedisAutoConfiguration              │ Redis                     │
     * │ MybatisAutoConfiguration            │ MyBatis                   │
     * │ TransactionAutoConfiguration        │ 事务管理                   │
     * │ SecurityAutoConfiguration           │ Spring Security           │
     * │ JacksonAutoConfiguration            │ JSON序列化                 │
     * │ TaskExecutionAutoConfiguration      │ 异步任务线程池              │
     * │ CacheAutoConfiguration              │ 缓存                       │
     * │ ValidationAutoConfiguration         │ 参数校验                   │
     * └─────────────────────────────────────┴───────────────────────────┘
     */

    // ==================== 面试高频问题 ====================

    /**
     * Q1: Spring Boot 自动装配原理？
     * A: 启动时通过 @EnableAutoConfiguration 导入 AutoConfigurationImportSelector，
     *    该类读取 spring.factories 或 imports 文件，获取所有候选配置类，
     *    再根据条件注解筛选，最后注册Bean到容器。
     *
     * Q2: @ConditionalOnMissingBean 为什么能防止重复？
     * A: 自动配置类中的Bean通常添加 @ConditionalOnMissingBean，
     *    如果用户已自定义了该Bean，自动配置就不会创建，实现了"用户优先"。
     *
     * Q3: 如何自定义Starter？
     * A: 1) 创建自动配置类 2) 创建配置属性类
     *    3) 在 META-INF/spring/...imports 中注册配置类
     *    4) 打包发布
     *
     * Q4: 为什么Starter能自动生效？
     * A: 因为Starter的jar包中包含了 spring.factories 或 imports 文件，
     *    Spring Boot启动时会扫描所有jar包中的这些文件。
     *
     * Q5: 如何排除某个自动配置？
     * A: 1) @SpringBootApplication(exclude=XXX.class)
     *    2) spring.autoconfigure.exclude=xxx
     *    3) @EnableAutoConfiguration(exclude=XXX.class)
     */

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        System.out.println("Spring Boot 自动装配原理");
        System.out.println("\n核心注解：@EnableAutoConfiguration");
        System.out.println("核心类：AutoConfigurationImportSelector");
        System.out.println("\n配置文件位置：");
        System.out.println("1. META-INF/spring.factories（旧版）");
        System.out.println("2. META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports（新版）");
    }
}
