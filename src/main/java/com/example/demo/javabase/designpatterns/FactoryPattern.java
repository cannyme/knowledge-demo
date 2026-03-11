package com.example.demo.javabase.designpatterns;

/**
 * 工厂模式 (Factory Pattern)
 *
 * 【核心思想】
 * 将对象的创建与使用分离，由工厂类负责创建对象。
 *
 * 【三种形式】
 * 1. 简单工厂 (Simple Factory) - 不属于GoF 23种设计模式，但常用
 * 2. 工厂方法 (Factory Method) - 定义创建对象的接口，让子类决定实例化哪个类
 * 3. 抽象工厂 (Abstract Factory) - 创建一系列相关或依赖对象的接口
 *
 * 【应用场景】
 * 1. 日志框架：SLF4J、Log4j等
 * 2. 数据库连接：不同数据库的Connection
 * 3. Spring BeanFactory
 */
public class FactoryPattern {

    // ==================== 场景：支付方式 ====================

    // 产品接口
    interface Payment {
        void pay(double amount);
    }

    // 具体产品：支付宝支付
    static class AlipayPayment implements Payment {
        @Override
        public void pay(double amount) {
            System.out.println("支付宝支付：" + amount + "元");
        }
    }

    // 具体产品：微信支付
    static class WechatPayment implements Payment {
        @Override
        public void pay(double amount) {
            System.out.println("微信支付：" + amount + "元");
        }
    }

    // 具体产品：银行卡支付
    static class BankCardPayment implements Payment {
        @Override
        public void pay(double amount) {
            System.out.println("银行卡支付：" + amount + "元");
        }
    }

    // ==================== 方式一：简单工厂 ====================
    /**
     * 简单工厂
     *
     * 【特点】
     * - 一个工厂类创建所有产品
     * - 通过参数决定创建哪种产品
     * - 违反开闭原则（新增产品需要修改工厂类）
     *
     * 【适用场景】
     * - 产品种类较少且固定
     * - 创建逻辑简单
     */
    static class SimplePaymentFactory {
        public static Payment create(String type) {
            // Java 8 兼容写法：传统 switch 语句
            switch (type.toLowerCase()) {
                case "alipay":
                    return new AlipayPayment();
                case "wechat":
                    return new WechatPayment();
                case "bank":
                    return new BankCardPayment();
                default:
                    throw new IllegalArgumentException("不支持的支付方式：" + type);
            }
        }
    }

    // ==================== 方式二：工厂方法 ====================
    /**
     * 工厂方法模式
     *
     * 【特点】
     * - 每个产品有对应的工厂
     * - 新增产品只需新增工厂类，符合开闭原则
     * - 客户端需要知道具体工厂类
     *
     * 【适用场景】
     * - 产品种类可能频繁增加
     * - 需要遵循开闭原则
     */

    // 工厂接口
    interface PaymentFactory {
        Payment create();
    }

    // 具体工厂：支付宝
    static class AlipayFactory implements PaymentFactory {
        @Override
        public Payment create() {
            return new AlipayPayment();
        }
    }

    // 具体工厂：微信
    static class WechatFactory implements PaymentFactory {
        @Override
        public Payment create() {
            return new WechatPayment();
        }
    }

    // 具体工厂：银行卡
    static class BankCardFactory implements PaymentFactory {
        @Override
        public Payment create() {
            return new BankCardPayment();
        }
    }

    // ==================== 方式三：抽象工厂 ====================
    /**
     * 抽象工厂模式
     *
     * 【特点】
     * - 创建一系列相关的对象（产品族）
     * - 客户端不依赖具体类，依赖抽象
     *
     * 【场景示例】
     * 不同品牌的家具：椅子、桌子、沙发等
     * 不同数据库：Connection、Statement、ResultSet等
     *
     * 【产品族概念】
     * - 现代风格家具族：现代椅子 + 现代桌子
     * - 古典风格家具族：古典椅子 + 古典桌子
     */

    // 产品族：家具
    // 产品A：椅子
    interface Chair {
        void sit();
    }

    // 产品B：桌子
    interface Table {
        void put(String item);
    }

    // 现代风格产品
    static class ModernChair implements Chair {
        @Override
        public void sit() {
            System.out.println("坐在现代风格的椅子上");
        }
    }

    static class ModernTable implements Table {
        @Override
        public void put(String item) {
            System.out.println("在现代风格的桌子上放：" + item);
        }
    }

    // 古典风格产品
    static class ClassicalChair implements Chair {
        @Override
        public void sit() {
            System.out.println("坐在古典风格的椅子上");
        }
    }

    static class ClassicalTable implements Table {
        @Override
        public void put(String item) {
            System.out.println("在古典风格的桌子上放：" + item);
        }
    }

    // 抽象工厂接口
    interface FurnitureFactory {
        Chair createChair();
        Table createTable();
    }

    // 现代风格工厂
    static class ModernFurnitureFactory implements FurnitureFactory {
        @Override
        public Chair createChair() {
            return new ModernChair();
        }

        @Override
        public Table createTable() {
            return new ModernTable();
        }
    }

    // 古典风格工厂
    static class ClassicalFurnitureFactory implements FurnitureFactory {
        @Override
        public Chair createChair() {
            return new ClassicalChair();
        }

        @Override
        public Table createTable() {
            return new ClassicalTable();
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        System.out.println("===== 简单工厂测试 =====");
        Payment payment1 = SimplePaymentFactory.create("alipay");
        payment1.pay(100.0);

        System.out.println("\n===== 工厂方法测试 =====");
        PaymentFactory factory = new WechatFactory();
        Payment payment2 = factory.create();
        payment2.pay(200.0);

        System.out.println("\n===== 抽象工厂测试 =====");
        FurnitureFactory furnitureFactory = new ModernFurnitureFactory();
        Chair chair = furnitureFactory.createChair();
        Table table = furnitureFactory.createTable();
        chair.sit();
        table.put("书籍");
    }
}
