package com.example.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 降级处理控制器
 * 当后端服务不可用时返回友好提示
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    /**
     * Author Service 降级处理
     */
    @GetMapping("/author")
    public Mono<Map<String, Object>> authorFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "作者服务暂时不可用，请稍后重试");
        response.put("code", 503);
        return Mono.just(response);
    }
    
    /**
     * EBookStore 降级处理
     */
    @GetMapping("/ebook-store")
    public Mono<Map<String, Object>> ebookStoreFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "电商服务暂时不可用，请稍后重试");
        response.put("code", 503);
        return Mono.just(response);
    }
    
    /**
     * 通用降级处理
     */
    @GetMapping("/default")
    public Mono<Map<String, Object>> defaultFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务暂时不可用，请稍后重试");
        response.put("code", 503);
        return Mono.just(response);
    }
}

