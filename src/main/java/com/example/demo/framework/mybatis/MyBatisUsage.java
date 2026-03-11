package com.example.demo.framework.mybatis;

import java.util.*;

/**
 * MyBatis核心知识点
 *
 * 【MyBatis vs Hibernate/JPA】
 * MyBatis：半自动ORM，SQL灵活，适合复杂查询
 * Hibernate：全自动ORM，对象操作，适合简单CRUD
 *
 * 【核心组件】
 * 1. SqlSessionFactory：创建SqlSession的工厂
 * 2. SqlSession：执行SQL的会话
 * 3. Mapper接口：定义数据库操作
 * 4. XML/注解：SQL定义
 */
public class MyBatisUsage {

    // ==================== Mapper XML 示例 ====================
    /*
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

    <mapper namespace="com.example.demo.mapper.UserMapper">

        <!- - 结果映射 - ->
        <resultMap id="UserResultMap" type="com.example.demo.entity.User">
            <id property="id" column="id"/>
            <result property="name" column="name"/>
            <result property="email" column="email"/>
            <!- - 一对多关联 - ->
            <collection property="orders" ofType="Order">
                <id property="orderId" column="order_id"/>
                <result property="orderNo" column="order_no"/>
            </collection>
        </resultMap>

        <!- - 基础查询 - ->
        <select id="selectById" resultType="User">
            SELECT * FROM user WHERE id = #{id}
        </select>

        <!- - 参数传递 - ->
        <select id="selectByCondition" resultType="User">
            SELECT * FROM user
            <where>
                <if test="name != null">
                    AND name LIKE CONCAT('%', #{name}, '%')
                </if>
                <if test="email != null">
                    AND email = #{email}
                </if>
            </where>
        </select>

        <!- - 动态SQL：foreach - ->
        <select id="selectByIds" resultType="User">
            SELECT * FROM user WHERE id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
        </select>

        <!- - 批量插入 - ->
        <insert id="batchInsert" useGeneratedKeys="true" keyProperty="id">
            INSERT INTO user (name, email) VALUES
            <foreach collection="list" item="user" separator=",">
                (#{user.name}, #{user.email})
            </foreach>
        </insert>

        <!- - 动态SQL：choose - ->
        <select id="selectByPriority" resultType="User">
            SELECT * FROM user WHERE 1=1
            <choose>
                <when test="id != null">
                    AND id = #{id}
                </when>
                <when test="name != null">
                    AND name = #{name}
                </when>
                <otherwise>
                    AND status = 1
                </otherwise>
            </choose>
        </select>

        <!- - set 标签（自动处理逗号） - ->
        <update id="updateSelective">
            UPDATE user
            <set>
                <if test="name != null">name = #{name},</if>
                <if test="email != null">email = #{email},</if>
            </set>
            WHERE id = #{id}
        </update>

    </mapper>
    */

    // ==================== 注解方式 ====================
    /*
    @Mapper
    public interface UserMapper {

        @Select("SELECT * FROM user WHERE id = #{id}")
        User selectById(Long id);

        @Select("SELECT * FROM user WHERE name = #{name}")
        @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "orders", column = "id",
                    many = @Many(select = "selectOrdersByUserId"))
        })
        User selectWithOrders(String name);

        @Insert("INSERT INTO user(name, email) VALUES(#{name}, #{email})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(User user);

        @Update("UPDATE user SET name = #{name} WHERE id = #{id}")
        int update(User user);

        @Delete("DELETE FROM user WHERE id = #{id}")
        int delete(Long id);
    }
    */

    // ==================== #{} vs ${} ====================
    /**
     * #{}：预编译，安全
     * ${}：字符串替换，不安全（SQL注入风险）
     *
     * 示例：
     * SELECT * FROM user WHERE name = #{name}
     * → SELECT * FROM user WHERE name = ?  （预编译）
     *
     * SELECT * FROM user WHERE name = '${name}'
     * → SELECT * FROM user WHERE name = '张三'  （直接拼接）
     *
     * 使用场景：
     * - #{}：参数值（推荐）
     * - ${}：表名、列名等动态部分（注意防止注入）
     *
     * 例：
     * SELECT * FROM ${tableName} WHERE ${column} = #{value}
     */

    // ==================== 结果映射 ====================
    /**
     * 1. 自动映射
     *    列名和属性名一致时自动映射（可配置下划线转驼峰）
     *
     * 2. resultMap
     *    复杂映射：一对一、一对多
     *
     * 3. association（一对一）
     *    <association property="dept" javaType="Department">
     *        <id property="deptId" column="dept_id"/>
     *        <result property="deptName" column="dept_name"/>
     *    </association>
     *
     * 4. collection（一对多）
     *    <collection property="orders" ofType="Order">
     *        <id property="orderId" column="order_id"/>
     *    </collection>
     */

    // ==================== 嵌套查询 vs 联表查询 ====================
    /**
     * 嵌套查询（N+1问题）：
     * - 先查主表
     * - 再根据关联ID查从表（每个主表记录触发一次查询）
     * - 问题：如果有100条主表记录，会执行101次查询！
     *
     * 联表查询（推荐）：
     * - 一次JOIN查询获取所有数据
     * - 性能更好
     *
     * 解决N+1问题：
     * 1. 使用JOIN查询
     * 2. 开启延迟加载：lazyLoadingEnabled=true
     * 3. 使用 @BatchSize 批量加载
     */

    // ==================== 插件机制 ====================
    /**
     * MyBatis插件（拦截器）
     *
     * 可拦截的接口：
     * - Executor：执行器
     * - StatementHandler：语句处理器
     * - ParameterHandler：参数处理器
     * - ResultSetHandler：结果集处理器
     *
     * 常见应用：
     * 1. 分页插件（PageHelper）
     * 2. SQL日志
     * 3. 数据权限
     * 4. SQL性能监控
     */
    /*
    @Intercepts({
        @Signature(type = Executor.class, method = "query",
            args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
    })
    public class MyPlugin implements Interceptor {
        @Override
        public Object intercept(Invocation invocation) throws Throwable {
            // 前置处理
            long start = System.currentTimeMillis();

            // 执行原方法
            Object result = invocation.proceed();

            // 后置处理
            System.out.println("SQL执行时间：" + (System.currentTimeMillis() - start));

            return result;
        }
    }
    */

    // ==================== 一级缓存 vs 二级缓存 ====================
    /**
     * 一级缓存（Session级别，默认开启）
     * - 同一个SqlSession中，相同查询直接返回缓存
     * - 执行insert/update/delete会清空缓存
     * - 手动清除：sqlSession.clearCache()
     *
     * 二级缓存（Mapper级别，需配置）
     * - 跨SqlSession共享
     * - 配置：<setting name="cacheEnabled" value="true"/>
     * - Mapper XML中添加：<cache/>
     *
     * 缓存失效场景：
     * 1. 执行了增删改操作
     * 2. sqlSession.clearCache()
     * 3. sqlSession.close()
     *
     * 分布式环境注意：
     * - 本地缓存可能导致数据不一致
     * - 建议使用Redis等分布式缓存
     */

    // ==================== 批量操作优化 ====================
    /**
     * 方式1：foreach批量插入（推荐）
     * INSERT INTO user (name) VALUES ('a'),('b'),('c')
     *
     * 方式2：Batch Executor
     * sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
     * - 重用预编译语句
     * - 性能更好
     */

    // ==================== 实体类示例 ====================
    static class User {
        private Long id;
        private String name;
        private String email;
        private List<Order> orders;

        // getter/setter
    }

    static class Order {
        private Long orderId;
        private String orderNo;
    }
}
