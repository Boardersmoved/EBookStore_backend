package com.example.authorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 作者查询微服务
 * 功能：根据书名查询作者信息
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthorServiceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("Author Service 启动成功！");
        System.out.println("服务端口: 8081");
        System.out.println("API 文档:");
        System.out.println("  - GET /api/authors/by-book?title=书名");
        System.out.println("  - GET /api/authors/search?keyword=关键词");
        System.out.println("========================================\n");
    }
}

