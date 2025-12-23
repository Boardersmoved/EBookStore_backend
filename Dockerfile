# 多阶段构建 - 第一阶段：构建应用
FROM maven:3.9-eclipse-temurin-17 AS build

# 设置工作目录
WORKDIR /app

# 复制pom.xml和源代码
COPY pom.xml .
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# 第二阶段：运行应用
FROM eclipse-temurin:17-jre

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=build /app/target/*.jar app.jar

# 暴露应用端口
EXPOSE 8080

# 设置JVM参数和启动命令
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]