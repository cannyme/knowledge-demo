package com.example.demo.javabase.jvm;

/**
 * 类加载机制
 *
 * 【类加载过程】
 * 加载 → 验证 → 准备 → 解析 → 初始化 → 使用 → 卸载
 *
 * 【类加载器】
 * Bootstrap ClassLoader（启动类加载器）
 *     ↓
 * Extension/Platform ClassLoader（扩展类加载器）
 *     ↓
 * Application ClassLoader（应用类加载器）
 *     ↓
 * 自定义类加载器
 */
public class ClassLoading {

    // ==================== 类加载过程详解 ====================

    /**
     * 1. 加载 (Loading)
     *
     * 做什么：
     * - 通过类全限定名获取.class文件（磁盘、网络、zip包等）
     * - 将字节流转为方法区的运行时数据结构
     * - 在堆中生成Class对象
     *
     * 时机：
     * - new对象
     * - 访问静态变量/方法
     * - 反射 Class.forName()
     * - 子类初始化时父类先初始化
     */

    /**
     * 2. 连接 (Linking)
     *
     * 验证 (Verification)：
     * - 文件格式验证（魔数、版本）
     * - 字节码验证
     * - 符号引用验证
     *
     * 准备 (Preparation)：
     * - 为静态变量分配内存
     * - 设置默认初始值（0、null、false）
     * - 注意：final变量直接赋值
     *
     * 示例：
     * static int a = 10;    // 准备阶段 a = 0，初始化阶段 a = 10
     * static final int B = 20; // 准备阶段 B = 20
     *
     * 解析 (Resolution)：
     * - 符号引用 → 直接引用
     * - 类/接口解析
     * - 字段解析
     * - 方法解析
     */

    /**
     * 3. 初始化 (Initialization)
     *
     * 执行类构造器 <clinit>()：
     * - 收集静态变量赋值和静态代码块
     * - 按代码顺序执行
     * - 父类<clinit>先执行
     *
     * 触发初始化的情况（主动引用）：
     * 1. new、getstatic、putstatic、invokestatic
     * 2. java.lang.reflect包的方法
     * 3. 初始化子类时先初始化父类
     * 4. 虚拟机启动时的主类
     * 5. MethodHandle/VarHandle相关
     *
     * 不会触发初始化的情况（被动引用）：
     * 1. 子类引用父类静态变量
     * 2. 通过数组定义类引用
     * 3. 常量（编译期常量进入常量池）
     */

    // 初始化示例
    static class InitDemo {
        static {
            System.out.println("InitDemo 初始化");
        }

        public static int value = 123;

        public static void method() {}
    }

    // ==================== 类加载器 ====================

    /**
     * 三种类加载器：
     *
     * ┌─────────────────────────────────────────────────────────┐
     * │ Bootstrap ClassLoader (C++实现)                         │
     * │ 加载：JAVA_HOME/lib/rt.jar, resources.jar等             │
     * │ 如：java.lang.*, java.util.*                            │
     * ├─────────────────────────────────────────────────────────┤
     * │ Platform/Extension ClassLoader (Java实现)              │
     * │ 加载：JAVA_HOME/lib/ext目录下的jar                      │
     * │ JDK 9+ 改名为 Platform ClassLoader                     │
     * ├─────────────────────────────────────────────────────────┤
     * │ Application ClassLoader (Java实现)                     │
     * │ 加载：classpath下的类                                   │
     * │ 也叫 System ClassLoader                                │
     * ├─────────────────────────────────────────────────────────┤
     * │ Custom ClassLoader                                     │
     * │ 用户自定义加载器                                        │
     * └─────────────────────────────────────────────────────────┘
     */

    public static void showClassLoaders() {
        // 获取类加载器
        ClassLoader appClassLoader = ClassLoading.class.getClassLoader();
        System.out.println("应用类加载器: " + appClassLoader);

        ClassLoader extClassLoader = appClassLoader.getParent();
        System.out.println("扩展类加载器: " + extClassLoader);

        // Bootstrap ClassLoader是C++实现的，返回null
        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        System.out.println("启动类加载器: " + bootstrapClassLoader);

        // String由启动类加载器加载
        ClassLoader stringClassLoader = String.class.getClassLoader();
        System.out.println("String的类加载器: " + stringClassLoader);
    }

    // ==================== 双亲委派模型 ====================

    /**
     * 双亲委派模型：
     *
     * 工作流程：
     * 1. 收到类加载请求
     * 2. 委派给父加载器处理
     * 3. 父加载器无法处理，自己加载
     *
     * 好处：
     * 1. 安全性：避免核心类被篡改
     *    用户无法自定义java.lang.String（会被Bootstrap加载）
     * 2. 避免重复加载：父加载器加载过的类，子加载器不会再加载
     *
     * 代码示意：
     * protected Class<?> loadClass(String name, boolean resolve) {
     *     // 1. 检查是否已加载
     *     Class<?> c = findLoadedClass(name);
     *     if (c == null) {
     *         // 2. 委派给父加载器
     *         if (parent != null) {
     *             c = parent.loadClass(name, false);
     *         } else {
     *             c = findBootstrapClassOrNull(name);
     *         }
     *
     *         // 3. 父加载器无法加载，自己加载
     *         if (c == null) {
     *             c = findClass(name);
     *         }
     *     }
     *     return c;
     * }
     */

    /**
     * 打破双亲委派：
     *
     * 1. JDK 1.0时代的自定义加载器（覆盖loadClass）
     * 2. SPI机制（JDBC、JNDI等）
     * 3. OSGi模块化
     * 4. Tomcat类加载
     *
     * SPI案例：JDBC
     * - DriverManager在rt.jar，由Bootstrap加载
     * - 但Driver实现是用户代码（classpath）
     * - Bootstrap无法加载classpath的类
     * - 解决：使用Thread.currentThread().getContextClassLoader()
     */

    // ==================== 自定义类加载器 ====================

    /**
     * 自定义类加载器步骤：
     * 1. 继承 ClassLoader
     * 2. 重写 findClass() 方法
     * 3. 调用 defineClass() 生成Class对象
     */
    static class MyClassLoader extends ClassLoader {
        private final String classPath;

        public MyClassLoader(String classPath) {
            this.classPath = classPath;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                // 读取class文件
                byte[] data = loadClassData(name);
                // 将字节码转为Class对象
                return defineClass(name, data, 0, data.length);
            } catch (Exception e) {
                throw new ClassNotFoundException(name);
            }
        }

        private byte[] loadClassData(String name) {
            // 将类名转为文件路径
            String path = classPath + "/" + name.replace('.', '/') + ".class";
            // 读取文件内容（伪代码）
            // return Files.readAllBytes(Paths.get(path));
            return new byte[0];
        }
    }

    // ==================== 类加载时机对比 ====================

    /**
     * 主动引用 vs 被动引用
     */
    static class Parent {
        static int a = 100;
        static {
            System.out.println("Parent 初始化");
        }
    }

    static class Child extends Parent {
        static {
            System.out.println("Child 初始化");
        }
    }

    public static void testInitTiming() {
        // 主动引用：会初始化Child
        // new Child();

        // 被动引用：只初始化Parent，不初始化Child
        // System.out.println(Child.a);

        // 被动引用：不会初始化任何类
        // Parent[] arr = new Parent[10];
    }

    // ==================== 运行时常量池 ====================

    /**
     * 常量池演进：
     *
     * JDK 1.6：
     * - 字符串常量池在方法区（永久代）
     *
     * JDK 1.7：
     * - 字符串常量池移到堆中
     * - intern() 不再复制对象，而是记录首次出现的引用
     *
     * JDK 1.8+：
     * - 方法区改为元空间
     */
    public static void stringPoolDemo() {
        // 创建字符串
        String s1 = new String("a");
        s1.intern(); // 将字符串放入常量池
        String s2 = "a";
        System.out.println(s1 == s2); // JDK 6: false, JDK 7+: false

        // intern示例
        String s3 = new String("b") + new String("c");
        s3.intern();
        String s4 = "bc";
        System.out.println(s3 == s4); // JDK 7+: true
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        System.out.println("=== 类加载器 ===");
        showClassLoaders();

        System.out.println("\n=== 初始化时机 ===");
        testInitTiming();

        System.out.println("\n=== 字符串常量池 ===");
        stringPoolDemo();
    }
}
