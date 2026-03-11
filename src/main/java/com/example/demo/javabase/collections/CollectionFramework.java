package com.example.demo.javabase.collections;

import java.util.*;
import java.util.concurrent.*;

/**
 * Java集合框架
 *
 * 【整体架构】
 * Collection（单值）
 * ├── List（有序、可重复）
 * │   ├── ArrayList：数组实现，随机访问快
 * │   ├── LinkedList：链表实现，插入删除快
 * │   └── Vector：线程安全的ArrayList（已过时）
 * │
 * ├── Set（无序、不可重复）
 * │   ├── HashSet：哈希表实现
 * │   ├── LinkedHashSet：保留插入顺序
 * │   └── TreeSet：红黑树实现，有序
 * │
 * └── Queue（队列）
 *     ├── LinkedList：普通队列
 *     ├── PriorityQueue：优先队列（堆）
 *     └── ArrayDeque：双端队列
 *
 * Map（键值对）
 * ├── HashMap：哈希表，线程不安全
 * ├── LinkedHashMap：保留插入/访问顺序
 * ├── TreeMap：红黑树，有序
 * ├── ConcurrentHashMap：线程安全的HashMap
 * └── Hashtable：线程安全（已过时）
 */
public class CollectionFramework {

    // ==================== ArrayList ====================
    /**
     * 底层：动态数组
     *
     * 特点：
     * 1. 随机访问O(1)，插入删除O(n)
     * 2. 默认容量10，扩容1.5倍
     * 3. 非线程安全
     *
     * 扩容机制：
     * int newCapacity = oldCapacity + (oldCapacity >> 1);
     */
    public static void arrayListDemo() {
        // 指定初始容量，避免扩容
        List<Integer> list = new ArrayList<>(100);

        // 批量添加
        list.addAll(Arrays.asList(1, 2, 3, 4, 5));

        // 遍历方式
        // 1. for-each
        for (Integer i : list) {
            System.out.println(i);
        }

        // 2. 迭代器
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) {
            System.out.println(it.next());
        }

        // 3. 索引遍历（适合随机访问）
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }

        // 4. forEach + lambda
        list.forEach(System.out::println);

        // 注意：遍历时删除元素
        // ❌ 错误：ConcurrentModificationException
        // for (Integer i : list) {
        //     if (i == 3) list.remove(i);
        // }

        // ✅ 正确：使用迭代器的remove
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == 3) {
                iterator.remove();
            }
        }

        // ✅ 正确：使用removeIf
        list.removeIf(i -> i == 3);
    }

    // ==================== LinkedList ====================
    /**
     * 底层：双向链表
     *
     * 特点：
     * 1. 插入删除O(1)，随机访问O(n)
     * 2. 内存开销比ArrayList大（存储前后指针）
     *
     * 适用场景：
     * - 频繁在头部/中间插入删除
     * - 实现队列/栈
     */
    public static void linkedListDemo() {
        LinkedList<Integer> list = new LinkedList<>();

        // 作为队列使用
        list.offer(1);      // 入队
        list.poll();        // 出队
        list.peek();        // 查看队首

        // 作为栈使用
        list.push(1);       // 入栈
        list.pop();         // 出栈

        // 作为双端队列
        list.addFirst(1);
        list.addLast(2);
        list.removeFirst();
        list.removeLast();
    }

    // ==================== HashMap ====================
    /**
     * 详见 HashMapSource.java
     */
    public static void hashMapDemo() {
        Map<String, Integer> map = new HashMap<>();

        map.put("a", 1);
        map.putIfAbsent("b", 2);     // 不存在才放入
        map.computeIfAbsent("c", k -> 3); // 不存在时计算
        map.compute("a", (k, v) -> v == null ? 0 : v + 1); // 更新
        map.merge("a", 1, Integer::sum); // 合并

        // 遍历
        map.forEach((k, v) -> System.out.println(k + "=" + v));
    }

    // ==================== LinkedHashMap ====================
    /**
     * 特点：保留插入顺序或访问顺序
     *
     * 应用场景：
     * 1. LRU缓存
     * 2. 需要保持顺序的Map
     */
    public static void linkedHashMapDemo() {
        // 访问顺序
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(16, 0.75f, true);
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        map.get("a"); // 访问后，a移到最后

        // LRU缓存实现
        LinkedHashMap<String, Integer> lru = new LinkedHashMap<String, Integer>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                return size() > 100; // 超过100个删除最老的
            }
        };
    }

    // ==================== TreeMap ====================
    /**
     * 底层：红黑树
     *
     * 特点：
     * 1. 按key排序（自然顺序或自定义比较器）
     * 2. 查找、插入、删除都是O(logn)
     *
     * 应用场景：
     * 1. 需要有序遍历
     * 2. 范围查询
     */
    public static void treeMapDemo() {
        TreeMap<Integer, String> map = new TreeMap<>();

        map.put(3, "c");
        map.put(1, "a");
        map.put(2, "b");

        // 自动排序
        map.keySet(); // [1, 2, 3]

        // 范围查询
        map.subMap(1, 3);      // [1, 2]
        map.headMap(2);        // [1]
        map.tailMap(2);        // [2, 3]

        // 获取最值
        map.firstKey();        // 1
        map.lastKey();         // 3
        map.lowerKey(2);       // 1（小于2的最大key）
        map.higherKey(2);      // 3（大于2的最小key）
    }

    // ==================== HashSet ====================
    /**
     * 底层：HashMap
     *
     * 特点：
     * 1. 不允许重复
     * 2. 无序
     * 3. 可以存null
     */
    public static void hashSetDemo() {
        Set<String> set = new HashSet<>();
        set.add("a");
        set.add("b");
        set.contains("a");  // O(1)

        // 去重
        List<Integer> list = Arrays.asList(1, 2, 2, 3, 3, 3);
        Set<Integer> unique = new HashSet<>(list);
    }

    // ==================== TreeSet ====================
    /**
     * 底层：TreeMap（红黑树）
     *
     * 特点：有序、不重复
     */
    public static void treeSetDemo() {
        TreeSet<Integer> set = new TreeSet<>();
        set.add(3);
        set.add(1);
        set.add(2);

        // 有序遍历
        set.first();           // 1
        set.last();            // 3
        set.lower(2);          // 1
        set.higher(2);         // 3
        set.subSet(1, 3);      // [1, 2]
    }

    // ==================== PriorityQueue ====================
    /**
     * 底层：最小堆
     *
     * 特点：
     * 1. 最小元素在队首
     * 2. 入队出队O(logn)
     */
    public static void priorityQueueDemo() {
        // 默认最小堆
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();
        minHeap.offer(3);
        minHeap.offer(1);
        minHeap.offer(2);
        minHeap.poll(); // 1

        // 最大堆
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);
        maxHeap.offer(3);
        maxHeap.offer(1);
        maxHeap.offer(2);
        maxHeap.poll(); // 3

        // Top K 问题
        int[] arr = {1, 5, 2, 8, 3};
        PriorityQueue<Integer> heap = new PriorityQueue<>();
        for (int num : arr) {
            heap.offer(num);
            if (heap.size() > 3) {
                heap.poll(); // 移除最小的
            }
        }
        // heap中保留最大的3个
    }

    // ==================== ConcurrentHashMap ====================
    /**
     * 线程安全的HashMap
     *
     * JDK 1.7：分段锁（Segment）
     * JDK 1.8：CAS + synchronized（锁链表头/树根）
     */
    public static void concurrentHashMapDemo() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        // 原子操作
        map.putIfAbsent("a", 1);
        map.computeIfAbsent("b", k -> 2);
        map.merge("a", 1, Integer::sum);

        // 批量操作
        map.forEach((k, v) -> System.out.println(k + "=" + v));

        // 并发计数
        map.reduceValues(1, Integer::sum);
    }

    // ==================== CopyOnWriteArrayList ====================
    /**
     * 写时复制
     *
     * 原理：
     * - 写操作时，复制一份新数组，在新数组上修改
     * - 读操作不需要加锁
     *
     * 适用场景：
     * - 读多写少
     * - 遍历操作远多于修改操作
     */
    public static void copyOnWriteDemo() {
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        list.add(1);

        // 遍历时可以修改，不会ConcurrentModificationException
        for (Integer i : list) {
            list.add(2); // 允许，但遍历的看不到新元素
        }
    }

    // ==================== 集合选择指南 ====================
    /**
     * ┌──────────────────────┬─────────────────────────────────┐
     * │ 场景                  │ 推荐集合                         │
     * ├──────────────────────┼─────────────────────────────────┤
     * │ 随机访问频繁          │ ArrayList                       │
     * │ 头部/中间频繁增删     │ LinkedList                      │
     * │ 去重                  │ HashSet                         │
     * │ 去重且有序            │ LinkedHashSet / TreeSet         │
     * │ 键值对存储            │ HashMap                         │
     * │ 键值对且有序          │ LinkedHashMap / TreeMap         │
     * │ 多线程环境            │ ConcurrentHashMap               │
     * │ 读多写少的并发场景    │ CopyOnWriteArrayList            │
     * │ 优先级队列            │ PriorityQueue                   │
     * └──────────────────────┴─────────────────────────────────┘
     */
}
