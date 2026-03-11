package com.example.demo.javabase.designpatterns;

/**
 * 装饰器模式 (Decorator Pattern)
 *
 * 【核心思想】
 * 动态地给对象添加额外职责，比继承更灵活。
 * 装饰器与被装饰对象有相同的接口，可以在不改变对象结构的情况下扩展功能。
 *
 * 【应用场景】
 * 1. Java I/O流（InputStream/OutputStream）
 * 2. 咖啡配料系统
 * 3. 报表装饰（加标题、加页脚、加水印）
 * 4. 请求/响应增强（过滤器链）
 *
 * 【优缺点】
 * 优点：开闭原则、灵活组合、避免类爆炸
 * 缺点：增加复杂度、装饰顺序敏感
 *
 * 【装饰器 vs 代理模式】
 * 装饰器：增强功能，添加新职责
 * 代理：控制访问，不增加新功能
 */
public class DecoratorPattern {

    // ==================== 场景：咖啡配料系统 ====================

    /**
     * 组件接口
     */
    interface Coffee {
        String getDescription();
        double getCost();
    }

    /**
     * 具体组件：基础咖啡
     */
    static class SimpleCoffee implements Coffee {
        @Override
        public String getDescription() {
            return "黑咖啡";
        }

        @Override
        public double getCost() {
            return 10.0;
        }
    }

    /**
     * 装饰器基类
     * 实现Coffee接口，并持有Coffee引用
     */
    static abstract class CoffeeDecorator implements Coffee {
        protected final Coffee coffee;

        public CoffeeDecorator(Coffee coffee) {
            this.coffee = coffee;
        }
    }

    /**
     * 具体装饰器：牛奶
     */
    static class MilkDecorator extends CoffeeDecorator {
        public MilkDecorator(Coffee coffee) {
            super(coffee);
        }

        @Override
        public String getDescription() {
            return coffee.getDescription() + " + 牛奶";
        }

        @Override
        public double getCost() {
            return coffee.getCost() + 3.0;
        }
    }

    /**
     * 具体装饰器：糖
     */
    static class SugarDecorator extends CoffeeDecorator {
        public SugarDecorator(Coffee coffee) {
            super(coffee);
        }

        @Override
        public String getDescription() {
            return coffee.getDescription() + " + 糖";
        }

        @Override
        public double getCost() {
            return coffee.getCost() + 1.0;
        }
    }

    /**
     * 具体装饰器：奶油
     */
    static class CreamDecorator extends CoffeeDecorator {
        public CreamDecorator(Coffee coffee) {
            super(coffee);
        }

        @Override
        public String getDescription() {
            return coffee.getDescription() + " + 奶油";
        }

        @Override
        public double getCost() {
            return coffee.getCost() + 5.0;
        }
    }

    // ==================== Java I/O中的装饰器 ====================

    /**
     * Java I/O 是装饰器模式的经典应用
     *
     * 抽象组件：InputStream / OutputStream
     * 具体组件：FileInputStream / ByteArrayInputStream
     * 抽象装饰器：FilterInputStream / FilterOutputStream
     * 具体装饰器：BufferedInputStream / DataInputStream / GZIPInputStream
     *
     * 示例：
     * InputStream is = new BufferedInputStream(
     *                     new GZIPInputStream(
     *                         new FileInputStream("file.gz")));
     */

    /**
     * 简化版InputStream示意
     */
    static abstract class InputStream {
        public abstract int read();
    }

    static class FileInputStream extends InputStream {
        @Override
        public int read() {
            return 0; // 从文件读取
        }
    }

    static class FilterInputStream extends InputStream {
        protected final InputStream in;

        public FilterInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public int read() {
            return in.read();
        }
    }

    static class BufferedInputStream extends FilterInputStream {
        private final byte[] buffer = new byte[8192];

        public BufferedInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() {
            // 先从缓冲区读取，缓冲区空了再从底层读取
            return super.read();
        }
    }

    // ==================== 报表装饰器示例 ====================

    interface Report {
        String getContent();
    }

    static class BasicReport implements Report {
        @Override
        public String getContent() {
            return "报表数据";
        }
    }

    static abstract class ReportDecorator implements Report {
        protected final Report report;

        public ReportDecorator(Report report) {
            this.report = report;
        }
    }

    static class HeaderDecorator extends ReportDecorator {
        private final String header;

        public HeaderDecorator(Report report, String header) {
            super(report);
            this.header = header;
        }

        @Override
        public String getContent() {
            return "【" + header + "】\n" + report.getContent();
        }
    }

    static class FooterDecorator extends ReportDecorator {
        private final String footer;

        public FooterDecorator(Report report, String footer) {
            super(report);
            this.footer = footer;
        }

        @Override
        public String getContent() {
            return report.getContent() + "\n" + footer;
        }
    }

    static class WatermarkDecorator extends ReportDecorator {
        private final String watermark;

        public WatermarkDecorator(Report report, String watermark) {
            super(report);
            this.watermark = watermark;
        }

        @Override
        public String getContent() {
            return report.getContent() + " [水印:" + watermark + "]";
        }
    }

    // ==================== Spring中的装饰器 ====================

    /**
     * Spring中的装饰器模式应用
     *
     * 1. HttpServletRequestWrapper
     *    可以对Request进行增强，如：
     *    - 修改参数
     *    - 缓存请求体（可重复读取）
     *    - 添加自定义头
     *
     * 2. HttpServletResponseWrapper
     *    可以对Response进行增强
     *
     * 3. BeanWrapper
     *    对Bean进行包装，提供属性访问能力
     */

    /**
     * 缓存请求体的Request包装器
     */
    /*
    public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final byte[] cachedBody;

        public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(cachedBody)));
        }
    }
    */

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        // 基础咖啡
        Coffee coffee = new SimpleCoffee();
        System.out.println(coffee.getDescription() + " = " + coffee.getCost() + "元");

        // 加牛奶
        coffee = new MilkDecorator(coffee);
        System.out.println(coffee.getDescription() + " = " + coffee.getCost() + "元");

        // 再加糖
        coffee = new SugarDecorator(coffee);
        System.out.println(coffee.getDescription() + " = " + coffee.getCost() + "元");

        // 再加奶油
        coffee = new CreamDecorator(coffee);
        System.out.println(coffee.getDescription() + " = " + coffee.getCost() + "元");

        // 另一种组合方式（链式）
        Coffee fancyCoffee = new CreamDecorator(
            new SugarDecorator(
                new MilkDecorator(
                    new SimpleCoffee())));
        System.out.println("\n花式咖啡：" + fancyCoffee.getDescription() + " = " + fancyCoffee.getCost() + "元");

        // 报表装饰器
        System.out.println("\n=== 报表装饰器 ===");
        Report report = new BasicReport();
        report = new HeaderDecorator(report, "月度销售报表");
        report = new FooterDecorator(report, "--- 生成时间: 2024-01 ---");
        report = new WatermarkDecorator(report, "机密");
        System.out.println(report.getContent());
    }
}
