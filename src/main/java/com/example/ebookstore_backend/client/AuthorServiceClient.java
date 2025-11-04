package com.example.ebookstore_backend.client;

import com.example.ebookstore_backend.dto.AuthorResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Author Service Feign 客户端
 * 用于调用 Author Service 微服务
 * 
 * name: 在 Eureka 中注册的服务名
 * fallback: 降级处理类，当服务不可用时调用
 */
@FeignClient(name = "author-service", fallback = AuthorServiceFallback.class)
public interface AuthorServiceClient {
    
    /**
     * 根据书名精确查询作者
     * @param title 书名
     * @return 作者信息
     */
    @GetMapping("/api/authors/by-book")
    AuthorResponseDto getAuthorByBookTitle(@RequestParam("title") String title);
}

