package com.example.demo.database.mysql;

import java.sql.*;
import java.util.concurrent.*;

/**
 * 数据库连接池详解
 *
 * 【为什么需要连接池】
 * 1. 创建连接开销大（TCP三次握手、认证）
 * 2. 频繁创建/销毁连接影响性能
 * 3. 连接池复用连接，提高性能
 *
 * 【主流连接池】
 * 1. HikariCP：Spring Boot默认，性能最强
 * 2. Druid：阿里巴巴开源，监控丰富
 * 3. C3P0：老牌连接池，性能较差
 * 4. DBCP：Apache开源，性能一般
 */
public class ConnectionPool {

    // ==================== HikariCP配置 ====================

    /**
     * HikariCP核心配置：
     *
     * spring:
     *   datasource:
     *     driver-class-name: com.mysql.cj.jdbc.Driver
     *     url: jdbc:mysql://localhost:3306/mydb
     *     username: root
     *     password: 123456
     *     hikari:
     *       # 连接池大小配置
     *       minimum-idle: 10           # 最小空闲连接数
     *       maximum-pool-size: 20      # 最大连接数
     *
     *       # 连接超时配置
     *       connection-timeout: 30000  # 获取连接超时时间（毫秒）
     *       idle-timeout: 600000       # 空闲连接超时时间（毫秒）
     *       max-lifetime: 1800000      # 连接最大存活时间（毫秒）
     *
     *       # 连接验证配置
     *       validation-timeout: 5000   # 连接验证超时时间
     *       leak-detection-threshold: 0 # 连接泄漏检测阈值
     *
     *       # 其他配置
     *       pool-name: MyHikariPool    # 连接池名称
     *       connection-test-query: SELECT 1  # 连接测试查询
     */

    /**
     * 关键参数详解：
     *
     * ┌───────────────────────────┬─────────────────────────────────────────┐
     * │ 参数                       │ 说明                                     │
     * ├───────────────────────────┼─────────────────────────────────────────┤
     * │ maximum-pool-size         │ 最大连接数 = (核心数 * 2) + 有效磁盘数    │
     * │ minimum-idle              │ 最小空闲连接数，建议与最大值相同          │
     * │ connection-timeout        │ 获取连接超时，默认30秒                    │
     * │ idle-timeout              │ 空闲连接存活时间，默认10分钟              │
     * │ max-lifetime              │ 连接最大存活时间，默认30分钟              │
     * │ leak-detection-threshold  │ 连接泄漏检测，未归还连接超过阈值告警       │
     * └───────────────────────────┴─────────────────────────────────────────┘
     *
     * 连接数计算公式：
     * connections = (CPU核心数 * 2) + 有效磁盘数
     *
     * 例如：4核CPU + 1块磁盘 = 4*2+1 = 9个连接
     */

    // ==================== HikariCP原理 ====================

    /**
     * HikariCP为什么快？
     *
     * 1. 字节码优化
     *    - 使用Javassist生成代理类
     *    - 避免反射调用
     *
     * 2. 并发设计
     *    - ConcurrentBag设计
     *    - ThreadLocal缓存
     *    - CAS操作
     *
     * 3. 代理优化
     *    - 快速判断连接是否关闭
     *    - 直接调用，不经过代理
     *
     * ConcurrentBag结构：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                     ConcurrentBag                           │
     * │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
     * │  │ sharedList  │  │ threadList  │  │  waiters    │         │
     * │  │ (共享列表)   │  │ (线程本地)  │  │ (等待队列)  │         │
     * │  └─────────────┘  └─────────────┘  └─────────────┘         │
     * └─────────────────────────────────────────────────────────────┘
     *
     * 获取连接流程：
     * 1. 先从ThreadLocal获取（无竞争）
     * 2. 未命中则从sharedList获取（CAS竞争）
     * 3. 都没有则等待其他连接归还
     */

    // ==================== 简易连接池实现 ====================

    /**
     * 简易连接池示意
     */
    static class SimpleConnectionPool {
        private final BlockingQueue<Connection> pool;
        private final int maxSize;
        private final String url;
        private final String username;
        private final String password;

        public SimpleConnectionPool(int initialSize, int maxSize,
                                    String url, String username, String password) {
            this.maxSize = maxSize;
            this.url = url;
            this.username = username;
            this.password = username;
            this.pool = new LinkedBlockingQueue<>(maxSize);

            // 初始化连接
            for (int i = 0; i < initialSize; i++) {
                pool.offer(createConnection());
            }
        }

        /**
         * 获取连接
         * 注意：实际连接池会返回包装后的Connection，重写close方法实现归还
         * 这里简化处理，直接返回原始连接
         */
        public Connection getConnection(long timeout, TimeUnit unit) throws Exception {
            Connection conn = pool.poll(timeout, unit);
            if (conn == null) {
                throw new RuntimeException("获取连接超时");
            }
            return conn;  // 实际连接池应返回包装后的Connection
        }

        /**
         * 归还连接
         */
        public void releaseConnection(Connection conn) {
            if (conn != null && pool.size() < maxSize) {
                pool.offer(conn);
            } else {
                closeConnection(conn);
            }
        }

        private Connection createConnection() {
            // 实际创建连接
            return null; // DriverManager.getConnection(url, username, password);
        }

        private void closeConnection(Connection conn) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        /**
         * 包装连接，重写close方法实现归还
         *
         * 实际实现中可以使用动态代理或实现所有Connection方法
         * 这里简化处理，不实现Connection接口
         */
        static class PooledConnection {
            private final Connection delegate;
            private final SimpleConnectionPool pool;
            private boolean closed = false;

            public PooledConnection(Connection delegate, SimpleConnectionPool pool) {
                this.delegate = delegate;
                this.pool = pool;
            }

            public void close() throws SQLException {
                if (!closed) {
                    closed = true;
                    pool.releaseConnection(delegate);
                }
            }

            // 委托方法
            public Statement createStatement() throws SQLException {
                return delegate.createStatement();
            }

            public PreparedStatement prepareStatement(String sql) throws SQLException {
                return delegate.prepareStatement(sql);
            }

            public Connection getDelegate() {
                return delegate;
            }
        }
    }

    // ==================== Druid连接池 ====================

    /**
     * Druid特点：
     * 1. 监控功能强大
     * 2. SQL防注入
     * 3. SQL执行日志
     * 4. 扩展性好
     *
     * 配置：
     * spring:
     *   datasource:
     *     type: com.alibaba.druid.pool.DruidDataSource
     *     druid:
     *       initial-size: 5
     *       min-idle: 5
     *       max-active: 20
     *       max-wait: 60000
     *       time-between-eviction-runs-millis: 60000
     *       min-evictable-idle-time-millis: 300000
     *       validation-query: SELECT 1
     *       test-while-idle: true
     *       test-on-borrow: false
     *       test-on-return: false
     *       pool-prepared-statements: true
     *       max-pool-prepared-statement-per-connection-size: 20
     *
     *       # 监控配置
     *       stat-view-servlet:
     *         enabled: true
     *         url-pattern: /druid/*
     *       web-stat-filter:
     *         enabled: true
     *         url-pattern: /*
     */

    // ==================== 连接泄漏问题 ====================

    /**
     * 连接泄漏：获取连接后未归还
     *
     * 场景：
     * 1. 忘记调用close()
     * 2. 异常导致close()未执行
     * 3. 长时间持有连接
     *
     * 解决方案：
     */

    /**
     * 1. try-with-resources
     */
    /*
    public void correctUsage() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // 处理结果
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 自动关闭连接
    }
    */

    /**
     * 2. Spring事务管理
     */
    /*
    @Transactional
    public void withTransaction() {
        // Spring自动管理连接
        // 无需手动获取/释放连接
        userRepository.findById(1L);
    }
    */

    /**
     * 3. 泄漏检测
     *
     * HikariCP配置：
     * leak-detection-threshold=60000
     *
     * 如果连接被持有超过60秒未归还，打印警告日志
     */

    // ==================== 最佳实践 ====================

    /**
     * 连接池配置最佳实践：
     *
     * 1. 连接数配置
     *    - 最小连接数 = 常规并发量
     *    - 最大连接数 = 峰值并发量
     *    - 公式：(CPU核心数 * 2) + 有效磁盘数
     *
     * 2. 超时配置
     *    - connection-timeout: 根据业务响应时间设置
     *    - idle-timeout: 10分钟
     *    - max-lifetime: 30分钟（小于数据库wait_timeout）
     *
     * 3. 监控配置
     *    - 开启leak-detection
     *    - 监控活跃连接数
     *    - 监控等待获取连接的线程数
     *
     * 4. 连接验证
     *    - 配置validation-query
     *    - 开启test-while-idle
     *
     * 5. 预编译语句缓存
     *    - 开启pool-prepared-statements
     *    - 设置合适的缓存大小
     */

    /**
     * 常见问题：
     *
     * Q1: 连接池满了怎么办？
     * A: 1. 检查是否有连接泄漏
     *    2. 增大连接池大小
     *    3. 优化慢查询
     *
     * Q2: HikariCP vs Druid怎么选？
     * A: HikariCP性能更好，Druid监控更强大
     *    一般场景用HikariCP，需要监控用Druid
     *
     * Q3: 连接数设置多少合适？
     * A: 公式：(CPU核心数 * 2) + 有效磁盘数
     *    但需要根据实际负载调优
     */
}
