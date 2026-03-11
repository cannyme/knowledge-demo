package com.example.demo.architecture;

/**
 * Docker与Kubernetes基础
 *
 * 【Docker】
 * 容器化技术，将应用及其依赖打包成镜像
 *
 * 【Kubernetes (K8s)】
 * 容器编排平台，管理容器化应用
 */
public class DockerKubernetes {

    // ==================== Docker核心概念 ====================

    /**
     * Docker核心概念：
     *
     * ┌─────────────────┬─────────────────────────────────────────┐
     * │ 概念             │ 说明                                     │
     * ├─────────────────┼─────────────────────────────────────────┤
     * │ Image（镜像）    │ 只读模板，包含运行应用所需的一切          │
     * │ Container（容器）│ 镜像的运行实例                           │
     * │ Dockerfile      │ 构建镜像的脚本                           │
     * │ Registry（仓库） │ 存储和分发镜像（Docker Hub）             │
     * │ Volume（卷）    │ 持久化存储                               │
     * │ Network（网络） │ 容器间通信                               │
     * └─────────────────┴─────────────────────────────────────────┘
     */

    // ==================== Dockerfile示例 ====================

    /**
     * Spring Boot应用Dockerfile
     *
     * # 基础镜像
     * FROM openjdk:17-jdk-slim
     *
     * # 工作目录
     * WORKDIR /app
     *
     * # 复制jar包
     * COPY target/myapp.jar app.jar
     *
     * # 暴露端口
     * EXPOSE 8080
     *
     * # 启动命令
     * ENTRYPOINT ["java", "-jar", "app.jar"]
     *
     * # 多阶段构建（优化镜像大小）
     * # 构建阶段
     * FROM maven:3.8-openjdk-17 AS build
     * COPY pom.xml .
     * COPY src ./src
     * RUN mvn package -DskipTests
     *
     * # 运行阶段
     * FROM openjdk:17-jdk-slim
     * COPY --from=build /target/myapp.jar app.jar
     * ENTRYPOINT ["java", "-jar", "app.jar"]
     */

    // ==================== Docker常用命令 ====================

    /**
     * # 构建镜像
     * docker build -t myapp:1.0 .
     *
     * # 运行容器
     * docker run -d -p 8080:8080 --name myapp myapp:1.0
     *
     * # 查看容器
     * docker ps
     * docker ps -a
     *
     * # 查看日志
     * docker logs myapp
     * docker logs -f myapp  # 实时查看
     *
     * # 进入容器
     * docker exec -it myapp /bin/bash
     *
     * # 停止/启动容器
     * docker stop myapp
     * docker start myapp
     *
     * # 删除容器
     * docker rm myapp
     * docker rm -f myapp  # 强制删除
     *
     * # 删除镜像
     * docker rmi myapp:1.0
     *
     * # 查看资源使用
     * docker stats
     *
     * # Docker Compose
     * docker-compose up -d
     * docker-compose down
     */

    // ==================== Docker Compose示例 ====================

    /**
     * docker-compose.yml
     *
     * version: '3.8'
     * services:
     *   app:
     *     build: .
     *     ports:
     *       - "8080:8080"
     *     environment:
     *       - SPRING_PROFILES_ACTIVE=prod
     *       - DB_HOST=db
     *     depends_on:
     *       - db
     *       - redis
     *     networks:
     *       - app-network
     *
     *   db:
     *     image: mysql:8.0
     *     environment:
     *       - MYSQL_ROOT_PASSWORD=root
     *       - MYSQL_DATABASE=mydb
     *     volumes:
     *       - db-data:/var/lib/mysql
     *     networks:
     *       - app-network
     *
     *   redis:
     *     image: redis:7
     *     networks:
     *       - app-network
     *
     * volumes:
     *   db-data:
     *
     * networks:
     *   app-network:
     */

    // ==================== Kubernetes核心概念 ====================

    /**
     * Kubernetes架构：
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                   Master Node                               │
     * │  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐ │
     * │  │ API Server│ │Scheduler  │ │Controller │ │    etcd   │ │
     * │  │           │ │           │ │  Manager  │ │           │ │
     * │  └───────────┘ └───────────┘ └───────────┘ └───────────┘ │
     * └─────────────────────────────────────────────────────────────┘
     *                            │
     *         ┌──────────────────┼──────────────────┐
     *         │                  │                  │
     *         ▼                  ▼                  ▼
     * ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
     * │  Worker Node  │  │  Worker Node  │  │  Worker Node  │
     * │ ┌───────────┐ │  │ ┌───────────┐ │  │ ┌───────────┐ │
     * │ │  kubelet  │ │  │ │  kubelet  │ │  │ │  kubelet  │ │
     * │ └───────────┘ │  │ └───────────┘ │  │ └───────────┘ │
     * │ ┌───────────┐ │  │ ┌───────────┐ │  │ ┌───────────┐ │
     * │ │  Pod 1    │ │  │ │  Pod 2    │ │  │ │  Pod 3    │ │
     * │ │  Pod 2    │ │  │ │  Pod 3    │ │  │ │  Pod 1    │ │
     * │ └───────────┘ │  │ └───────────┘ │  │ └───────────┘ │
     * └───────────────┘  └───────────────┘  └───────────────┘
     *
     * 核心组件：
     * ┌───────────────────┬─────────────────────────────────────────┐
     * │ 组件               │ 说明                                     │
     * ├───────────────────┼─────────────────────────────────────────┤
     * │ API Server        │ 集群统一入口，RESTful API                │
     * │ Scheduler         │ 调度Pod到合适的Node                       │
     * │ Controller Manager│ 控制器管理器，维护集群状态                │
     * │ etcd              │ 键值存储，保存集群数据                    │
     * │ kubelet           │ 节点代理，管理Pod生命周期                 │
     * │ kube-proxy        │ 网络代理，实现Service负载均衡             │
     * └───────────────────┴─────────────────────────────────────────┘
     */

    /**
     * Kubernetes资源对象：
     *
     * ┌───────────────────┬─────────────────────────────────────────┐
     * │ 资源               │ 说明                                     │
     * ├───────────────────┼─────────────────────────────────────────┤
     * │ Pod               │ 最小部署单元，一个或多个容器              │
     * │ Deployment        │ 管理Pod副本数量，滚动更新                 │
     * │ Service           │ 服务发现和负载均衡                       │
     * │ ConfigMap         │ 配置管理                                 │
     * │ Secret            │ 敏感信息管理                             │
     * │ PV/PVC            │ 持久化存储                               │
     * │ Ingress           │ HTTP路由，域名到Service的映射            │
     * │ Namespace         │ 资源隔离                                 │
     * │ HPA               │ 自动扩缩容                               │
     * └───────────────────┴─────────────────────────────────────────┘
     */

    // ==================== Kubernetes配置示例 ====================

    /**
     * Deployment配置
     *
     * apiVersion: apps/v1
     * kind: Deployment
     * metadata:
     *   name: myapp
     * spec:
     *   replicas: 3
     *   selector:
     *     matchLabels:
     *       app: myapp
     *   template:
     *     metadata:
     *       labels:
     *         app: myapp
     *     spec:
     *       containers:
     *       - name: myapp
     *         image: myapp:1.0
     *         ports:
     *         - containerPort: 8080
     *         resources:
     *           requests:
     *             memory: "256Mi"
     *             cpu: "250m"
     *           limits:
     *             memory: "512Mi"
     *             cpu: "500m"
     *         livenessProbe:
     *           httpGet:
     *             path: /actuator/health
     *             port: 8080
     *           initialDelaySeconds: 30
     *           periodSeconds: 10
     *         readinessProbe:
     *           httpGet:
     *             path: /actuator/health
     *             port: 8080
     *           initialDelaySeconds: 5
     *           periodSeconds: 5
     *         env:
     *         - name: SPRING_PROFILES_ACTIVE
     *           valueFrom:
     *             configMapKeyRef:
     *               name: myapp-config
     *               key: profile
     */

    /**
     * Service配置
     *
     * apiVersion: v1
     * kind: Service
     * metadata:
     *   name: myapp
     * spec:
     *   selector:
     *     app: myapp
     *   ports:
     *   - port: 80
     *     targetPort: 8080
     *   type: ClusterIP  # NodePort, LoadBalancer
     */

    /**
     * Ingress配置
     *
     * apiVersion: networking.k8s.io/v1
     * kind: Ingress
     * metadata:
     *   name: myapp-ingress
     * spec:
     *   rules:
     *   - host: myapp.example.com
     *     http:
     *       paths:
     *       - path: /
     *         pathType: Prefix
     *         backend:
     *           service:
     *             name: myapp
     *             port:
     *               number: 80
     */

    /**
     * ConfigMap配置
     *
     * apiVersion: v1
     * kind: ConfigMap
     * metadata:
     *   name: myapp-config
     * data:
     *   profile: "prod"
     *   application.yml: |
     *     server:
     *       port: 8080
     *     spring:
     *       datasource:
     *         url: jdbc:mysql://db:3306/mydb
     */

    // ==================== kubectl常用命令 ====================

    /**
     * # 查看资源
     * kubectl get pods
     * kubectl get pods -o wide
     * kubectl get deployments
     * kubectl get services
     * kubectl get all
     *
     * # 查看详情
     * kubectl describe pod myapp-xxx
     *
     * # 查看日志
     * kubectl logs myapp-xxx
     * kubectl logs -f myapp-xxx
     *
     * # 进入容器
     * kubectl exec -it myapp-xxx -- /bin/bash
     *
     * # 创建资源
     * kubectl apply -f deployment.yaml
     *
     * # 删除资源
     * kubectl delete -f deployment.yaml
     * kubectl delete pod myapp-xxx
     *
     * # 扩缩容
     * kubectl scale deployment myapp --replicas=5
     *
     * # 滚动更新
     * kubectl set image deployment/myapp myapp=myapp:2.0
     * kubectl rollout status deployment/myapp
     * kubectl rollout undo deployment/myapp  # 回滚
     *
     * # 端口转发
     * kubectl port-forward pod/myapp-xxx 8080:8080
     */

    // ==================== 最佳实践 ====================

    /**
     * Docker最佳实践：
     * 1. 使用多阶段构建减小镜像体积
     * 2. 使用.dockerignore排除不必要文件
     * 3. 不要以root用户运行容器
     * 4. 使用特定版本标签，不使用latest
     * 5. 利用缓存优化构建
     *
     * Kubernetes最佳实践：
     * 1. 配置资源限制（requests/limits）
     * 2. 配置健康检查（liveness/readiness）
     * 3. 使用ConfigMap/Secret管理配置
     * 4. 使用Helm管理复杂应用
     * 5. 配置HPA自动扩缩容
     * 6. 使用Namespace隔离环境
     */
}
