package com.example.demo.framework.spring;

import org.aspectj.lang.annotation.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

/**
 * Spring AOP（面向切面编程）
 *
 * 【核心概念】
 * AOP：将横切关注点（日志、事务、权限等）从业务逻辑中分离出来
 *
 * 【应用场景】
 * 1. 日志记录
 * 2. 事务管理
 * 3. 权限校验
 * 4. 性能监控
 * 5. 异常处理
 * 6. 缓存
 *
 * 【关键术语】
 * ┌──────────────┬─────────────────────────────────────────┐
 * │ 术语          │ 说明                                     │
 * ├──────────────┼─────────────────────────────────────────┤
 * │ 切面(Aspect)  │ 横切关注点的模块化（日志、事务等）          │
 * │ 连接点(JoinPoint)│ 程序执行的特定点（方法调用、异常抛出）   │
 * │ 切点(Pointcut)│ 匹配连接点的表达式                        │
 * │ 通知(Advice) │ 在切点执行的动作                          │
 * │ 目标对象(Target)│ 被通知的对象                             │
 * │ 代理(Proxy)  │ AOP创建的对象，包含通知                   │
 * │ 织入(Weaving)│ 将切面应用到目标对象的过程                │
 * └──────────────┴─────────────────────────────────────────┘
 */
public class SpringAOP {

    // ==================== 切点表达式 ====================

    /**
     * execution表达式语法：
     * execution(修饰符? 返回类型 包名.类名.方法名(参数) 异常?)
     *
     * 常用示例：
     *
     * // 匹配所有public方法
     * execution(public * *(..))
     *
     * // 匹配UserService的所有方法
     * execution(* com.example.service.UserService.*(..))
     *
     * // 匹配service包下所有类的所有方法
     * execution(* com.example.service.*.*(..))
     *
     * // 匹配service包及子包下所有类的所有方法
     * execution(* com.example.service..*.*(..))
     *
     * // 匹配所有以save开头的方法
     * execution(* save*(..))
     *
     * // 匹配只有一个String参数的方法
     * execution(* *(String))
     *
     * // 匹配第一个参数是String的方法
     * execution(* *(String, ..))
     */

    /**
     * 其他切点指示符：
     *
     * @annotation() - 匹配有特定注解的方法
     * @within()    - 匹配有特定注解的类的方法
     * @args()      - 匹配参数有特定注解的方法
     * args()       - 匹配参数类型
     * within()     - 匹配类型
     * bean()       - 匹配Bean名称
     *
     * 示例：
     * @annotation(com.example.anno.Log)
     * within(com.example.service.*)
     * bean(userService)
     */

    // ==================== 五种通知类型 ====================

    /**
     * @Before     - 前置通知：方法执行前
     * @After      - 后置通知：方法执行后（无论成功失败）
     * @AfterReturning - 返回通知：方法成功返回后
     * @AfterThrowing  - 异常通知：方法抛出异常后
     * @Around     - 环绕通知：完全控制方法执行
     *
     * 执行顺序：
     * ┌────────────────────────────────────────┐
     * │ Around(前置部分)                        │
     * ├────────────────────────────────────────┤
     * │ Before                                  │
     * ├────────────────────────────────────────┤
     * │ 目标方法执行                             │
     * ├───────────────┬────────────────────────┤
     * │ 成功           │ 异常                   │
     * ├───────────────┼────────────────────────┤
     * │ AfterReturning │ AfterThrowing         │
     * ├───────────────┴────────────────────────┤
     * │ After                                   │
     * ├────────────────────────────────────────┤
     * │ Around(后置部分)                        │
     * └────────────────────────────────────────┘
     */

    // ==================== 示例：日志切面 ====================

    /**
     * 定义注解
     */
    // @Target(ElementType.METHOD)
    // @Retention(RetentionPolicy.RUNTIME)
    public @interface Log {
        String value() default "";
    }

    /**
     * 日志切面
     */
    // @Aspect
    // @Component
    static class LogAspect {

        // 定义切点：所有带@Log注解的方法
        // @Pointcut("@annotation(com.example.demo.framework.spring.SpringAOP.Log)")
        public void logPointcut() {}

        // 前置通知
        // @Before("logPointcut()")
        public void before(/* JoinPoint joinPoint */) {
            System.out.println("方法开始执行");
            // String methodName = joinPoint.getSignature().getName();
            // Object[] args = joinPoint.getArgs();
        }

        // 后置通知
        // @After("logPointcut()")
        public void after() {
            System.out.println("方法执行结束");
        }

        // 返回通知
        // @AfterReturning(pointcut = "logPointcut()", returning = "result")
        public void afterReturning(Object result) {
            System.out.println("方法返回：" + result);
        }

        // 异常通知
        // @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
        public void afterThrowing(Exception e) {
            System.out.println("方法异常：" + e.getMessage());
        }

        // 环绕通知（最强大）
        // @Around("logPointcut()")
        public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
            long start = System.currentTimeMillis();
            System.out.println("环绕前置");

            try {
                // 执行目标方法
                Object result = joinPoint.proceed();
                System.out.println("环绕返回");
                return result;
            } catch (Exception e) {
                System.out.println("环绕异常");
                throw e;
            } finally {
                long end = System.currentTimeMillis();
                System.out.println("耗时：" + (end - start) + "ms");
            }
        }
    }

    // ==================== 示例：权限校验切面 ====================

    /**
     * 权限注解
     */
    // @Target(ElementType.METHOD)
    // @Retention(RetentionPolicy.RUNTIME)
    public @interface RequireRole {
        String[] value();
    }

    /**
     * 权限切面
     */
    // @Aspect
    // @Component
    static class PermissionAspect {

        // @Before("@annotation(requireRole)")
        public void checkPermission(/* RequireRole requireRole */) {
            // 获取当前用户角色
            // String currentUserRole = SecurityContext.getRole();

            // 检查是否有权限
            // String[] requiredRoles = requireRole.value();
            // if (!Arrays.asList(requiredRoles).contains(currentUserRole)) {
            //     throw new PermissionDeniedException("无权限");
            // }

            System.out.println("权限校验通过");
        }
    }

    // ==================== 示例：性能监控切面 ====================

    /**
     * 性能监控切面
     */
    // @Aspect
    // @Component
    static class PerformanceAspect {

        // @Around("execution(* com.example.service..*.*(..))")
        public Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            long start = System.nanoTime();
            try {
                return joinPoint.proceed();
            } finally {
                long duration = System.nanoTime() - start;
                System.out.printf("[%s.%s] 耗时: %.2fms%n",
                    className, methodName, duration / 1_000_000.0);
            }
        }
    }

    // ==================== JDK动态代理 vs CGLIB ====================

    /**
     * Spring AOP的两种代理方式：
     *
     * ┌─────────────────┬─────────────────────┬─────────────────────┐
     * │ 特性             │ JDK动态代理          │ CGLIB               │
     * ├─────────────────┼─────────────────────┼─────────────────────┤
     * │ 原理             │ 基于接口             │ 基于继承             │
     * │ 限制             │ 目标类必须实现接口    │ 不能代理final类/方法 │
     * │ 性能             │ JDK8后效率接近CGLIB  │ 生成子类，略快       │
     * │ Spring默认       │ 有接口时使用          │ 无接口时使用         │
     * └─────────────────┴─────────────────────┴─────────────────────┘
     *
     * 配置：
     * spring.aop.proxy-target-class=true  强制使用CGLIB
     * spring.aop.proxy-target-class=false 有接口用JDK，无接口用CGLIB
     */

    /**
     * JDK动态代理示例
     */
    interface UserService {
        void save();
    }

    static class UserServiceImpl implements UserService {
        @Override
        public void save() {
            System.out.println("保存用户");
        }
    }

    static class JdkProxyDemo implements java.lang.reflect.InvocationHandler {
        private final Object target;

        public JdkProxyDemo(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args)
                throws Throwable {
            System.out.println("JDK代理前置");
            Object result = method.invoke(target, args);
            System.out.println("JDK代理后置");
            return result;
        }

        public static <T> T createProxy(T target) {
            return (T) java.lang.reflect.Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new JdkProxyDemo(target)
            );
        }
    }

    /**
     * CGLIB代理示例（伪代码）
     */
    /*
    static class CglibProxyDemo implements MethodInterceptor {
        private final Object target;

        public Object intercept(Object obj, Method method, Object[] args,
                                MethodProxy proxy) throws Throwable {
            System.out.println("CGLIB代理前置");
            Object result = method.invoke(target, args);
            System.out.println("CGLIB代理后置");
            return result;
        }

        public static <T> T createProxy(T target) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(target.getClass());
            enhancer.setCallback(new CglibProxyDemo(target));
            return (T) enhancer.create();
        }
    }
    */

    // ==================== AOP实现原理 ====================

    /**
     * Spring AOP实现原理：
     *
     * 1. 启动阶段
     *    - 扫描@Aspect类
     *    - 解析切点表达式
     *    - 创建Advisor（切点+通知）
     *
     * 2. Bean实例化阶段
     *    - AbstractAutoProxyCreator.postProcessAfterInitialization()
     *    - 查找匹配当前Bean的Advisor
     *    - 如果有匹配，创建代理对象
     *
     * 3. 代理对象创建
     *    - 有接口：JDK动态代理
     *    - 无接口：CGLIB
     *
     * 4. 方法调用
     *    - 调用代理对象的方法
     *    - 代理对象按责任链模式执行通知
     *    - 最终调用目标方法
     */

    /**
     * 责任链模式执行通知
     *
     * 代理对象内部维护一个拦截器链：
     *
     * MethodInterceptor[] interceptors = {
     *     ExposeInvocationInterceptor,  // 暴露调用上下文
     *     AspectJAfterThrowingAdvice,   // @AfterThrowing
     *     AspectJAfterReturningAdvice,  // @AfterReturning
     *     AspectJAfterAdvice,           // @After
     *     AspectJAroundAdvice,          // @Around
     *     AspectJMethodBeforeAdvice,    // @Before
     *     targetMethod                  // 目标方法
     * };
     *
     * 执行过程类似递归：
     * interceptor[i].invoke() → interceptor[i+1].invoke() → ...
     */

    // ==================== AOP注意事项 ====================

    /**
     * 1. 自调用问题
     *
     * 同一个类中，方法A调用方法B，B的切面不生效
     * 原因：自调用走的是this，不是代理对象
     */
    // @Service
    static class SelfCallService {

        public void methodA() {
            // ❌ B的切面不生效
            // this.methodB();

            // ✅ 通过代理调用
            // ((SelfCallService) AopContext.currentProxy()).methodB();
        }

        // @Log
        public void methodB() {
            System.out.println("B方法");
        }
    }

    /**
     * 2. private方法
     *
     * AOP无法代理private方法（CGLIB限制）
     * 解决：改为protected或public
     */

    /**
     * 3. final方法/类
     *
     * CGLIB无法代理final，会静默忽略
     * 解决：去掉final修饰符，或使用接口+JDK代理
     */

    /**
     * 4. 循环依赖 + AOP
     *
     * Spring通过三级缓存解决：
     * - singletonObjects：完整Bean
     * - earlySingletonObjects：早期Bean（可能被AOP）
     * - singletonFactories：Bean工厂
     *
     * 流程：
     * 1. 创建Bean A
     * 2. 注入B，发现B不存在
     * 3. 创建Bean B
     * 4. B注入A，从三级缓存获取A的早期引用（可能已代理）
     * 5. B完成创建
     * 6. A完成创建
     */

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // JDK动态代理测试
        UserService userService = new UserServiceImpl();
        UserService proxy = JdkProxyDemo.createProxy(userService);
        proxy.save();
    }
}
