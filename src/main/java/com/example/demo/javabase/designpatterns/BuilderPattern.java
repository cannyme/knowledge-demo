package com.example.demo.javabase.designpatterns;

/**
 * 建造者模式 (Builder Pattern)
 *
 * 【核心思想】
 * 将复杂对象的构建与表示分离，使得同样的构建过程可以创建不同的表示。
 *
 * 【应用场景】
 * 1. 对象属性很多，且有可选属性
 * 2. 需要创建不可变对象
 * 3. 参数组合复杂
 * 4. StringBuilder、StringBuilder
 * 5. Lombok @Builder
 *
 * 【优缺点】
 * 优点：链式调用、参数清晰、不可变对象、默认值
 * 缺点：增加代码量、需要创建Builder类
 */
public class BuilderPattern {

    // ==================== 传统建造者模式 ====================

    /**
     * 产品类
     */
    static class Computer {
        // 必需属性
        private final String cpu;
        private final String ram;

        // 可选属性
        private final String storage;
        private final String gpu;
        private final String monitor;
        private final boolean wifi;

        // 私有构造器，只能通过Builder创建
        private Computer(Builder builder) {
            this.cpu = builder.cpu;
            this.ram = builder.ram;
            this.storage = builder.storage;
            this.gpu = builder.gpu;
            this.monitor = builder.monitor;
            this.wifi = builder.wifi;
        }

        // 静态Builder类
        public static class Builder {
            // 必需属性
            private final String cpu;
            private final String ram;

            // 可选属性，有默认值
            private String storage = "256GB SSD";
            private String gpu = null;
            private String monitor = "15寸";
            private boolean wifi = true;

            // Builder构造器，包含必需属性
            public Builder(String cpu, String ram) {
                this.cpu = cpu;
                this.ram = ram;
            }

            // 设置可选属性的链式方法
            public Builder storage(String storage) {
                this.storage = storage;
                return this;
            }

            public Builder gpu(String gpu) {
                this.gpu = gpu;
                return this;
            }

            public Builder monitor(String monitor) {
                this.monitor = monitor;
                return this;
            }

            public Builder wifi(boolean wifi) {
                this.wifi = wifi;
                return this;
            }

            // 构建产品对象
            public Computer build() {
                return new Computer(this);
            }
        }

        @Override
        public String toString() {
            return "Computer{" +
                "cpu='" + cpu + '\'' +
                ", ram='" + ram + '\'' +
                ", storage='" + storage + '\'' +
                ", gpu='" + gpu + '\'' +
                ", monitor='" + monitor + '\'' +
                ", wifi=" + wifi +
                '}';
        }
    }

    // ==================== Lombok @Builder ====================

    /**
     * 使用Lombok简化代码
     *
     * @Builder
     * @AllArgsConstructor(access = AccessLevel.PRIVATE)
     * public class User {
     *     private String name;
     *     private Integer age;
     *     private String email;
     * }
     *
     * 使用：
     * User user = User.builder()
     *     .name("张三")
     *     .age(25)
     *     .email("zhangsan@example.com")
     *     .build();
     */

    // ==================== 嵌套Builder ====================

    /**
     * 复杂对象：订单包含地址
     */
    static class Order {
        private final String orderId;
        private final String customer;
        private final Address address;

        private Order(Builder builder) {
            this.orderId = builder.orderId;
            this.customer = builder.customer;
            this.address = builder.address;
        }

        public static class Builder {
            private String orderId;
            private String customer;
            private Address address;

            public Builder orderId(String orderId) {
                this.orderId = orderId;
                return this;
            }

            public Builder customer(String customer) {
                this.customer = customer;
                return this;
            }

            public Builder address(Address address) {
                this.address = address;
                return this;
            }

            // 嵌套Builder
            public Address.Builder address() {
                return new Address.Builder(this);
            }

            public Order build() {
                return new Order(this);
            }
        }

        static class Address {
            private final String province;
            private final String city;
            private final String detail;

            private Address(Builder builder) {
                this.province = builder.province;
                this.city = builder.city;
                this.detail = builder.detail;
            }

            public static class Builder {
                private final Order.Builder orderBuilder;
                private String province;
                private String city;
                private String detail;

                public Builder(Order.Builder orderBuilder) {
                    this.orderBuilder = orderBuilder;
                }

                public Builder province(String province) {
                    this.province = province;
                    return this;
                }

                public Builder city(String city) {
                    this.city = city;
                    return this;
                }

                public Builder detail(String detail) {
                    this.detail = detail;
                    return this;
                }

                // 返回父级Builder，支持嵌套链式调用
                public Order.Builder end() {
                    orderBuilder.address(new Address(this));
                    return orderBuilder;
                }
            }
        }

        @Override
        public String toString() {
            return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customer='" + customer + '\'' +
                ", address=" + address.province + address.city + address.detail +
                '}';
        }
    }

    // ==================== 经典建造者模式（Director）====================

    /**
     * 产品
     */
    static class House {
        private String foundation;
        private String structure;
        private String roof;
        private String interior;

        // setters...
        public void setFoundation(String foundation) { this.foundation = foundation; }
        public void setStructure(String structure) { this.structure = structure; }
        public void setRoof(String roof) { this.roof = roof; }
        public void setInterior(String interior) { this.interior = interior; }

        @Override
        public String toString() {
            return "House{" + foundation + ", " + structure + ", " + roof + ", " + interior + "}";
        }
    }

    /**
     * 建造者接口
     */
    interface HouseBuilder {
        void buildFoundation();
        void buildStructure();
        void buildRoof();
        void buildInterior();
        House getHouse();
    }

    /**
     * 具体建造者：木屋
     */
    static class WoodenHouseBuilder implements HouseBuilder {
        private House house = new House();

        @Override
        public void buildFoundation() {
            house.setFoundation("木质地基");
        }

        @Override
        public void buildStructure() {
            house.setStructure("木质结构");
        }

        @Override
        public void buildRoof() {
            house.setRoof("木质屋顶");
        }

        @Override
        public void buildInterior() {
            house.setInterior("木质内饰");
        }

        @Override
        public House getHouse() {
            return house;
        }
    }

    /**
     * 具体建造者：混凝土房屋
     */
    static class ConcreteHouseBuilder implements HouseBuilder {
        private House house = new House();

        @Override
        public void buildFoundation() {
            house.setFoundation("混凝土地基");
        }

        @Override
        public void buildStructure() {
            house.setStructure("钢筋混凝土结构");
        }

        @Override
        public void buildRoof() {
            house.setRoof("混凝土屋顶");
        }

        @Override
        public void buildInterior() {
            house.setInterior("现代内饰");
        }

        @Override
        public House getHouse() {
            return house;
        }
    }

    /**
     * 指挥者：控制构建流程
     */
    static class HouseDirector {
        public House construct(HouseBuilder builder) {
            builder.buildFoundation();
            builder.buildStructure();
            builder.buildRoof();
            builder.buildInterior();
            return builder.getHouse();
        }
    }

    // ==================== StringBuilder源码示意 ====================

    /**
     * StringBuilder就是建造者模式的应用
     *
     * StringBuilder sb = new StringBuilder();
     * sb.append("Hello")
     *   .append(" ")
     *   .append("World")
     *   .insert(0, ">>> ");
     *
     * StringBuilder内部维护char[]数组，通过链式append构建最终字符串
     */

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        System.out.println("=== 链式Builder ===");
        Computer computer = new Computer.Builder("Intel i7", "16GB")
            .storage("512GB SSD")
            .gpu("RTX 3080")
            .monitor("27寸 4K")
            .wifi(true)
            .build();
        System.out.println(computer);

        System.out.println("\n=== 嵌套Builder ===");
        Order order = new Order.Builder()
            .orderId("ORD-001")
            .customer("张三")
            .address()
                .province("北京市")
                .city("海淀区")
                .detail("中关村大街1号")
                .end()
            .build();
        System.out.println(order);

        System.out.println("\n=== 经典Builder（Director）===");
        HouseDirector director = new HouseDirector();

        House woodenHouse = director.construct(new WoodenHouseBuilder());
        System.out.println("木屋：" + woodenHouse);

        House concreteHouse = director.construct(new ConcreteHouseBuilder());
        System.out.println("混凝土房：" + concreteHouse);
    }
}
