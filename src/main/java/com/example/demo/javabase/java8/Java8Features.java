package com.example.demo.javabase.java8;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Java 8+ 新特性详解
 *
 * 【核心特性】
 * 1. Lambda表达式
 * 2. 函数式接口
 * 3. Stream API
 * 4. Optional类
 * 5. 默认方法
 * 6. 方法引用
 * 7. CompletableFuture（Java 8）
 * 8. var局部变量类型推断（Java 10）
 * 9. Records（Java 14/16）
 * 10. Sealed Classes（Java 17）
 */
public class Java8Features {

    // ==================== Lambda表达式 ====================

    /**
     * Lambda语法： (参数) -> { 方法体 }
     *
     * 简化规则：
     * 1. 参数类型可省略（类型推断）
     * 2. 单参数可省略括号
     * 3. 单语句可省略花括号和return
     */
    public static void lambdaDemo() {
        // 传统写法
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                System.out.println("Hello");
            }
        };

        // Lambda写法
        Runnable r2 = () -> System.out.println("Hello Lambda");

        // 带参数
        Comparator<Integer> c1 = (a, b) -> a - b;

        // 多语句
        Comparator<Integer> c2 = (a, b) -> {
            System.out.println("比较：" + a + " 和 " + b);
            return a - b;
        };
    }

    // ==================== 函数式接口 ====================

    /**
     * Java 8内置函数式接口：
     *
     * ┌──────────────────────┬─────────────────────┬─────────────────────┐
     * │ 接口                  │ 方法                 │ 用途                 │
     * ├──────────────────────┼─────────────────────┼─────────────────────┤
     * │ Function<T,R>        │ R apply(T t)        │ 有输入有输出         │
     * │ Consumer<T>          │ void accept(T t)    │ 有输入无输出         │
     * │ Supplier<T>          │ T get()             │ 无输入有输出         │
     * │ Predicate<T>         │ boolean test(T t)   │ 判断条件             │
     * │ UnaryOperator<T>     │ T apply(T t)        │ 一元运算             │
     * │ BinaryOperator<T>    │ T apply(T t1, T t2) │ 二元运算             │
     * │ BiFunction<T,U,R>    │ R apply(T t, U u)   │ 两输入一输出         │
     * └──────────────────────┴─────────────────────┴─────────────────────┘
     */
    public static void functionalInterfaceDemo() {
        // Function：转换
        Function<String, Integer> length = String::length;
        System.out.println(length.apply("Hello")); // 5

        // Consumer：消费
        Consumer<String> print = System.out::println;
        print.accept("Hello Consumer");

        // Supplier：供给
        Supplier<Double> random = Math::random;
        System.out.println(random.get());

        // Predicate：判断
        Predicate<Integer> isEven = n -> n % 2 == 0;
        System.out.println(isEven.test(4)); // true

        // 组合使用
        Function<Integer, Integer> addOne = x -> x + 1;
        Function<Integer, Integer> multiplyTwo = x -> x * 2;

        // andThen：先执行当前，再执行参数
        Function<Integer, Integer> addThenMultiply = addOne.andThen(multiplyTwo);
        System.out.println(addThenMultiply.apply(3)); // (3+1)*2 = 8

        // compose：先执行参数，再执行当前
        Function<Integer, Integer> multiplyThenAdd = addOne.compose(multiplyTwo);
        System.out.println(multiplyThenAdd.apply(3)); // 3*2+1 = 7
    }

    // ==================== 方法引用 ====================

    /**
     * 方法引用类型：
     *
     * ┌──────────────────────────┬─────────────────────────┬─────────────────────┐
     * │ 类型                      │ 语法                     │ 示例                 │
     * ├──────────────────────────┼─────────────────────────┼─────────────────────┤
     * │ 静态方法引用              │ ClassName::staticMethod │ Math::abs           │
     * │ 实例方法引用              │ instance::method        │ "hello"::toUpperCase│
     * │ 特定类型的实例方法引用     │ ClassName::method       │ String::length      │
     * │ 构造方法引用              │ ClassName::new          │ ArrayList::new      │
     * │ 数组构造方法引用          │ TypeName[]::new         │ int[]::new          │
     * └──────────────────────────┴─────────────────────────┴─────────────────────┘
     */
    public static void methodReferenceDemo() {
        List<String> list = Arrays.asList("a", "b", "c");

        // 1. 静态方法引用
        list.forEach(System.out::println);

        // 2. 实例方法引用
        String prefix = "Item: ";
        list.forEach(s -> System.out.println(prefix + s));

        // 3. 特定类型的实例方法引用
        List<String> upper = list.stream()
            .map(String::toUpperCase)
            .collect(Collectors.toList());

        // 4. 构造方法引用
        Supplier<List<String>> listSupplier = ArrayList::new;
        Function<Integer, List<String>> listWithSize = ArrayList::new;

        // 5. 数组构造方法引用
        Function<Integer, int[]> arrayCreator = int[]::new;
        int[] arr = arrayCreator.apply(10);
    }

    // ==================== Stream API ====================

    /**
     * Stream操作分类：
     *
     * ┌─────────────────────┬─────────────────────────────────────────────────┐
     * │ 中间操作（返回Stream）│ filter, map, flatMap, distinct, sorted,        │
     * │                     │ peek, limit, skip                               │
     * ├─────────────────────┼─────────────────────────────────────────────────┤
     * │ 终止操作            │ collect, forEach, count, reduce, min, max,      │
     * │                     │ anyMatch, allMatch, noneMatch, findFirst        │
     * └─────────────────────┴─────────────────────────────────────────────────┘
     */
    public static void streamDemo() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // filter：过滤
        List<Integer> evens = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(Collectors.toList());
        System.out.println("偶数：" + evens);

        // map：映射
        List<Integer> squares = numbers.stream()
            .map(n -> n * n)
            .collect(Collectors.toList());
        System.out.println("平方：" + squares);

        // flatMap：扁平化
        List<List<Integer>> nested = Arrays.asList(
            Arrays.asList(1, 2),
            Arrays.asList(3, 4)
        );
        List<Integer> flat = nested.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        System.out.println("扁平化：" + flat);

        // sorted：排序
        List<Integer> sorted = numbers.stream()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        // distinct：去重
        List<Integer> distinct = Arrays.asList(1, 1, 2, 2, 3).stream()
            .distinct()
            .collect(Collectors.toList());

        // limit/skip：分页
        List<Integer> page = numbers.stream()
            .skip(5)   // 跳过前5个
            .limit(3)  // 取3个
            .collect(Collectors.toList());

        // peek：调试用（不改变流）
        numbers.stream()
            .peek(n -> System.out.println("处理：" + n))
            .collect(Collectors.toList());
    }

    /**
     * reduce：归约操作
     */
    public static void reduceDemo() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        // 求和
        int sum = numbers.stream()
            .reduce(0, Integer::sum);
        System.out.println("求和：" + sum);

        // 求最大值
        Optional<Integer> max = numbers.stream()
            .reduce(Integer::max);

        // 无初始值（返回Optional）
        Optional<Integer> product = numbers.stream()
            .reduce((a, b) -> a * b);

        // 复杂归约（组合器用于并行场景）
        Integer result = numbers.stream()
            .reduce(
                0,                     // 初始值
                (acc, n) -> acc + n,   // 累加器
                Integer::sum           // 组合器
            );
    }

    /**
     * Collectors：收集器
     */
    public static void collectorsDemo() {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");

        // 转List
        List<String> list = names.stream().collect(Collectors.toList());

        // 转Set
        Set<String> set = names.stream().collect(Collectors.toSet());

        // 转Map
        Map<String, Integer> map = names.stream()
            .collect(Collectors.toMap(
                name -> name,           // key
                String::length          // value
            ));

        // 分组
        Map<Integer, List<String>> groupedByLength = names.stream()
            .collect(Collectors.groupingBy(String::length));

        // 分区（按条件分两组）
        Map<Boolean, List<String>> partitioned = names.stream()
            .collect(Collectors.partitioningBy(s -> s.length() > 4));

        // 统计
        IntSummaryStatistics stats = names.stream()
            .collect(Collectors.summarizingInt(String::length));
        System.out.println("统计：" + stats);

        // 拼接字符串
        String joined = names.stream()
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("拼接：" + joined);
    }

    /**
     * 并行流
     */
    public static void parallelStreamDemo() {
        List<Integer> numbers = IntStream.range(1, 1000000).boxed().collect(Collectors.toList());

        // 串行流
        long serialSum = numbers.stream()
            .mapToLong(n -> n * n)
            .sum();

        // 并行流（自动利用多核）
        long parallelSum = numbers.parallelStream()
            .mapToLong(n -> n * n)
            .sum();

        // 注意：并行流使用ForkJoinPool.commonPool()
        // 线程数 = CPU核心数 - 1

        // 并行流适用场景：
        // 1. 数据量大
        // 2. 操作无状态
        // 3. 操作无依赖关系
    }

    // ==================== Optional类 ====================

    /**
     * Optional：解决空指针问题
     */
    public static void optionalDemo() {
        // 创建Optional
        Optional<String> opt1 = Optional.of("Hello");       // 非null
        Optional<String> opt2 = Optional.ofNullable(null);  // 可为null
        Optional<String> opt3 = Optional.empty();           // 空Optional

        // 获取值
        String value1 = opt1.get();                         // 不推荐，可能抛异常
        String value2 = opt1.orElse("默认值");              // 推荐
        String value3 = opt1.orElseGet(() -> "计算默认值");  // 懒加载
        String value4 = opt1.orElseThrow(RuntimeException::new); // 抛异常

        // 判断
        boolean present = opt1.isPresent();
        opt1.ifPresent(System.out::println);

        // 转换
        Optional<Integer> length = opt1.map(String::length);
        Optional<String> upper = opt1.map(String::toUpperCase);

        // 过滤
        Optional<String> filtered = opt1.filter(s -> s.length() > 3);

        // flatMap
        Optional<String> result = opt1.flatMap(s -> Optional.of(s + "!"));

        // 链式调用示例
        String name = getUserName(1L);
        // 传统写法
        if (name != null) {
            System.out.println(name.toUpperCase());
        }

        // Optional写法
        Optional.ofNullable(getUserName(1L))
            .map(String::toUpperCase)
            .ifPresent(System.out::println);
    }

    static String getUserName(Long id) {
        return id == 1 ? "Alice" : null;
    }

    // ==================== CompletableFuture ====================

    /**
     * CompletableFuture：异步编程
     */
    public static void completableFutureDemo() throws Exception {
        // 创建异步任务
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleep(1000);
            return "Hello";
        });

        // 获取结果
        String result1 = future1.get();              // 阻塞等待
        String result2 = future1.get(2, TimeUnit.SECONDS);  // 超时
        String result3 = future1.join();             // 阻塞，抛Unchecked异常
        String result4 = future1.getNow("默认值");   // 立即返回，未完成返回默认值

        // 链式调用
        CompletableFuture<String> chain = CompletableFuture
            .supplyAsync(() -> "Hello")
            .thenApply(s -> s + " World")        // 同步转换
            .thenApplyAsync(String::toUpperCase); // 异步转换

        // 组合多个Future
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> "!");

        // thenCombine：两个都完成后合并
        CompletableFuture<String> combined = future1.thenCombine(future2, (a, b) -> a + " " + b);

        // thenCompose：依赖链
        CompletableFuture<String> composed = future1.thenCompose(s ->
            CompletableFuture.supplyAsync(() -> s + " Composed"));

        // allOf：全部完成
        CompletableFuture<Void> all = CompletableFuture.allOf(future1, future2, future3);

        // anyOf：任一完成
        CompletableFuture<Object> any = CompletableFuture.anyOf(future1, future2, future3);

        // 异常处理
        CompletableFuture<String> withException = CompletableFuture
            .supplyAsync(() -> {
                if (true) throw new RuntimeException("异常");
                return "OK";
            })
            .exceptionally(ex -> "异常恢复：" + ex.getMessage())
            .handle((result, ex) -> {
                if (ex != null) return "处理异常";
                return result;
            });

        // 指定线程池
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletableFuture<String> withExecutor = CompletableFuture.supplyAsync(() -> {
            return "使用自定义线程池";
        }, executor);
    }

    static void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { }
    }

    // ==================== Java 10+ var ====================

    /**
     * var：局部变量类型推断（Java 10+）
     *
     * Java 8 替代方案：显式声明类型
     */
    public static void varDemo() {
        // Java 8 需要显式声明类型
        ArrayList<String> list = new ArrayList<String>();
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        java.util.stream.Stream<String> stream = list.stream();

        // 循环中使用
        for (String item : list) {
            System.out.println(item);
        }

        // try-with-resources
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("file.txt"))) {
            String line = reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 注意：var只能用于局部变量，不能用于字段、方法参数、方法返回值
    }

    // ==================== Java 14+ Records ====================

    /**
     * Record：不可变数据类（Java 14预览，Java 16正式）
     *
     * Java 8 替代方案：使用 final 类 + 手动实现
     */
    public static final class Person {
        private final String name;
        private final int age;

        // 构造器
        public Person(String name, int age) {
            if (age < 0) {
                throw new IllegalArgumentException("年龄不能为负");
            }
            this.name = name;
            this.age = age;
        }

        // getter
        public String name() { return name; }
        public int age() { return age; }

        // 业务方法
        public boolean isAdult() {
            return age >= 18;
        }

        // 静态工厂方法
        public static Person of(String name, int age) {
            return new Person(name, age);
        }

        // equals、hashCode、toString
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && name.equals(person.name);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + age;
        }

        @Override
        public String toString() {
            return "Person[name=" + name + ", age=" + age + "]";
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) throws Exception {
        System.out.println("=== 函数式接口 ===");
        functionalInterfaceDemo();

        System.out.println("\n=== Stream API ===");
        streamDemo();

        System.out.println("\n=== Collectors ===");
        collectorsDemo();

        System.out.println("\n=== Optional ===");
        optionalDemo();

        System.out.println("\n=== Record ===");
        Person person = new Person("张三", 25);
        System.out.println(person);
        System.out.println("是否成年：" + person.isAdult());
    }
}
