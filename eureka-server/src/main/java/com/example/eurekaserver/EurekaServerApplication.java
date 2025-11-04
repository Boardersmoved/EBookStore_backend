package com.example.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka 服务注册中心
 * 提供服务注册、服务发现、健康检查功能
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Eureka Server 启动成功！");
        System.out.println("访问控制台: http://localhost:8761");
        System.out.println("========================================\n");
    }
}

