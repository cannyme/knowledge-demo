package com.example.demo.javabase.designpatterns;

import java.util.*;

/**
 * 策略模式 (Strategy Pattern)
 *
 * 【核心思想】
 * 定义一系列算法，把它们封装起来，并使它们可以相互替换。
 * 策略模式让算法独立于使用它的客户端而变化。
 *
 * 【应用场景】
 * 1. 支付方式选择（支付宝、微信、银行卡）
 * 2. 排序算法选择
 * 3. 打折策略（满减、折扣、会员价）
 * 4. 路线规划（步行、驾车、公交）
 * 5. 数据加密方式选择
 *
 * 【优缺点】
 * 优点：开闭原则、避免if-else、算法可复用
 * 缺点：客户端需要知道所有策略、策略类增多
 */
public class StrategyPattern {

    // ==================== 场景：支付策略 ====================

    /**
     * 策略接口
     */
    interface PaymentStrategy {
        void pay(double amount);
        String getName();
    }

    /**
     * 具体策略：支付宝
     */
    static class AlipayStrategy implements PaymentStrategy {
        @Override
        public void pay(double amount) {
            System.out.println("支付宝支付：" + amount + "元");
        }

        @Override
        public String getName() {
            return "支付宝";
        }
    }

    /**
     * 具体策略：微信
     */
    static class WechatStrategy implements PaymentStrategy {
        @Override
        public void pay(double amount) {
            System.out.println("微信支付：" + amount + "元");
        }

        @Override
        public String getName() {
            return "微信";
        }
    }

    /**
     * 具体策略：银行卡
     */
    static class BankCardStrategy implements PaymentStrategy {
        @Override
        public void pay(double amount) {
            System.out.println("银行卡支付：" + amount + "元");
        }

        @Override
        public String getName() {
            return "银行卡";
        }
    }

    /**
     * 上下文类：支付处理器
     * 持有策略引用，委托给具体策略执行
     */
    static class PaymentContext {
        private PaymentStrategy strategy;

        public void setStrategy(PaymentStrategy strategy) {
            this.strategy = strategy;
        }

        public void executePayment(double amount) {
            if (strategy == null) {
                throw new IllegalStateException("未设置支付方式");
            }
            System.out.println("使用" + strategy.getName() + "支付");
            strategy.pay(amount);
        }
    }

    // ==================== 场景：排序策略 ====================

    interface SortStrategy {
        <T extends Comparable<T>> void sort(T[] arr);
        String getName();
    }

    static class QuickSortStrategy implements SortStrategy {
        @Override
        public <T extends Comparable<T>> void sort(T[] arr) {
            quickSort(arr, 0, arr.length - 1);
        }

        private <T extends Comparable<T>> void quickSort(T[] arr, int low, int high) {
            if (low < high) {
                int pivot = partition(arr, low, high);
                quickSort(arr, low, pivot - 1);
                quickSort(arr, pivot + 1, high);
            }
        }

        private <T extends Comparable<T>> int partition(T[] arr, int low, int high) {
            T pivot = arr[high];
            int i = low - 1;
            for (int j = low; j < high; j++) {
                if (arr[j].compareTo(pivot) <= 0) {
                    i++;
                    T temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }
            T temp = arr[i + 1];
            arr[i + 1] = arr[high];
            arr[high] = temp;
            return i + 1;
        }

        @Override
        public String getName() {
            return "快速排序";
        }
    }

    static class BubbleSortStrategy implements SortStrategy {
        @Override
        public <T extends Comparable<T>> void sort(T[] arr) {
            for (int i = 0; i < arr.length - 1; i++) {
                for (int j = 0; j < arr.length - 1 - i; j++) {
                    if (arr[j].compareTo(arr[j + 1]) > 0) {
                        T temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;
                    }
                }
            }
        }

        @Override
        public String getName() {
            return "冒泡排序";
        }
    }

    // ==================== 策略工厂 + 枚举优化 ====================

    /**
     * 使用枚举 + 策略模式，避免创建过多策略类
     */
    enum PaymentType implements PaymentStrategy {
        ALIPAY {
            @Override
            public void pay(double amount) {
                System.out.println("支付宝支付：" + amount + "元");
            }
            @Override
            public String getName() { return "支付宝"; }
        },
        WECHAT {
            @Override
            public void pay(double amount) {
                System.out.println("微信支付：" + amount + "元");
            }
            @Override
            public String getName() { return "微信"; }
        },
        BANK_CARD {
            @Override
            public void pay(double amount) {
                System.out.println("银行卡支付：" + amount + "元");
            }
            @Override
            public String getName() { return "银行卡"; }
        }
    }

    /**
     * 策略工厂
     */
    static class PaymentStrategyFactory {
        private static final Map<String, PaymentStrategy> STRATEGIES = new HashMap<>();

        static {
            STRATEGIES.put("alipay", new AlipayStrategy());
            STRATEGIES.put("wechat", new WechatStrategy());
            STRATEGIES.put("bank", new BankCardStrategy());
        }

        public static PaymentStrategy getStrategy(String type) {
            PaymentStrategy strategy = STRATEGIES.get(type);
            if (strategy == null) {
                throw new IllegalArgumentException("不支持的支付方式：" + type);
            }
            return strategy;
        }

        // 使用枚举
        public static PaymentStrategy getStrategy(PaymentType type) {
            return type;
        }
    }

    // ==================== 结合Spring使用 ====================

    /**
     * Spring中的策略模式应用
     *
     * 1. 定义策略接口
     * 2. 各策略实现类加 @Component 注解
     * 3. 通过 @Autowired Map<String, Strategy> 注入所有策略
     * 4. 根据类型从Map中获取策略
     */
    /*
    @Service
    public class PaymentService {
        // Spring会自动注入所有PaymentStrategy实现，key为bean名称
        private final Map<String, PaymentStrategy> strategyMap;

        public PaymentService(Map<String, PaymentStrategy> strategyMap) {
            this.strategyMap = strategyMap;
        }

        public void pay(String type, double amount) {
            PaymentStrategy strategy = strategyMap.get(type + "Strategy");
            if (strategy == null) {
                throw new IllegalArgumentException("不支持的支付方式");
            }
            strategy.pay(amount);
        }
    }
    */

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // 方式1：直接设置策略
        PaymentContext context = new PaymentContext();
        context.setStrategy(new AlipayStrategy());
        context.executePayment(100.0);

        // 方式2：使用工厂获取策略
        PaymentStrategy strategy = PaymentStrategyFactory.getStrategy("wechat");
        strategy.pay(200.0);

        // 方式3：使用枚举策略
        PaymentType.ALIPAY.pay(300.0);

        // 方式4：排序策略
        Integer[] arr = {5, 2, 8, 1, 9};
        SortStrategy sortStrategy = new QuickSortStrategy();
        System.out.println("使用" + sortStrategy.getName());
        sortStrategy.sort(arr);
        System.out.println("排序结果：" + Arrays.toString(arr));
    }
}
