# 多阶段构建 - 第一阶段：编译
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 编译打包（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# 第二阶段：运行
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 创建非 root 用户以提高安全性
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

# 从构建阶段复制 jar 文件
COPY --from=builder /build/target/price-calculator-service-1.0.0.jar app.jar

# 更改所有权
RUN chown -R appuser:appuser /app

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8084

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/api/price/health || exit 1

# JVM 优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

