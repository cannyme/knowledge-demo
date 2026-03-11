package com.example.demo.csbasics.network;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
 * Netty与NIO
 *
 * 【IO模型】
 * 1. BIO（Blocking IO）：同步阻塞
 * 2. NIO（Non-blocking IO）：同步非阻塞
 * 3. AIO（Asynchronous IO）：异步非阻塞
 *
 * 【Netty核心】
 * - 基于NIO的高性能网络框架
 * - 零拷贝、内存池、Reactor模式
 */
public class NettyNIO {

    // ==================== BIO vs NIO vs AIO ====================

    /**
     * ┌─────────────────┬─────────────────────┬─────────────────────┬─────────────────────┐
     * │ 特性             │ BIO                 │ NIO                 │ AIO                 │
     * ├─────────────────┼─────────────────────┼─────────────────────┼─────────────────────┤
     * │ 阻塞性           │ 阻塞                │ 非阻塞              │ 非阻塞              │
     * │ 同步/异步        │ 同步                │ 同步                │ 异步                │
     * │ 线程模型         │ 一连接一线程        │ 一线程处理多连接    │ 回调模式            │
     * │ 适用场景         │ 连接数少            │ 连接数多            │ 连接数很多          │
     * │ Java实现        │ ServerSocket        │ Selector+Channel    │ AsynchronousChannel │
     * └─────────────────┴─────────────────────┴─────────────────────┴─────────────────────┘
     */

    // ==================== BIO示例 ====================

    /**
     * BIO服务端（一连接一线程）
     */
    static class BIOServer {
        public static void main(String[] args) throws IOException {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("BIO服务器启动...");

            while (true) {
                // 阻塞等待连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端连接：" + socket.getInetAddress());

                // 为每个连接创建新线程
                new Thread(() -> {
                    try {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                        PrintWriter writer = new PrintWriter(socket.getOutputStream());

                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("收到：" + line);
                            writer.println("Echo: " + line);
                            writer.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    // ==================== NIO核心组件 ====================

    /**
     * NIO三大核心组件：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    NIO核心组件                               │
     * ├─────────────────┬───────────────────────────────────────────┤
     * │ Channel（通道）  │ 双向通道，可读可写                        │
     * │ Buffer（缓冲区） │ 数据容器，读写数据都经过缓冲区             │
     * │ Selector（选择器）│ 多路复用器，轮询注册的Channel             │
     * └─────────────────┴───────────────────────────────────────────┘
     *
     * 工作原理：
     *
     *   ┌─────────────┐
     *   │  Selector   │ ← 多路复用器，一个线程管理多个Channel
     *   └──────┬──────┘
     *          │ 监控
     *    ┌─────┼─────┐
     *    │     │     │
     *    ▼     ▼     ▼
     * ┌─────┐ ┌─────┐ ┌─────┐
     * │Ch 1 │ │Ch 2 │ │Ch 3 │
     * └─────┘ └─────┘ └─────┘
     */

    /**
     * Buffer核心属性：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                        Buffer                               │
     * │  ┌───────────────────────────────────────────────────────┐ │
     * │  │   已读完   │   可读数据   │    可写空间     │           │ │
     * │  └───────────────────────────────────────────────────────┘ │
     * │  ↑           ↑              ↑               ↑              │
     * │  0        position        limit          capacity          │
     * └─────────────────────────────────────────────────────────────┘
     *
     * capacity：容量
     * limit：限制（不可操作的位置）
     * position：当前位置
     * mark：标记位置
     *
     * 操作流程：
     * write: clear() → put() → flip()
     * read:  flip() → get() → clear()
     */
    public static void bufferDemo() {
        // 创建Buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 写入数据
        buffer.put("Hello".getBytes());

        // 切换为读模式
        buffer.flip();

        // 读取数据
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        System.out.println(new String(data));

        // 清空，准备再次写入
        buffer.clear();
    }

    /**
     * NIO服务端示例
     */
    static class NIOServer {
        public static void main(String[] args) throws IOException {
            // 创建Selector
            Selector selector = Selector.open();

            // 创建ServerSocketChannel
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(8080));
            serverChannel.configureBlocking(false);

            // 注册到Selector，监听连接事件
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIO服务器启动...");

            while (true) {
                // 阻塞等待事件（可设置超时）
                selector.select();

                // 获取就绪的事件
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        // 接受连接
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println("客户端连接：" + client.getRemoteAddress());

                    } else if (key.isReadable()) {
                        // 读取数据
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        int bytesRead = client.read(buffer);

                        if (bytesRead == -1) {
                            client.close();
                        } else {
                            buffer.flip();
                            client.write(buffer);
                        }
                    }
                }
            }
        }
    }

    // ==================== Reactor模式 ====================

    /**
     * Reactor模式：基于事件驱动的多路复用设计
     *
     * 单Reactor单线程：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                      Reactor                                │
     * │  ┌─────────────────────────────────────────────────────┐   │
     * │  │           Selector + EventHandler                   │   │
     * │  │       (一个线程处理所有I/O事件)                      │   │
     * │  └─────────────────────────────────────────────────────┘   │
     * └─────────────────────────────────────────────────────────────┘
     *                             │
     *         ┌───────────────────┼───────────────────┐
     *         │                   │                   │
     *         ▼                   ▼                   ▼
     *    ┌─────────┐         ┌─────────┐         ┌─────────┐
     *    │ Handler │         │ Handler │         │ Handler │
     *    │  处理器  │         │  处理器  │         │  处理器  │
     *    └─────────┘         └─────────┘         └─────────┘
     *
     * 问题：一个线程处理所有请求，无法利用多核CPU
     *
     * 单Reactor多线程（Netty采用）：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                      Reactor                                │
     * │  ┌─────────────────────────────────────────────────────┐   │
     * │  │  mainReactor（Boss线程）                             │   │
     * │  │  只负责连接建立                                      │   │
     * │  └─────────────────────────────────────────────────────┘   │
     * │                          │                                  │
     * │                          ▼                                  │
     * │  ┌─────────────────────────────────────────────────────┐   │
     * │  │  subReactor（Worker线程池）                          │   │
     * │  │  负责I/O读写                                         │   │
     * │  │  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐                   │   │
     * │  │  │ T1  │ │ T2  │ │ T3  │ │ T4  │                   │   │
     * │  │  └─────┘ └─────┘ └─────┘ └─────┘                   │   │
     * │  └─────────────────────────────────────────────────────┘   │
     * └─────────────────────────────────────────────────────────────┘
     */

    // ==================== Netty核心组件 ====================

    /**
     * Netty核心组件：
     *
     * 1. EventLoopGroup：事件循环组（Boss/Worker）
     * 2. EventLoop：事件循环（处理Channel的I/O操作）
     * 3. Channel：网络通道
     * 4. ChannelPipeline：处理器链
     * 5. ChannelHandler：处理器（业务逻辑）
     * 6. ChannelHandlerContext：处理器上下文
     * 7. ByteBuf：字节缓冲区（优于NIO ByteBuffer）
     */

    /**
     * Netty服务端示例
     */
    /*
    public class NettyServer {
        public static void main(String[] args) throws Exception {
            // Boss线程组：处理连接
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            // Worker线程组：处理I/O
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new ServerHandler());
                        }
                    });

                ChannelFuture future = bootstrap.bind(8080).sync();
                System.out.println("Netty服务器启动...");
                future.channel().closeFuture().sync();

            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Sharable
    public class ServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println("收到消息：" + msg);
            ctx.writeAndFlush("Echo: " + msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
    */

    // ==================== 零拷贝 ====================

    /**
     * 零拷贝技术：减少数据在内核态和用户态之间的复制
     *
     * 传统数据传输：
     *
     * ┌──────────┐   read    ┌──────────┐   write   ┌──────────┐
     * │   磁盘   │ ────────→ │  内核缓冲 │ ────────→ │ Socket缓冲│
     * │          │           │   区     │           │    区    │
     * └──────────┘           └──────────┘           └──────────┘
     *                              │        复制         │
     *                              ▼                     ▼
     *                         ┌──────────┐         ┌──────────┐
     *                         │ 用户缓冲  │         │   网卡   │
     *                         │   区     │         │          │
     *                         └──────────┘         └──────────┘
     *
     * 4次复制，4次上下文切换
     *
     * 零拷贝（sendfile）：
     *
     * ┌──────────┐  DMA复制  ┌──────────┐  DMA复制  ┌──────────┐
     * │   磁盘   │ ────────→ │ 内核缓冲 │ ────────→ │   网卡   │
     * │          │           │   区     │           │          │
     * └──────────┘           └──────────┘           └──────────┘
     *
     * 2次复制，2次上下文切换
     *
     * Netty零拷贝实现：
     * 1. CompositeByteBuf：逻辑合并多个Buffer，无需物理复制
     * 2. FileRegion：使用sendfile系统调用
     * 3. DirectBuffer：堆外内存，避免JVM堆与内核间复制
     */

    // ==================== ByteBuf ====================

    /**
     * ByteBuf vs ByteBuffer：
     *
     * ┌─────────────────┬─────────────────────┬─────────────────────┐
     * │ 特性             │ ByteBuffer          │ ByteBuf             │
     * ├─────────────────┼─────────────────────┼─────────────────────┤
     * │ 读写指针         │ 需要flip切换        │ 独立的读写指针       │
     * │ 容量扩展         │ 固定大小            │ 自动扩展            │
     * │ 内存类型         │ 堆内存              │ 堆/堆外内存         │
     * │ 引用计数         │ 无                  │ 有                  │
     * │ 池化             │ 无                  │ 有                  │
     * └─────────────────┴─────────────────────┴─────────────────────┘
     *
     * ByteBuf类型：
     * - Heap ByteBuf：堆内存，读写效率高
     * - Direct ByteBuf：堆外内存，减少一次拷贝
     * - Pooled ByteBuf：池化，减少内存分配开销
     */

    // ==================== 最佳实践 ====================

    /**
     * Netty最佳实践：
     *
     * 1. 线程数配置
     *    - Boss线程：1个即可
     *    - Worker线程：CPU核心数 * 2
     *
     * 2. 内存配置
     *    - 使用池化DirectBuffer
     *    - 合理设置ByteBuf大小
     *
     * 3. Handler设计
     *    - 避免在Handler中执行耗时操作
     *    - 使用@Sharable注解共享无状态Handler
     *
     * 4. 资源释放
     *    - 使用ReferenceCountUtil.release()
     *    - 使用SimpleChannelInboundHandler自动释放
     */
}
