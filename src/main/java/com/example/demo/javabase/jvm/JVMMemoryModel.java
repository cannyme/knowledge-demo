package com.example.demo.javabase.jvm;

import java.lang.ref.*;
import java.util.*;

/**
 * JVM内存模型与垃圾回收
 *
 * 【JVM整体结构】
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                         JVM 运行时数据区                         │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
 * │  │   堆 (Heap)  │  │ 方法区/Metaspace│  │    线程私有区域          │ │
 * │  │             │  │             │  │  ┌──────────────────┐   │ │
 * │  │  新生代      │  │  类信息       │  │  │ 虚拟机栈          │   │ │
 * │  │  ├─Eden     │  │  常量池       │  │  │ (每个方法一个栈帧)  │   │ │
 * │  │  ├─S0      │  │  静态变量     │  │  └──────────────────┘   │ │
 * │  │  └─S1      │  │             │  │  ┌──────────────────┐   │ │
 * │  │  老年代     │  │             │  │  │ 本地方法栈        │   │ │
 * │  │             │  │             │  │  └──────────────────┘   │ │
 * │  └─────────────┘  └─────────────┘  │  ┌──────────────────┐   │ │
 * │                                     │  │ 程序计数器(PC)     │   │ │
 * │                                     │  └──────────────────┘   │ │
 * │                                     └─────────────────────────┘ │
 * └─────────────────────────────────────────────────────────────────┘
 */
public class JVMMemoryModel {

    // ==================== 运行时数据区详解 ====================

    /**
     * 1. 堆 (Heap)
     *
     * - 所有线程共享
     * - 存储对象实例
     * - GC的主要区域
     *
     * 分代结构：
     * ┌─────────────────────────────────────┐
     * │              堆 (Heap)              │
     * ├───────────────────┬─────────────────┤
     * │     新生代 (1/3)   │   老年代 (2/3)   │
     * ├─────┬─────┬───────┤                 │
     * │Eden │ S0  │ S1    │                 │
     * │ 8   │ 1   │ 1     │                 │
     * └─────┴─────┴───────┴─────────────────┘
     *
     * 对象分配流程：
     * 1. 新对象先在Eden分配
     * 2. Eden满，触发Minor GC
     * 3. 存活对象复制到Survivor区
     * 4. Survivor区对象年龄达到阈值（默认15），晋升老年代
     * 5. 大对象直接进入老年代
     */

    /**
     * 2. 方法区 / Metaspace
     *
     * JDK 1.7及之前：方法区（永久代），JVM内存
     * JDK 1.8及之后：Metaspace（元空间），本地内存
     *
     * 存储内容：
     * - 类信息（类名、修饰符、字段、方法）
     * - 运行时常量池
     * - 静态变量
     * - JIT编译后的代码
     *
     * 常量池演进：
     * - JDK 1.6：字符串常量池在方法区
     * - JDK 1.7：字符串常量池移到堆中
     * - JDK 1.8：方法区改为元空间
     *
     * Metaspace优势：
     * - 使用本地内存，不占用JVM堆内存
     * - 默认无上限，防止内存泄漏导致OOM
     */

    /**
     * 3. 虚拟机栈 (VM Stack)
     *
     * - 线程私有
     * - 每个方法调用创建一个栈帧
     * - 栈帧包含：局部变量表、操作数栈、动态链接、返回地址
     *
     * 栈帧结构：
     * ┌─────────────────────────────┐
     * │      当前栈帧               │
     * ├─────────────────────────────┤
     * │ 局部变量表                  │
     * │  [0] this                  │
     * │  [1] param1                │
     * │  [2] localVar              │
     * ├─────────────────────────────┤
     * │ 操作数栈                    │
     * │  (计算过程中的中间结果)       │
     * ├─────────────────────────────┤
     * │ 动态链接 (指向运行时常量池)   │
     * ├─────────────────────────────┤
     * │ 返回地址                    │
     * └─────────────────────────────┘
     *
     * StackOverflowError：递归过深
     * OutOfMemoryError：无法创建新线程（栈空间不足）
     */

    /**
     * 4. 程序计数器 (PC Register)
     *
     * - 线程私有
     * - 记录当前执行的字节码指令地址
     * - 唯一没有OOM的区域
     */

    // ==================== 对象内存布局 ====================
    /**
     * 对象在堆中的存储结构：
     *
     * ┌─────────────────────────────────────┐
     * │           对象头 (Header)           │
     * ├─────────────────────────────────────┤
     * │ Mark Word (32/64位)                 │
     * │  - 哈希码                           │
     * │  - GC分代年龄                        │
     * │  - 锁状态标志                        │
     * │  - 偏向线程ID                        │
     * ├─────────────────────────────────────┤
     * │ 类型指针 (Class Pointer)             │
     * │  - 指向类元数据                       │
     * ├─────────────────────────────────────┤
     * │ 实例数据 (Instance Data)            │
     * │  - 字段值                           │
     * ├─────────────────────────────────────┤
     * │ 对齐填充 (Padding)                  │
     * │  - 保证8字节对齐                      │
     * └─────────────────────────────────────┘
     *
     * 指针压缩（-XX:+UseCompressedOops）：
     * - 64位JVM默认开启
     * - 将64位指针压缩为32位
     * - 减少内存占用
     */

    // ==================== 垃圾回收算法 ====================
    /**
     * 1. 判断对象是否存活
     *
     * 引用计数法（Python使用）：
     * - 问题：无法解决循环引用
     *
     * 可达性分析（Java使用）：
     * - 从GC Roots出发，遍历引用链
     * - 不可达的对象即为垃圾
     *
     * GC Roots包括：
     * 1. 虚拟机栈中的引用
     * 2. 方法区静态属性引用
     * 3. 方法区常量引用
     * 4. 本地方法栈JNI引用
     * 5. 同步锁持有的对象
     */

    /**
     * 2. 垃圾回收算法
     *
     * ┌──────────────┬─────────────────────────────────────────┐
     * │ 算法          │ 说明                                     │
     * ├──────────────┼─────────────────────────────────────────┤
     * │ 标记-清除     │ 效率低，内存碎片                          │
     * │ 标记-复制     │ 适合新生代，浪费一半空间                   │
     * │ 标记-整理     │ 适合老年代，效率低                        │
     * │ 分代收集      │ 新生代用复制，老年代用整理                 │
     * └──────────────┴─────────────────────────────────────────┘
     */

    // ==================== 垃圾收集器 ====================
    /**
     * 收集器对比：
     *
     * ┌──────────────┬─────────┬─────────┬─────────┬─────────────────────┐
     * │ 收集器        │ 新生代   │ 老年代   │ 特点     │ 适用场景             │
     * ├──────────────┼─────────┼─────────┼─────────┼─────────────────────┤
     * │ Serial       │ 复制    │ 整理    │ 单线程   │ 客户端模式           │
     * │ ParNew       │ 复制    │ -       │ 多线程   │ 配合CMS              │
     * │ Parallel Scavenge│ 复制 │ -    │ 吞吐量优先│ 后台计算             │
     * │ CMS          │ -       │ 标记清除 │ 低延迟   │ 互联网应用（已废弃）  │
     * │ G1           │ 分区+复制│ 分区+整理│ 可预测停顿│ 大内存服务端（推荐） │
     * │ ZGC          │ 分区    │ 分区    │ 超低延迟 │ JDK 15+生产可用      │
     * │ Shenandoah   │ 分区    │ 分区    │ 超低延迟 │ OpenJDK             │
     * └──────────────┴─────────┴─────────┴─────────┴─────────────────────┘
     */

    /**
     * G1收集器详解
     *
     * 特点：
     * 1. 把堆分成多个大小相等的Region
     * 2. 仍然保留分代概念
     * 3. 可预测停顿时间模型
     * 4. 无内存碎片
     *
     * Region分区：
     * ┌───┬───┬───┬───┬───┬───┬───┬───┐
     * │ E │ E │ S │   │ O │ O │ O │ H │
     * └───┴───┴───┴───┴───┴───┴───┴───┘
     * E: Eden  S: Survivor  O: Old  H: Humongous
     *
     * 工作模式：
     * 1. Young GC：Eden区满时触发
     * 2. 并发标记：老年代占用达到阈值
     * 3. Mixed GC：回收部分老年代Region
     * 4. Full GC：兜底（应避免）
     */

    /**
     * ZGC收集器（JDK 15+）
     *
     * 目标：停顿时间不超过10ms
     *
     * 核心技术：
     * 1. 着色指针（Colored Pointer）
     * 2. 读屏障（Load Barrier）
     * 3. 多重映射（Multi-Mapping）
     *
     * 优势：
     * - 停顿时间不随堆大小增加而增加
     * - 支持TB级堆内存
     */

    // ==================== GC日志分析 ====================
    /**
     * 常用参数：
     *
     * -XX:+PrintGCDetails        打印GC详情
     * -XX:+PrintGCDateStamps     打印时间戳
     * -Xloggc:gc.log             输出到文件
     * -XX:+PrintTenuringDistribution 打印年龄分布
     *
     * JDK 9+：
     * -Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10m
     */

    /**
     * GC日志示例解读：
     *
     * [GC (Allocation Failure) [PSYoungGen: 6144K->512K(7168K)]
     *  6144K->1024K(25600K), 0.0012345 secs]
     *
     * 解读：
     * - GC类型：Minor GC
     * - 触发原因：分配失败
     * - 新生代：6144K → 512K，总容量7168K
     * - 整堆：6144K → 1024K，总容量25600K
     * - 耗时：0.0012345秒
     */

    // ==================== 内存泄漏案例 ====================

    /**
     * 案例1：静态集合
     * 静态集合的生命周期与程序相同，会导致对象无法回收
     */
    static class StaticCollectionLeak {
        // ❌ 危险：静态Map会一直增长
        // private static final Map<String, Object> CACHE = new HashMap<>();

        // ✅ 正确：使用WeakHashMap或设置过期时间
        private static final Map<String, Object> CACHE = new WeakHashMap<>();
    }

    /**
     * 案例2：未关闭的资源
     */
    static class ResourceLeak {
        public void readFile(String path) {
            // ❌ 危险：连接未关闭
            // Scanner scanner = new Scanner(new File(path));

            // ✅ 正确：try-with-resources
            // try (Scanner scanner = new Scanner(new File(path))) {
            //     // 使用scanner
            // }
        }
    }

    /**
     * 案例3：ThreadLocal未清理
     */
    static class ThreadLocalLeak {
        private static final ThreadLocal<byte[]> THREAD_LOCAL = new ThreadLocal<>();

        public void process() {
            THREAD_LOCAL.set(new byte[1024 * 1024]);

            // ❌ 危险：线程池场景下，线程不销毁，value无法回收

            // ✅ 正确：使用完毕后清理
            try {
                // 业务逻辑
            } finally {
                THREAD_LOCAL.remove();
            }
        }
    }

    // ==================== 引用类型 ====================
    /**
     * Java四种引用类型：
     *
     * ┌──────────────┬─────────────────────────────────────────┐
     * │ 引用类型      │ 说明                                     │
     * ├──────────────┼─────────────────────────────────────────┤
     * │ 强引用        │ 永不回收（除非不可达）                     │
     * │ 软引用        │ 内存不足时回收（适合缓存）                  │
     * │ 弱引用        │ 下次GC时回收（适合WeakHashMap）            │
     * │ 虚引用        │ 无法通过引用获取对象，用于跟踪GC            │
     * └──────────────┴─────────────────────────────────────────┘
     */
    public static void referenceDemo() {
        // 强引用
        Object strongRef = new Object();

        // 软引用
        SoftReference<byte[]> softRef = new SoftReference<>(new byte[1024 * 1024]);
        byte[] data = softRef.get(); // 获取对象
        // 内存不足时，softRef.get()可能返回null

        // 弱引用
        WeakReference<Object> weakRef = new WeakReference<>(new Object());
        // GC后，weakRef.get()返回null

        // 虚引用（必须配合引用队列使用）
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        PhantomReference<Object> phantomRef = new PhantomReference<>(new Object(), queue);
        // phantomRef.get() 永远返回null
        // 通过queue.poll()可以知道对象何时被回收
    }

    // ==================== JVM调优常用参数 ====================
    /**
     * 内存设置：
     * -Xms              初始堆大小
     * -Xmx              最大堆大小
     * -Xmn              新生代大小
     * -XX:MetaspaceSize 元空间初始大小
     * -XX:MaxMetaspaceSize 元空间最大大小
     * -XX:SurvivorRatio=8 Eden:Survivor比例
     * -XX:NewRatio=2     新生代:老年代比例
     *
     * GC选择：
     * -XX:+UseSerialGC      使用Serial + Serial Old
     * -XX:+UseParallelGC    使用Parallel Scavenge + Parallel Old（默认）
     * -XX:+UseG1GC          使用G1（推荐）
     * -XX:+UseZGC           使用ZGC
     *
     * G1参数：
     * -XX:MaxGCPauseMillis=200 最大停顿时间目标
     * -XX:G1HeapRegionSize=4m  Region大小
     * -XX:InitiatingHeapOccupancyPercent=45 触发并发标记的堆占用率
     *
     * OOM处理：
     * -XX:+HeapDumpOnOutOfMemoryError  OOM时dump堆
     * -XX:HeapDumpPath=/tmp/heap.hprof dump文件路径
     */

    // ==================== 排查工具 ====================
    /**
     * 命令行工具：
     * jps       查看Java进程
     * jstat     查看GC统计
     * jmap      生成堆转储
     * jstack    打印线程栈
     * jinfo     查看JVM参数
     * jcmd      多功能命令
     *
     * GUI工具：
     * JConsole   JDK自带监控工具
     * VisualVM   JDK自带分析工具
     * JMC        Java Mission Control
     * Arthas     阿里开源诊断工具
     *
     * 示例命令：
     * jstat -gcutil <pid> 1000  每秒打印GC统计
     * jmap -histo <pid> | head -20  查看对象统计（前20）
     * jmap -dump:format=b,file=heap.hprof <pid>  dump堆
     * jstack <pid> > thread.txt  打印线程栈
     */

    public static void main(String[] args) {
        // 查看JVM参数
        Runtime runtime = Runtime.getRuntime();
        System.out.println("最大内存: " + runtime.maxMemory() / 1024 / 1024 + " MB");
        System.out.println("总内存: " + runtime.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("空闲内存: " + runtime.freeMemory() / 1024 / 1024 + " MB");
        System.out.println("处理器数: " + runtime.availableProcessors());
    }
}
