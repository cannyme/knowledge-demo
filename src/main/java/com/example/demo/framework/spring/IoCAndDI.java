package com.example.demo.framework.spring;

import org.springframework.context.annotation.Configuration;

/**
 * Spring IoC（控制反转）和 DI（依赖注入）
 *
 * 【核心概念】
 * IoC（Inversion of Control）：控制反转
 * - 传统方式：对象自己创建和管理依赖
 * - IoC方式：由容器创建和管理对象，对象被动接收依赖
 *
 * DI（Dependency Injection）：依赖注入
 * - IoC的具体实现方式
 * - 容器负责将依赖注入到对象中
 *
 * 【IoC容器】
 * 1. BeanFactory：基础容器，延迟加载
 * 2. ApplicationContext：高级容器，立即加载，支持AOP、国际化等
 *
 * 【依赖注入方式】
 * 1. 构造器注入（推荐）
 * 2. Setter注入
 * 3. 字段注入（不推荐）
 */
public class IoCAndDI {

    // ==================== Bean的注册方式 ====================

    /**
     * 方式1：@Component 及其衍生注解
     *
     * @Component - 通用组件
     * @Repository - 数据访问层
     * @Service - 业务层
     * @Controller - 控制层
     * @Configuration - 配置类
     */
    // @Component
    // @Scope("singleton") // 默认单例
    // @Scope("prototype") // 原型，每次获取创建新实例
    static class UserService {
        // 业务逻辑
    }

    /**
     * 方式2：@Configuration + @Bean
     *
     * 适用于：第三方库的类、需要复杂初始化逻辑
     */
    @Configuration
    static class AppConfig {

        // @Bean
        // @Bean("customName") // 指定bean名称
        // @Bean(initMethod = "init", destroyMethod = "destroy")
        public UserService userService() {
            return new UserService();
        }
    }

    /**
     * 方式3：@Import
     *
     * 快速导入配置类或组件
     */
    // @Import(AppConfig.class)
    // @Import(UserService.class) // 直接导入类
    static class MainConfig {
    }

    // ==================== 依赖注入方式对比 ====================

    // 场景：用户服务依赖用户仓库
    static class UserRepository {
        public String findUserById(Long id) {
            return "User-" + id;
        }
    }

    /**
     * 方式1：构造器注入（推荐）✅
     *
     * 优点：
     * 1. 依赖明确，不可变（final）
     * 2. 便于单元测试（可以手动传入mock对象）
     * 3. 确保对象构造完成后就是完整的
     * 4. 循环依赖会在启动时报错（容易发现）
     *
     * 注意：如果只有一个构造器，@Autowired 可省略
     */
    // @Service
    static class UserConstructorService {
        private final UserRepository userRepository;

        // @Autowired // 单构造器可省略
        public UserConstructorService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
    }

    /**
     * 方式2：Setter注入
     *
     * 优点：
     * 1. 可选依赖
     * 2. 可以在运行时重新注入
     *
     * 缺点：
     * 1. 依赖可能为null
     * 2. 对象可能不完整
     */
    // @Service
    static class UserSetterService {
        private UserRepository userRepository;

        // @Autowired
        public void setUserRepository(UserRepository userRepository) {
            this.userRepository = userRepository;
        }
    }

    /**
     * 方式3：字段注入（不推荐）❌
     *
     * 缺点：
     * 1. 无法设置final字段
     * 2. 单元测试困难（无法注入mock）
     * 3. 依赖不透明
     * 4. 容易隐藏循环依赖问题
     */
    // @Service
    static class UserFieldService {
        // @Autowired // 不推荐
        private UserRepository userRepository;
    }

    // ==================== Bean的生命周期 ====================
    /**
     * Bean生命周期（简化版）：
     *
     * 1. 实例化（Instantiation）
     *    └─ 调用构造方法创建对象
     *
     * 2. 属性赋值（Populate）
     *    └─ 注入依赖
     *
     * 3. 初始化（Initialization）
     *    ├─ Aware接口回调（BeanNameAware, BeanFactoryAware等）
     *    ├─ BeanPostProcessor.postProcessBeforeInitialization()
     *    ├─ @PostConstruct 注解方法
     *    ├─ InitializingBean.afterPropertiesSet()
     *    └─ BeanPostProcessor.postProcessAfterInitialization()
     *
     * 4. 使用（Use）
     *
     * 5. 销毁（Destruction）
     *    ├─ @PreDestroy 注解方法
     *    └─ DisposableBean.destroy()
     */
    // @Component
    static class LifecycleBean /* implements InitializingBean, DisposableBean */ {

        // @PostConstruct
        public void init() {
            System.out.println("初始化方法");
        }

        // @PreDestroy
        public void cleanup() {
            System.out.println("销毁方法");
        }

        // @Override
        // public void afterPropertiesSet() throws Exception {
        //     System.out.println("InitializingBean初始化");
        // }

        // @Override
        // public void destroy() throws Exception {
        //     System.out.println("DisposableBean销毁");
        // }
    }

    // ==================== Bean的作用域 ====================
    /**
     * Scope类型：
     *
     * singleton   - 单例（默认），整个应用只有一个实例
     * prototype   - 原型，每次获取创建新实例
     * request     - 每次HTTP请求一个实例（Web环境）
     * session     - 每个HTTP Session一个实例（Web环境）
     * application - 整个ServletContext生命周期（Web环境）
     * websocket   - 每个WebSocket会话一个实例
     */
    // @Component
    // @Scope("prototype")
    static class PrototypeBean {
    }

    // ==================== 条件化注册 ====================
    /**
     * 条件注解：
     *
     * @Conditional          - 自定义条件
     * @ConditionalOnClass   - 类路径存在某个类时生效
     * @ConditionalOnMissingClass  - 类路径不存在某个类时生效
     * @ConditionalOnBean    - 容器中存在某个Bean时生效
     * @ConditionalOnMissingBean   - 容器中不存在某个Bean时生效
     * @ConditionalOnProperty      - 配置属性满足条件时生效
     */
    // @Configuration
    static class ConditionalConfig {

        // @Bean
        // @ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
        public UserService userService() {
            return new UserService();
        }
    }

    // ==================== 循环依赖问题 ====================
    /**
     * 循环依赖：A依赖B，B依赖A
     *
     * 解决方案：
     * 1. 构造器注入：无法解决，启动时报错
     * 2. Setter/字段注入：Spring通过三级缓存解决
     * 3. @Lazy：延迟加载打破循环
     * 4. 重构设计：最佳方案，避免循环依赖
     *
     * 三级缓存：
     * - singletonObjects：完整的Bean
     * - earlySingletonObjects：提前暴露的Bean（未完成属性注入）
     * - singletonFactories：Bean工厂，用于生成代理对象
     */
    // @Service
    static class ServiceA {
        private final ServiceB serviceB;

        // 构造器注入的循环依赖解决方案：使用@Lazy
        // public ServiceA(@Lazy ServiceB serviceB) {
        //     this.serviceB = serviceB;
        // }
        public ServiceA(ServiceB serviceB) {
            this.serviceB = serviceB;
        }
    }

    // @Service
    static class ServiceB {
        private final ServiceA serviceA;

        public ServiceB(ServiceA serviceA) {
            this.serviceA = serviceA;
        }
    }

    // ==================== 自动装配 ====================
    /**
     * @Autowired 装配规则：
     * 1. 首先按类型查找
     * 2. 如果有多个，按名称匹配
     * 3. 如果仍无法确定，抛出异常
     *
     * 解决多实现类问题：
     * 1. @Primary - 设置默认实现
     * 2. @Qualifier - 指定名称
     * 3. List<T> - 注入所有实现
     */

    interface PaymentService {
        void pay();
    }

    // @Service
    // @Primary // 默认实现
    static class AlipayService implements PaymentService {
        @Override
        public void pay() {
            System.out.println("支付宝支付");
        }
    }

    // @Service
    static class WechatPayService implements PaymentService {
        @Override
        public void pay() {
            System.out.println("微信支付");
        }
    }

    // @Service
    static class PaymentController {
        private final PaymentService paymentService;

        // 方式1：使用@Primary
        // public PaymentController(PaymentService paymentService) {
        //     this.paymentService = paymentService;
        // }

        // 方式2：使用@Qualifier指定
        // public PaymentController(@Qualifier("wechatPayService") PaymentService paymentService) {
        //     this.paymentService = paymentService;
        // }

        // 方式3：注入所有实现
        // public PaymentController(List<PaymentService> services) {
        //     // 获取所有PaymentService实现
        // }

        public PaymentController(PaymentService paymentService) {
            this.paymentService = paymentService;
        }
    }
}
