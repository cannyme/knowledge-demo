package com.example.demo.csbasics.datastructure;

import java.util.*;
import java.util.concurrent.*;

/**
 * HashMap源码分析
 *
 * 【核心问题】
 * 1. 底层数据结构是什么？
 * 2. 如何计算数组下标？
 * 3. 扩容机制是怎样的？
 * 4. 为什么JDK8要引入红黑树？
 * 5. HashMap为什么线程不安全？
 */
public class HashMapSource {

    // ==================== JDK 1.7 实现 ====================
    /**
     * JDK 1.7 的 HashMap：数组 + 链表
     *
     * 结构示意：
     * Entry[] table
     * ┌───┐
     * │ 0 │ → Entry → Entry → Entry   (链表)
     * ├───┤
     * │ 1 │ → Entry
     * ├───┤
     * │ 2 │ → null
     * ├───┤
     * │...│
     * └───┘
     *
     * 问题：
     * 1. 链表过长时，查询效率退化到O(n)
     * 2. 并发扩容时，链表可能形成死循环（JDK7特有）
     */

    // ==================== JDK 1.8 实现 ====================
    /**
     * JDK 1.8 的 HashMap：数组 + 链表 + 红黑树
     *
     * 结构示意：
     * Node[] table
     * ┌───┐
     * │ 0 │ → Node → Node → Node → Node → Node → Node → Node → Node (≥8个转为红黑树)
     * ├───┤          ↓
     * │ 1 │     TreeNode (红黑树节点)
     * ├───┤
     * │ 2 │ → null
     * └───┘
     *
     * 关键参数：
     * - DEFAULT_INITIAL_CAPACITY = 16：默认初始容量
     * - MAXIMUM_CAPACITY = 1 << 30：最大容量
     * - DEFAULT_LOAD_FACTOR = 0.75f：默认负载因子
     * - TREEIFY_THRESHOLD = 8：链表转红黑树阈值
     * - UNTREEIFY_THRESHOLD = 6：红黑树转链表阈值
     * - MIN_TREEIFY_CAPACITY = 64：转红黑树的最小容量
     */

    // ==================== 关键源码解析 ====================

    /**
     * 1. 计算hash值
     *
     * 为什么是 (h = key.hashCode()) ^ (h >>> 16)？
     *
     * 原因：让高16位也参与运算，减少hash冲突
     *
     * 假设 n = 16，下标计算为 (n-1) & hash
     * 如果不扰动，高16位完全没用到
     *
     * 示例：
     * hashCode = 0000 0000 0000 0000 0000 0000 0000 1101
     * h >>> 16 = 0000 0000 0000 0000
     * 异或后    = 0000 0000 0000 0000 0000 0000 0000 1101
     * （此例高16位全0，效果不明显，但对于高16位不为0的hashCode有效）
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 2. 计算数组下标
     *
     * index = (n - 1) & hash
     *
     * 为什么用 & 而不是 %？
     * - & 运算比 % 快
     * - 要求 n 是2的幂，这样 (n-1) 的二进制全是1
     *
     * 例如 n = 16：
     * n - 1 = 15 = 0000 1111
     * hash & 15 只保留低4位，结果范围 0-15
     *
     * 这也是为什么HashMap容量必须是2的幂！
     */

    /**
     * 3. put方法流程
     *
     * put(key, value)：
     * 1. 计算key的hash值
     * 2. 如果table为空或长度为0，进行扩容
     * 3. 计算下标：(n-1) & hash
     * 4. 如果该位置为空，直接放入
     * 5. 如果不为空：
     *    a. 相同key，覆盖value
     *    b. 是TreeNode，红黑树插入
     *    c. 是链表，遍历插入：
     *       - 遍历过程中发现有相同key，覆盖
     *       - 到达链表尾部，插入新节点
     *       - 链表长度 ≥ 8，转换为红黑树
     * 6. size++，检查是否需要扩容
     */

    /**
     * 4. 扩容机制
     *
     * 扩容时机：size > capacity * loadFactor
     * 扩容后：容量翻倍
     *
     * JDK 1.8 优化：
     * 扩容时，元素的新位置要么是原位置，要么是原位置 + 原容量
     *
     * 原理：
     * 假设原容量 n=16，新容量 32
     * hash & 15  → 原位置
     * hash & 31  → 新位置
     * 31 = 16 + 15，即多判断了一位
     *
     * 如果hash的那一位是0，位置不变
     * 如果是1，新位置 = 原位置 + 16
     *
     * 这样可以均匀分配元素到新旧两个位置
     */

    // ==================== HashMap线程不安全问题 ====================
    /**
     * 为什么线程不安全？
     *
     * 1. JDK 1.7：扩容时链表可能形成死循环
     *    原因：头插法 + 并发扩容
     *    修复：JDK 1.8 改用尾插法
     *
     * 2. JDK 1.8：数据覆盖问题
     *    场景：
     *    - 线程A判断位置为空，准备插入
     *    - 线程B也判断位置为空，插入成功
     *    - 线程A继续执行，覆盖了线程B的数据
     *
     * 3. size++ 非原子操作
     */

    // ==================== 线程安全的替代方案 ====================

    /**
     * 1. ConcurrentHashMap（推荐）
     *
     * JDK 1.7：分段锁（Segment）
     * - 将数据分成多个段，每段一把锁
     * - 并发度 = Segment数量
     *
     * JDK 1.8：CAS + synchronized
     * - 只锁链表头/树根
     * - 更细粒度的锁
     * - 性能更好
     */
    // ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    /**
     * 2. Collections.synchronizedMap
     *
     * 原理：对每个方法加 synchronized
     * 性能差，不推荐
     */
    // Map<String, String> syncMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * 3. HashTable
     *
     * 原理：所有方法加 synchronized
     * 性能最差，已过时，不推荐
     */

    // ==================== HashMap 使用注意事项 ====================
    /**
     * 1. 初始容量设置
     *
     * 如果已知元素数量，建议设置初始容量，避免扩容
     *
     * 公式：initialCapacity = (int) (元素数量 / 0.75) + 1
     *
     * 例：需要存1000个元素
     * 1000 / 0.75 + 1 = 1334
     * 设置 new HashMap<>(1334) 或 new HashMap<>(2048)（取最近的2的幂）
     */

    /**
     * 2. 自定义对象作为Key
     *
     * 要求：正确重写 hashCode() 和 equals()
     *
     * 原则：
     * - equals相等，hashCode必须相等
     * - hashCode相等，equals不一定相等
     *
     * 为什么？
     * - 先用hashCode定位数组下标
     * - 再用equals比较链表/树中的元素
     */
    static class Person {
        private String name;
        private int age;

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Person person = (Person) obj;
            return age == person.age && Objects.equals(name, person.name);
        }
    }

    /**
     * 3. 负载因子选择
     *
     * 默认 0.75 是空间和时间的权衡：
     * - 太小：空间浪费
     * - 太大：冲突增多，查询效率下降
     *
     * 特殊场景：
     * - 内存紧张、查询少：可设为 0.8-0.9
     * - 查询频繁、内存充足：可设为 0.6-0.7
     */

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // 设置初始容量
        Map<String, Integer> map = new HashMap<>(16);
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        System.out.println("hash(\"a\") = " + hash("a"));
        System.out.println("index = " + ((16 - 1) & hash("a")));

        // 遍历方式
        // 1. entrySet
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        // 2. forEach (Java 8+)
        map.forEach((k, v) -> System.out.println(k + " = " + v));

        // 3. getOrDefault
        System.out.println(map.getOrDefault("d", 0));

        // 4. computeIfAbsent
        map.computeIfAbsent("e", k -> 5);
    }
}
