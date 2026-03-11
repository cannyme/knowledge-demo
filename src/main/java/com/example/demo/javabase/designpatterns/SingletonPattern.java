package com.example.demo.javabase.designpatterns;

/**
 * 单例模式 (Singleton Pattern)
 *
 * 【核心思想】
 * 确保一个类只有一个实例，并提供一个全局访问点。
 *
 * 【应用场景】
 * 1. 配置管理器
 * 2. 数据库连接池
 * 3. 日志记录器
 * 4. 缓存管理器
 *
 * 【实现方式对比】
 * ┌──────────────────┬─────────────┬─────────────┬─────────────┐
 * │ 实现方式          │ 线程安全     │ 懒加载       │ 推荐程度     │
 * ├──────────────────┼─────────────┼─────────────┼─────────────┤
 * │ 饿汉式            │ 是          │ 否          │ ⭐⭐⭐       │
 * │ 懒汉式(非同步)    │ 否          │ 是          │ ❌ 不推荐    │
 * │ 懒汉式(同步方法)  │ 是          │ 是          │ ⭐ 性能差    │
 * │ 双重检查锁        │ 是          │ 是          │ ⭐⭐⭐⭐     │
 * │ 静态内部类        │ 是          │ 是          │ ⭐⭐⭐⭐⭐   │
 * │ 枚举              │ 是          │ 否          │ ⭐⭐⭐⭐⭐   │
 * └──────────────────┴─────────────┴─────────────┴─────────────┘
 */
public class SingletonPattern {

    // ==================== 方式一：饿汉式 ====================
    // 优点：实现简单，线程安全（类加载时初始化）
    // 缺点：不支持懒加载，可能造成资源浪费
    static class EagerSingleton {
        // 类加载时就创建实例
        private static final EagerSingleton INSTANCE = new EagerSingleton();

        // 私有构造方法，防止外部实例化
        private EagerSingleton() {}

        public static EagerSingleton getInstance() {
            return INSTANCE;
        }
    }

    // ==================== 方式二：懒汉式（不推荐） ====================
    // 优点：懒加载
    // 缺点：线程不安全！多线程环境下可能创建多个实例
    static class LazySingleton {
        private static LazySingleton instance;

        private LazySingleton() {}

        // ❌ 线程不安全：多线程可能同时进入if判断
        public static LazySingleton getInstance() {
            if (instance == null) {
                instance = new LazySingleton();
            }
            return instance;
        }
    }

    // ==================== 方式三：双重检查锁 (DCL) ====================
    // 优点：线程安全 + 懒加载 + 较高性能
    // 关键点：volatile 防止指令重排序
    static class DCLSingleton {
        // volatile 关键字的作用：
        // 1. 保证可见性：一个线程修改后，其他线程立即可见
        // 2. 禁止指令重排序：防止 instance = new DCLSingleton() 被重排序
        //
        // 为什么需要禁止重排序？
        // new 操作分为三步：
        //   1. 分配内存空间
        //   2. 初始化对象
        //   3. 将引用指向内存地址
        // 如果 2 和 3 重排序，其他线程可能获取到未初始化完成的对象
        private static volatile DCLSingleton instance;

        private DCLSingleton() {}

        public static DCLSingleton getInstance() {
            // 第一次检查：避免不必要的同步
            if (instance == null) {
                synchronized (DCLSingleton.class) {
                    // 第二次检查：确保只有一个实例被创建
                    if (instance == null) {
                        instance = new DCLSingleton();
                    }
                }
            }
            return instance;
        }
    }

    // ==================== 方式四：静态内部类（推荐） ====================
    // 优点：
    // 1. 线程安全：JVM 保证类加载时的线程安全
    // 2. 懒加载：外部类加载时不会初始化内部类
    // 3. 代码简洁：不需要 synchronized
    //
    // 原理：类加载机制
    // - 外部类加载时，静态内部类不会加载
    // - 只有调用 getInstance() 时，JVM 才加载 Holder 类
    // - JVM 保证类初始化时的线程安全性
    static class StaticInnerSingleton {
        private StaticInnerSingleton() {}

        // 静态内部类持有外部类的实例
        private static class Holder {
            private static final StaticInnerSingleton INSTANCE = new StaticInnerSingleton();
        }

        public static StaticInnerSingleton getInstance() {
            return Holder.INSTANCE;
        }
    }

    // ==================== 方式五：枚举（最推荐） ====================
    // 优点：
    // 1. 线程安全：枚举类型由 JVM 保证线程安全
    // 2. 防止反射攻击：枚举构造器私有且无法通过反射调用
    // 3. 防止序列化破坏：枚举自带序列化机制
    // 4. 代码简洁
    //
    // Effective Java 作者推荐的方式！
    enum EnumSingleton {
        INSTANCE;

        // 可以添加方法
        public void doSomething() {
            System.out.println("单例方法执行");
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // 测试枚举单例
        EnumSingleton instance1 = EnumSingleton.INSTANCE;
        EnumSingleton instance2 = EnumSingleton.INSTANCE;
        System.out.println("枚举单例测试：" + (instance1 == instance2)); // true

        // 测试静态内部类单例
        StaticInnerSingleton s1 = StaticInnerSingleton.getInstance();
        StaticInnerSingleton s2 = StaticInnerSingleton.getInstance();
        System.out.println("静态内部类单例测试：" + (s1 == s2)); // true
    }
}
