package com.example.demo.javabase.designpatterns;

import java.util.*;

/**
 * 责任链模式 (Chain of Responsibility)
 *
 * 【核心思想】
 * 将请求沿着处理链传递，直到有对象处理它为止。
 * 解耦请求发送者和接收者。
 *
 * 【应用场景】
 * 1. Servlet Filter链
 * 2. Spring Interceptor拦截器
 * 3. 审批流程
 * 4. 日志级别处理
 * 5. 中间件模式
 *
 * 【优缺点】
 * 优点：解耦、灵活、符合单一职责
 * 缺点：链过长影响性能、不易调试
 */
public class ChainOfResponsibility {

    // ==================== 基础实现 ====================

    /**
     * 请求类
     */
    static class Request {
        private String data;
        private boolean authenticated;
        private boolean authorized;
        private boolean validated;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public boolean isAuthenticated() { return authenticated; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
        public boolean isAuthorized() { return authorized; }
        public void setAuthorized(boolean authorized) { this.authorized = authorized; }
        public boolean isValidated() { return validated; }
        public void setValidated(boolean validated) { this.validated = validated; }
    }

    /**
     * 处理器抽象类
     */
    static abstract class Handler {
        protected Handler next;

        public Handler setNext(Handler next) {
            this.next = next;
            return next; // 支持链式调用
        }

        public abstract void handle(Request request);

        protected void passToNext(Request request) {
            if (next != null) {
                next.handle(request);
            }
        }
    }

    /**
     * 具体处理器：认证
     */
    static class AuthHandler extends Handler {
        @Override
        public void handle(Request request) {
            System.out.println("【认证】检查用户身份...");

            // 模拟认证
            if (request.getData().contains("token")) {
                request.setAuthenticated(true);
                System.out.println("【认证】认证通过");
                passToNext(request);
            } else {
                System.out.println("【认证】认证失败，终止请求");
            }
        }
    }

    /**
     * 具体处理器：授权
     */
    static class AuthorizationHandler extends Handler {
        @Override
        public void handle(Request request) {
            System.out.println("【授权】检查用户权限...");

            if (request.isAuthenticated()) {
                request.setAuthorized(true);
                System.out.println("【授权】授权通过");
                passToNext(request);
            } else {
                System.out.println("【授权】未认证，无法授权");
            }
        }
    }

    /**
     * 具体处理器：验证
     */
    static class ValidationHandler extends Handler {
        @Override
        public void handle(Request request) {
            System.out.println("【验证】验证请求数据...");

            if (request.getData() != null && !request.getData().isEmpty()) {
                request.setValidated(true);
                System.out.println("【验证】验证通过");
                passToNext(request);
            } else {
                System.out.println("【验证】数据无效");
            }
        }
    }

    /**
     * 具体处理器：业务处理
     */
    static class BusinessHandler extends Handler {
        @Override
        public void handle(Request request) {
            if (request.isAuthenticated() && request.isAuthorized() && request.isValidated()) {
                System.out.println("【业务】执行业务逻辑：" + request.getData());
            } else {
                System.out.println("【业务】前置检查未通过，无法执行");
            }
        }
    }

    // ==================== Servlet Filter模式 ====================

    /**
     * Filter接口（模拟Servlet Filter）
     */
    interface Filter {
        void doFilter(Request request, FilterChain chain);
    }

    /**
     * FilterChain接口
     */
    interface FilterChain {
        void doFilter(Request request);
    }

    /**
     * FilterChain实现
     */
    static class ApplicationFilterChain implements FilterChain {
        private final List<Filter> filters = new ArrayList<>();
        private int pos = 0;

        public void addFilter(Filter filter) {
            filters.add(filter);
        }

        @Override
        public void doFilter(Request request) {
            if (pos < filters.size()) {
                Filter filter = filters.get(pos++);
                filter.doFilter(request, this);
            } else {
                // 所有Filter执行完毕，执行目标Servlet
                System.out.println("执行目标Servlet");
            }
        }
    }

    /**
     * 具体Filter
     */
    static class LoggingFilter implements Filter {
        @Override
        public void doFilter(Request request, FilterChain chain) {
            System.out.println("[Filter] 请求开始：" + request.getData());
            chain.doFilter(request);
            System.out.println("[Filter] 请求结束");
        }
    }

    static class EncodingFilter implements Filter {
        @Override
        public void doFilter(Request request, FilterChain chain) {
            System.out.println("[Filter] 设置编码UTF-8");
            chain.doFilter(request);
        }
    }

    // ==================== 审批流程示例 ====================

    /**
     * 请假申请
     */
    static class LeaveRequest {
        private String name;
        private int days;

        public LeaveRequest(String name, int days) {
            this.name = name;
            this.days = days;
        }

        public String getName() { return name; }
        public int getDays() { return days; }
    }

    /**
     * 审批人抽象类
     */
    static abstract class Approver {
        protected Approver next;
        protected String name;

        public Approver(String name) {
            this.name = name;
        }

        public Approver setNext(Approver next) {
            this.next = next;
            return next;
        }

        public abstract void approve(LeaveRequest request);
    }

    /**
     * 组长：1-3天
     */
    static class TeamLeader extends Approver {
        public TeamLeader(String name) {
            super(name);
        }

        @Override
        public void approve(LeaveRequest request) {
            if (request.getDays() <= 3) {
                System.out.println("组长 " + name + " 审批通过："
                    + request.getName() + " 请假 " + request.getDays() + " 天");
            } else if (next != null) {
                System.out.println("组长 " + name + " 无权审批，转交上级");
                next.approve(request);
            }
        }
    }

    /**
     * 经理：4-7天
     */
    static class Manager extends Approver {
        public Manager(String name) {
            super(name);
        }

        @Override
        public void approve(LeaveRequest request) {
            if (request.getDays() <= 7) {
                System.out.println("经理 " + name + " 审批通过："
                    + request.getName() + " 请假 " + request.getDays() + " 天");
            } else if (next != null) {
                System.out.println("经理 " + name + " 无权审批，转交上级");
                next.approve(request);
            }
        }
    }

    /**
     * 总监：7天以上
     */
    static class Director extends Approver {
        public Director(String name) {
            super(name);
        }

        @Override
        public void approve(LeaveRequest request) {
            System.out.println("总监 " + name + " 审批通过："
                + request.getName() + " 请假 " + request.getDays() + " 天");
        }
    }

    // ==================== Spring Interceptor ====================

    /**
     * Spring MVC拦截器示意
     *
     * public interface HandlerInterceptor {
     *     default boolean preHandle(HttpServletRequest request,
     *                              HttpServletResponse response,
     *                              Object handler) { return true; }
     *
     *     default void postHandle(...) {}
     *
     *     default void afterCompletion(...) {}
     * }
     *
     * 执行顺序：
     * preHandle1 -> preHandle2 -> Controller -> postHandle2 -> postHandle1
     *           -> afterCompletion2 -> afterCompletion1
     */

    // ==================== 函数式责任链 ====================

    /**
     * 使用函数式接口实现责任链
     */
    @FunctionalInterface
    interface RequestHandler {
        Request handle(Request request);

        default RequestHandler andThen(RequestHandler next) {
            return request -> {
                Request result = this.handle(request);
                return result != null ? next.handle(result) : null;
            };
        }
    }

    // ==================== 测试代码 ====================
    public static void main(String[] args) {
        System.out.println("=== 基础责任链 ===");
        // 构建责任链
        Handler auth = new AuthHandler();
        Handler authz = new AuthorizationHandler();
        Handler validation = new ValidationHandler();
        Handler business = new BusinessHandler();

        auth.setNext(authz).setNext(validation).setNext(business);

        Request request = new Request();
        request.setData("token:abc123, data:hello");
        auth.handle(request);

        System.out.println("\n=== Servlet Filter模式 ===");
        ApplicationFilterChain chain = new ApplicationFilterChain();
        chain.addFilter(new LoggingFilter());
        chain.addFilter(new EncodingFilter());

        Request req = new Request();
        req.setData("/api/user");
        chain.doFilter(req);

        System.out.println("\n=== 审批流程 ===");
        TeamLeader leader = new TeamLeader("张组长");
        Manager manager = new Manager("李经理");
        Director director = new Director("王总监");

        leader.setNext(manager).setNext(director);

        LeaveRequest req1 = new LeaveRequest("小明", 2);
        LeaveRequest req2 = new LeaveRequest("小红", 5);
        LeaveRequest req3 = new LeaveRequest("小刚", 10);

        leader.approve(req1);
        System.out.println();
        leader.approve(req2);
        System.out.println();
        leader.approve(req3);
    }
}
