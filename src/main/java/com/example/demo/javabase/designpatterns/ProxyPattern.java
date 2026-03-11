package com.example.demo.javabase.designpatterns;

import java.lang.reflect.*;

/**
 * 代理模式 (Proxy Pattern)
 *
 * 【核心思想】
 * 为其他对象提供代理，以控制对这个对象的访问。
 * 代理对象在客户端和目标对象之间起到中介作用。
 *
 * 【应用场景】
 * 1. 远程代理：RPC调用
 * 2. 虚拟代理：延迟加载（图片懒加载）
 * 3. 保护代理：权限控制
 * 4. 智能引用：AOP、缓存、日志
 *
 * 【代理 vs 装饰器】
 * 代理：控制访问，不增加功能
 * 装饰器：增强功能，添加职责
 */
public class ProxyPattern {

    // ==================== 静态代理 ====================

    interface UserService {
        void addUser(String name);
        void deleteUser(String name);
    }

    static class UserServiceImpl implements UserService {
        @Override
        public void addUser(String name) {
            System.out.println("添加用户：" + name);
        }

        @Override
        public void deleteUser(String name) {
            System.out.println("删除用户：" + name);
        }
    }

    static class UserServiceProxy implements UserService {
        private final UserService target;

        public UserServiceProxy(UserService target) {
            this.target = target;
        }

        @Override
        public void addUser(String name) {
            System.out.println("[静态代理] 记录日志 - 添加用户前");
            target.addUser(name);
            System.out.println("[静态代理] 记录日志 - 添加用户后");
        }

        @Override
        public void deleteUser(String name) {
            System.out.println("[静态代理] 记录日志 - 删除用户前");
            target.deleteUser(name);
            System.out.println("[静态代理] 记录日志 - 删除用户后");
        }
    }

    // ==================== JDK动态代理 ====================

    static class JdkProxyHandler implements InvocationHandler {
        private final Object target;

        public JdkProxyHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("[JDK代理] 方法调用前：" + method.getName());
            long start = System.currentTimeMillis();
            try {
                Object result = method.invoke(target, args);
                System.out.println("[JDK代理] 方法调用成功");
                return result;
            } catch (Exception e) {
                System.out.println("[JDK代理] 方法调用异常：" + e.getMessage());
                throw e;
            } finally {
                System.out.println("[JDK代理] 耗时：" + (System.currentTimeMillis() - start) + "ms");
            }
        }

        @SuppressWarnings("unchecked")
        public static <T> T createProxy(T target) {
            return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new JdkProxyHandler(target)
            );
        }
    }

    // ==================== 保护代理 ====================

    interface Document {
        void read();
        void edit(String content);
        void remove();
    }

    static class RealDocument implements Document {
        private final String name;

        public RealDocument(String name) {
            this.name = name;
        }

        @Override
        public void read() {
            System.out.println("读取文档：" + name);
        }

        @Override
        public void edit(String content) {
            System.out.println("编辑文档：" + name + "，内容：" + content);
        }

        @Override
        public void remove() {
            System.out.println("删除文档：" + name);
        }
    }

    static class DocumentProxy implements Document {
        private final RealDocument document;
        private final String role;

        public DocumentProxy(RealDocument document, String role) {
            this.document = document;
            this.role = role;
        }

        @Override
        public void read() {
            document.read();
        }

        @Override
        public void edit(String content) {
            if (!"admin".equals(role) && !"editor".equals(role)) {
                System.out.println("权限不足：无法编辑文档");
                return;
            }
            document.edit(content);
        }

        @Override
        public void remove() {
            if (!"admin".equals(role)) {
                System.out.println("权限不足：无法删除文档");
                return;
            }
            document.remove();
        }
    }

    // ==================== 虚拟代理（延迟加载） ====================

    static class HeavyImage {
        private final String filename;

        public HeavyImage(String filename) {
            this.filename = filename;
            System.out.println("加载图片：" + filename + "（耗时操作）");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void display() {
            System.out.println("显示图片：" + filename);
        }
    }

    static class ImageProxy {
        private final String filename;
        private HeavyImage image;

        public ImageProxy(String filename) {
            this.filename = filename;
        }

        public void display() {
            if (image == null) {
                System.out.println("首次访问，延迟加载...");
                image = new HeavyImage(filename);
            }
            image.display();
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        System.out.println("=== 静态代理 ===");
        UserService staticProxy = new UserServiceProxy(new UserServiceImpl());
        staticProxy.addUser("张三");

        System.out.println("\n=== JDK动态代理 ===");
        UserService jdkProxy = JdkProxyHandler.createProxy(new UserServiceImpl());
        jdkProxy.addUser("李四");

        System.out.println("\n=== 保护代理 ===");
        RealDocument doc = new RealDocument("机密文档.txt");
        Document guestDoc = new DocumentProxy(doc, "guest");
        guestDoc.read();
        guestDoc.edit("test");
        guestDoc.remove();

        System.out.println("\n=== 虚拟代理 ===");
        ImageProxy imageProxy = new ImageProxy("big_image.jpg");
        System.out.println("代理创建完成，图片还未加载");
        imageProxy.display();
        imageProxy.display();
    }
}
