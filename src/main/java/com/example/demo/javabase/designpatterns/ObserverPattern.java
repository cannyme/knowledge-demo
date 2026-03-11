package com.example.demo.javabase.designpatterns;

import java.util.*;

/**
 * 观察者模式 (Observer Pattern)
 *
 * 【核心思想】
 * 定义对象间的一对多依赖关系，当一个对象状态改变时，
 * 所有依赖它的对象都会收到通知并自动更新。
 *
 * 【应用场景】
 * 1. 事件驱动系统（GUI事件、按钮点击）
 * 2. 消息推送（群聊、公众号）
 * 3. 日志监控（日志变更通知）
 * 4. 数据同步（缓存更新通知）
 * 5. Spring事件机制（ApplicationEvent）
 *
 * 【角色】
 * Subject（主题）：被观察者，维护观察者列表
 * Observer（观察者）：定义更新接口
 * ConcreteSubject：具体主题
 * ConcreteObserver：具体观察者
 */
public class ObserverPattern {

    // ==================== 基础实现 ====================

    /**
     * 观察者接口
     */
    interface Observer {
        void update(String message);
    }

    /**
     * 主题接口
     */
    interface Subject {
        void attach(Observer observer);
        void detach(Observer observer);
        void notifyObservers(String message);
    }

    /**
     * 具体主题：公众号
     */
    static class OfficialAccount implements Subject {
        private final List<Observer> observers = new ArrayList<>();
        private final String name;

        public OfficialAccount(String name) {
            this.name = name;
        }

        @Override
        public void attach(Observer observer) {
            observers.add(observer);
            System.out.println("新用户关注了：" + name);
        }

        @Override
        public void detach(Observer observer) {
            observers.remove(observer);
            System.out.println("用户取关了：" + name);
        }

        @Override
        public void notifyObservers(String message) {
            System.out.println("\n=== " + name + " 发布新文章 ===");
            for (Observer observer : observers) {
                observer.update(message);
            }
        }

        public void publishArticle(String title) {
            notifyObservers(title);
        }
    }

    /**
     * 具体观察者：用户
     */
    static class User implements Observer {
        private final String name;

        public User(String name) {
            this.name = name;
        }

        @Override
        public void update(String message) {
            System.out.println(name + " 收到推送：" + message);
        }
    }

    // ==================== JDK内置观察者 ====================

    /**
     * JDK提供的观察者模式支持（已过时，仅作了解）
     *
     * java.util.Observable - 被观察者（类，不是接口，限制了复用）
     * java.util.Observer - 观察者接口
     *
     * 缺点：
     * 1. Observable是类，不是接口，违反"组合优于继承"
     * 2. 线程不安全
     * 3. 已在Java 9标记为过时
     */

    // ==================== 推模型 vs 拉模型 ====================

    /**
     * 推模型：主题主动推送数据
     */
    interface PushObserver {
        void update(String message, Object data);
    }

    /**
     * 拉模型：观察者主动获取数据
     */
    interface PullObserver {
        void update(Subject subject);
    }

    /**
     * 拉模型观察者实现
     */
    static class PullUser implements PullObserver {
        private final String name;

        public PullUser(String name) {
            this.name = name;
        }

        @Override
        public void update(Subject subject) {
            // 观察者主动从主题获取需要的数据
            // String data = ((OfficialAccount) subject).getLatestArticle();
            System.out.println(name + " 主动获取数据");
        }
    }

    // ==================== Spring事件机制 ====================

    /**
     * Spring的事件驱动模型就是观察者模式的典型应用
     *
     * 核心组件：
     * 1. ApplicationEvent - 事件基类
     * 2. ApplicationListener - 事件监听器（观察者）
     * 3. ApplicationEventPublisher - 事件发布器（主题）
     */

    /**
     * 自定义事件
     */
    static class OrderCreatedEvent /* extends ApplicationEvent */ {
        private final Long orderId;
        private final String userId;

        public OrderCreatedEvent(Object source, Long orderId, String userId) {
            // super(source);
            this.orderId = orderId;
            this.userId = userId;
        }

        public Long getOrderId() { return orderId; }
        public String getUserId() { return userId; }
    }

    /**
     * 事件监听器
     */
    // @Component
    static class OrderEventListener /* implements ApplicationListener<OrderCreatedEvent> */ {

        // @Override
        public void onApplicationEvent(/* OrderCreatedEvent event */) {
            // System.out.println("收到订单创建事件：" + event.getOrderId());
        }
    }

    /**
     * 注解方式（更简洁，推荐）
     */
    // @Component
    static class OrderEventHandler {

        // @EventListener
        public void handleOrderCreated(/* OrderCreatedEvent event */) {
            // System.out.println("处理订单：" + event.getOrderId());
        }

        // 异步处理
        // @Async
        // @EventListener
        public void handleAsync(/* OrderCreatedEvent event */) {
        }

        // 条件监听
        // @EventListener(condition = "#event.orderId > 1000")
        public void handleHighValueOrder(/* OrderCreatedEvent event */) {
        }

        // 事务相关
        // @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handleAfterCommit(/* OrderCreatedEvent event */) {
        }
    }

    /**
     * 事件发布
     */
    // @Service
    static class OrderService {
        // @Autowired
        // private ApplicationEventPublisher eventPublisher;

        public void createOrder(Long orderId, String userId) {
            // 创建订单逻辑...

            // 发布事件
            // eventPublisher.publishEvent(new OrderCreatedEvent(this, orderId, userId));
        }
    }

    // ==================== 响应式编程中的观察者 ====================

    /**
     * RxJava / Reactor 中的观察者模式
     *
     * Publisher（发布者）= Observable（被观察者）
     * Subscriber（订阅者）= Observer（观察者）
     *
     * Flow：
     * Publisher → emit → Subscriber
     *
     * 示例：
     * Flux.just("a", "b", "c")
     *     .map(String::toUpperCase)
     *     .subscribe(System.out::println);
     */

    // ==================== Event Bus 实现 ====================

    /**
     * 简易事件总线
     */
    static class EventBus {
        private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();

        public <T> void register(Class<T> eventType, Consumer<T> listener) {
            listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
        }

        @SuppressWarnings("unchecked")
        public <T> void post(T event) {
            List<Consumer<?>> consumers = listeners.get(event.getClass());
            if (consumers != null) {
                for (Consumer<?> consumer : consumers) {
                    ((Consumer<T>) consumer).accept(event);
                }
            }
        }
    }

    @FunctionalInterface
    interface Consumer<T> {
        void accept(T t);
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // 创建公众号
        OfficialAccount account = new OfficialAccount("技术分享");

        // 创建用户
        User user1 = new User("张三");
        User user2 = new User("李四");
        User user3 = new User("王五");

        // 用户关注
        account.attach(user1);
        account.attach(user2);
        account.attach(user3);

        // 发布文章
        account.publishArticle("观察者模式详解");

        // 用户取关
        account.detach(user2);

        // 再次发布
        account.publishArticle("Spring事件机制");

        // 测试EventBus
        System.out.println("\n=== EventBus测试 ===");
        EventBus eventBus = new EventBus();
        eventBus.register(String.class, msg -> System.out.println("收到消息：" + msg));
        eventBus.register(Integer.class, num -> System.out.println("收到数字：" + num));

        eventBus.post("Hello World");
        eventBus.post(123);
    }
}
