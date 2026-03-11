package com.example.demo.javabase.designpatterns;

/**
 * 模板方法模式 (Template Method Pattern)
 *
 * 【核心思想】
 * 定义算法骨架，将某些步骤延迟到子类实现。
 * 模板方法让子类在不改变算法结构的情况下，重新定义算法的某些步骤。
 *
 * 【应用场景】
 * 1. Spring JdbcTemplate
 * 2. HttpServlet的service方法
 * 3. 排序算法骨架（比较逻辑由子类实现）
 * 4. 业务流程（订单处理、支付流程）
 *
 * 【优缺点】
 * 优点：代码复用、反向控制、符合开闭原则
 * 缺点：类数量增加、继承限制
 */
public class TemplateMethodPattern {

    // ==================== 基础示例：数据处理流程 ====================

    /**
     * 抽象模板类
     * 定义算法骨架
     */
    static abstract class DataProcessor {
        /**
         * 模板方法：定义算法骨架（final防止子类覆盖）
         */
        public final void process(String data) {
            // 1. 验证数据
            if (!validate(data)) {
                System.out.println("数据验证失败，终止处理");
                return;
            }

            // 2. 解析数据
            Object parsedData = parse(data);

            // 3. 处理数据（具体实现由子类决定）
            Object result = doProcess(parsedData);

            // 4. 保存结果
            save(result);

            // 5. 记录日志（钩子方法，子类可选实现）
            log();
        }

        /**
         * 具体方法：验证数据（通用实现）
         */
        protected boolean validate(String data) {
            if (data == null || data.isEmpty()) {
                System.out.println("数据为空");
                return false;
            }
            System.out.println("数据验证通过");
            return true;
        }

        /**
         * 具体方法：解析数据（通用实现）
         */
        protected Object parse(String data) {
            System.out.println("解析数据：" + data);
            return data;
        }

        /**
         * 抽象方法：处理数据（子类必须实现）
         */
        protected abstract Object doProcess(Object data);

        /**
         * 具体方法：保存结果（通用实现）
         */
        protected void save(Object result) {
            System.out.println("保存结果：" + result);
        }

        /**
         * 钩子方法：记录日志（子类可选实现）
         */
        protected void log() {
            // 默认空实现，子类可以覆盖
        }
    }

    /**
     * 具体实现：JSON数据处理
     */
    static class JsonDataProcessor extends DataProcessor {
        @Override
        protected Object doProcess(Object data) {
            System.out.println("处理JSON数据：" + data);
            return "{\"processed\": \"" + data + "\"}";
        }

        @Override
        protected void log() {
            System.out.println("[JSON处理器] 记录处理日志");
        }
    }

    /**
     * 具体实现：XML数据处理
     */
    static class XmlDataProcessor extends DataProcessor {
        @Override
        protected Object doProcess(Object data) {
            System.out.println("处理XML数据：" + data);
            return "<processed>" + data + "</processed>";
        }

        // 可以覆盖其他方法
        @Override
        protected Object parse(String data) {
            System.out.println("特殊XML解析：" + data);
            return data.toUpperCase();
        }
    }

    // ==================== Spring JdbcTemplate示例 ====================

    /**
     * 简化版JdbcTemplate示意
     *
     * Spring的JdbcTemplate使用模板方法模式：
     * - 定义了数据库操作的骨架
     * - 将变化的部分（SQL、参数、结果映射）留给使用者
     */
    static abstract class JdbcTemplate {

        /**
         * 模板方法：查询流程
         */
        public final <T> T query(String sql, RowMapper<T> rowMapper, Object... params) {
            // 1. 获取连接
            Object connection = getConnection();
            try {
                // 2. 创建语句
                Object statement = createStatement(connection, sql, params);

                // 3. 执行查询
                Object resultSet = executeQuery(statement);

                // 4. 映射结果（由调用者实现）
                T result = rowMapper.mapRow(resultSet);

                // 5. 返回结果
                return result;
            } finally {
                // 6. 关闭连接
                closeConnection(connection);
            }
        }

        // 这些方法可以被子类覆盖，也可以有默认实现
        protected Object getConnection() {
            System.out.println("获取数据库连接");
            return new Object();
        }

        protected Object createStatement(Object connection, String sql, Object[] params) {
            System.out.println("创建PreparedStatement: " + sql);
            return new Object();
        }

        protected Object executeQuery(Object statement) {
            System.out.println("执行查询");
            return new Object();
        }

        protected void closeConnection(Object connection) {
            System.out.println("关闭数据库连接");
        }
    }

    /**
     * 行映射接口（函数式接口）
     */
    @FunctionalInterface
    interface RowMapper<T> {
        T mapRow(Object resultSet);
    }

    /**
     * 使用示例
     */
    static class UserRepository {
        // @Autowired
        // private JdbcTemplate jdbcTemplate;

        public void findById(Long id) {
            // jdbcTemplate.query(
            //     "SELECT * FROM user WHERE id = ?",
            //     rs -> {
            //         User user = new User();
            //         user.setId(rs.getLong("id"));
            //         user.setName(rs.getString("name"));
            //         return user;
            //     },
            //     id
            // );

            System.out.println("查询用户ID：" + id);
        }
    }

    // ==================== HttpServlet模板方法 ====================

    /**
     * HttpServlet中的模板方法
     *
     * service()方法根据请求方法调用相应的doXxx方法：
     *
     * protected void service(HttpServletRequest req, HttpServletResponse resp) {
     *     String method = req.getMethod();
     *     switch (method) {
     *         case "GET":  doGet(req, resp);  break;
     *         case "POST": doPost(req, resp); break;
     *         case "PUT":  doPut(req, resp);  break;
     *         // ...
     *     }
     * }
     *
     * 开发者只需覆盖doGet/doPost等方法即可
     */

    // ==================== 订单处理流程 ====================

    /**
     * 订单处理模板
     */
    static abstract class OrderProcessor {

        /**
         * 模板方法：订单处理流程
         */
        public final void processOrder(Order order) {
            // 1. 验证订单
            validateOrder(order);

            // 2. 计算价格
            double totalPrice = calculatePrice(order);
            order.setTotalPrice(totalPrice);

            // 3. 扣减库存
            deductStock(order);

            // 4. 支付
            boolean paid = doPayment(order);
            if (!paid) {
                rollback(order);
                return;
            }

            // 5. 创建物流
            createShipping(order);

            // 6. 发送通知（钩子方法）
            sendNotification(order);
        }

        protected void validateOrder(Order order) {
            if (order.getItems() == null || order.getItems().isEmpty()) {
                throw new RuntimeException("订单商品为空");
            }
            System.out.println("订单验证通过");
        }

        protected double calculatePrice(Order order) {
            return order.getItems().stream()
                .mapToDouble(Item::getPrice)
                .sum();
        }

        protected void deductStock(Order order) {
            System.out.println("扣减库存");
        }

        /**
         * 抽象方法：支付方式由子类实现
         */
        protected abstract boolean doPayment(Order order);

        protected void rollback(Order order) {
            System.out.println("订单回滚");
        }

        protected void createShipping(Order order) {
            System.out.println("创建物流");
        }

        /**
         * 钩子方法：默认不发送通知
         */
        protected void sendNotification(Order order) {
            // 默认空实现
        }
    }

    /**
     * 普通订单处理器
     */
    static class NormalOrderProcessor extends OrderProcessor {
        @Override
        protected boolean doPayment(Order order) {
            System.out.println("普通订单支付：" + order.getTotalPrice() + "元");
            return true;
        }
    }

    /**
     * 会员订单处理器
     */
    static class VipOrderProcessor extends OrderProcessor {

        @Override
        protected double calculatePrice(Order order) {
            double basePrice = super.calculatePrice(order);
            return basePrice * 0.9;  // 9折优惠
        }

        @Override
        protected boolean doPayment(Order order) {
            System.out.println("VIP订单支付：" + order.getTotalPrice() + "元（已优惠）");
            return true;
        }

        @Override
        protected void sendNotification(Order order) {
            System.out.println("发送VIP专属通知");
        }
    }

    // 实体类
    static class Order {
        private java.util.List<Item> items;
        private double totalPrice;

        public java.util.List<Item> getItems() { return items; }
        public void setItems(java.util.List<Item> items) { this.items = items; }
        public double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    }

    static class Item {
        private String name;
        private double price;

        public Item(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public double getPrice() { return price; }
    }

    // ==================== 回调 vs 模板方法 ====================

    /**
     * 回调方式（更灵活）
     *
     * 模板方法使用继承，回调使用组合
     * Java 8+ 使用Lambda更简洁
     */
    static class DataProcessorWithCallback {

        public void process(String data,
                           java.util.function.Function<String, Object> parser,
                           java.util.function.Function<Object, Object> processor) {
            // 1. 验证
            if (data == null || data.isEmpty()) {
                return;
            }

            // 2. 解析（回调）
            Object parsed = parser.apply(data);

            // 3. 处理（回调）
            Object result = processor.apply(parsed);

            // 4. 保存
            System.out.println("保存结果：" + result);
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        System.out.println("=== 数据处理模板 ===");
        DataProcessor jsonProcessor = new JsonDataProcessor();
        jsonProcessor.process("{\"name\":\"test\"}");

        System.out.println("\n=== 订单处理模板 ===");
        OrderProcessor normalProcessor = new NormalOrderProcessor();
        Order order = new Order();
        order.setItems(java.util.Arrays.asList(
            new Item("商品A", 100),
            new Item("商品B", 200)
        ));
        normalProcessor.processOrder(order);

        System.out.println("\n=== VIP订单处理 ===");
        OrderProcessor vipProcessor = new VipOrderProcessor();
        Order vipOrder = new Order();
        vipOrder.setItems(java.util.Arrays.asList(
            new Item("商品A", 100),
            new Item("商品B", 200)
        ));
        vipProcessor.processOrder(vipOrder);

        System.out.println("\n=== 回调方式 ===");
        DataProcessorWithCallback callbackProcessor = new DataProcessorWithCallback();
        callbackProcessor.process(
            "test data",
            s -> "解析后：" + s,
            o -> "处理后：" + o
        );
    }
}
