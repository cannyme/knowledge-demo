package com.example.demo.javabase.designpatterns;

/**
 * 适配器模式 (Adapter Pattern)
 *
 * 【核心思想】
 * 将一个类的接口转换成客户端期望的另一个接口，
 * 使原本因接口不兼容而不能一起工作的类可以一起工作。
 *
 * 【三种形式】
 * 1. 类适配器：通过继承实现
 * 2. 对象适配器：通过组合实现（推荐）
 * 3. 接口适配器：通过抽象类实现
 *
 * 【应用场景】
 * 1. 第三方SDK接口不兼容
 * 2. 旧系统升级
 * 3. Java I/O：InputStreamReader适配InputStream到Reader
 * 4. Arrays.asList()
 */
public class AdapterPattern {

    // ==================== 对象适配器（推荐）====================

    /**
     * 目标接口：期望的接口
     */
    interface MediaPlayer {
        void play(String filename);
    }

    /**
     * 被适配者：已存在的接口
     */
    static class AdvancedMediaPlayer {
        public void playVlc(String filename) {
            System.out.println("播放VLC文件：" + filename);
        }

        public void playMp4(String filename) {
            System.out.println("播放MP4文件：" + filename);
        }
    }

    /**
     * 对象适配器：持有被适配者引用
     */
    static class MediaAdapter implements MediaPlayer {
        private final AdvancedMediaPlayer advancedPlayer;

        public MediaAdapter(AdvancedMediaPlayer advancedPlayer) {
            this.advancedPlayer = advancedPlayer;
        }

        @Override
        public void play(String filename) {
            if (filename.endsWith(".vlc")) {
                advancedPlayer.playVlc(filename);
            } else if (filename.endsWith(".mp4")) {
                advancedPlayer.playMp4(filename);
            } else {
                System.out.println("不支持的格式：" + filename);
            }
        }
    }

    // ==================== 类适配器 ====================

    /**
     * 类适配器：通过继承实现
     * 缺点：Java单继承限制，耦合度高
     */
    static class MediaAdapterByClass extends AdvancedMediaPlayer implements MediaPlayer {
        @Override
        public void play(String filename) {
            if (filename.endsWith(".vlc")) {
                playVlc(filename);
            } else if (filename.endsWith(".mp4")) {
                playMp4(filename);
            }
        }
    }

    // ==================== 接口适配器 ====================

    /**
     * 接口适配器（缺省适配器）
     * 当接口方法很多，但只需要实现部分方法时使用
     */

    /**
     * 定义多个方法的接口
     */
    interface FileOperation {
        void read();
        void modify();
        void remove();
        void copy();
        void move();
    }

    /**
     * 抽象适配器：提供默认空实现
     */
    static abstract class FileAdapter implements FileOperation {
        @Override public void read() {}
        @Override public void modify() {}
        @Override public void remove() {}
        @Override public void copy() {}
        @Override public void move() {}
    }

    /**
     * 具体实现：只需覆盖需要的方法
     */
    static class ReadOnlyFile extends FileAdapter {
        @Override
        public void read() {
            System.out.println("只读文件");
        }
        // 其他方法使用默认空实现
    }

    // ==================== Spring中的适配器 ====================

    /**
     * Spring MVC HandlerAdapter
     *
     * 不同类型的Handler（Controller）有不同的调用方式：
     * - @RequestMapping方法
     * - Controller接口
     * - HttpRequestHandler接口
     * - Servlet
     *
     * HandlerAdapter为每种Handler提供适配，统一调用接口
     *
     * public interface HandlerAdapter {
     *     boolean supports(Object handler);
     *     ModelAndView handle(HttpServletRequest request,
     *                        HttpServletResponse response,
     *                        Object handler);
     * }
     */

    /**
     * 简化版HandlerAdapter示意
     */
    interface Handler {
        void handle();
    }

    interface HandlerAdapter {
        boolean supports(Object handler);
        void handle(Object handler);
    }

    static class RequestMappingHandlerAdapter implements HandlerAdapter {
        @Override
        public boolean supports(Object handler) {
            return handler instanceof RequestMappingHandler;
        }

        @Override
        public void handle(Object handler) {
            ((RequestMappingHandler) handler).invokeHandlerMethod();
        }
    }

    static class RequestMappingHandler {
        public void invokeHandlerMethod() {
            System.out.println("执行@RequestMapping方法");
        }
    }

    // ==================== 日志框架适配 ====================

    /**
     * SLF4J与各种日志框架的适配
     *
     * SLF4J定义统一接口
     * 各日志框架（Log4j、Logback、JUL）通过适配器实现
     */

    interface Logger {
        void info(String message);
        void error(String message);
    }

    /**
     * Log4j适配器
     */
    static class Log4jAdapter implements Logger {
        private final Object log4jLogger; // 实际是org.apache.log4j.Logger

        public Log4jAdapter(Object logger) {
            this.log4jLogger = logger;
        }

        @Override
        public void info(String message) {
            // log4jLogger.info(message);
            System.out.println("[Log4j] INFO: " + message);
        }

        @Override
        public void error(String message) {
            // log4jLogger.error(message);
            System.out.println("[Log4j] ERROR: " + message);
        }
    }

    /**
     * JUL适配器
     */
    static class JulAdapter implements Logger {
        private final java.util.logging.Logger julLogger;

        public JulAdapter(java.util.logging.Logger logger) {
            this.julLogger = logger;
        }

        @Override
        public void info(String message) {
            julLogger.info(message);
        }

        @Override
        public void error(String message) {
            julLogger.severe(message);
        }
    }

    // ==================== 双向适配器 ====================

    /**
     * 双向适配器：两个接口可以互相转换
     */
    interface TargetA {
        void operationA();
    }

    interface TargetB {
        void operationB();
    }

    static class ConcreteA implements TargetA {
        @Override
        public void operationA() {
            System.out.println("A的操作");
        }
    }

    static class ConcreteB implements TargetB {
        @Override
        public void operationB() {
            System.out.println("B的操作");
        }
    }

    /**
     * 双向适配器
     */
    static class TwoWayAdapter implements TargetA, TargetB {
        private TargetA targetA;
        private TargetB targetB;

        public TwoWayAdapter(TargetA targetA) {
            this.targetA = targetA;
        }

        public TwoWayAdapter(TargetB targetB) {
            this.targetB = targetB;
        }

        @Override
        public void operationA() {
            System.out.println("适配：B -> A");
            if (targetB != null) {
                targetB.operationB();
            }
        }

        @Override
        public void operationB() {
            System.out.println("适配：A -> B");
            if (targetA != null) {
                targetA.operationA();
            }
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        System.out.println("=== 对象适配器 ===");
        MediaPlayer player = new MediaAdapter(new AdvancedMediaPlayer());
        player.play("movie.mp4");
        player.play("music.vlc");

        System.out.println("\n=== 接口适配器 ===");
        FileOperation readOnly = new ReadOnlyFile();
        readOnly.read();
        readOnly.remove(); // 空实现，什么都不做

        System.out.println("\n=== 双向适配器 ===");
        TwoWayAdapter adapter1 = new TwoWayAdapter(new ConcreteA());
        adapter1.operationB(); // A转B

        TwoWayAdapter adapter2 = new TwoWayAdapter(new ConcreteB());
        adapter2.operationA(); // B转A
    }
}
