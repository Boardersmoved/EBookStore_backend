package com.example.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API 网关
 * 提供统一入口、路由转发、负载均衡功能
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("API Gateway 启动成功！");
        System.out.println("网关地址: http://localhost:8080");
        System.out.println("路由规则:");
        System.out.println("  - /author-service/** → author-service");
        System.out.println("  - /ebook-store/** → ebook-store-backend");
        System.out.println("========================================\n");
    }
}

