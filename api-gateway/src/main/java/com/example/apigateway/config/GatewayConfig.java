package com.example.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

/**
 * Gateway 配置类
 * 配置全局过滤器、日志等
 */
@Configuration
public class GatewayConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayConfig.class);
    
    /**
     * 全局日志过滤器
     * 记录所有经过网关的请求
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            String method = exchange.getRequest().getMethod().toString();
            String path = exchange.getRequest().getURI().getPath();
            String query = exchange.getRequest().getURI().getQuery();
            
            logger.info("【Gateway 请求】{} {} {}", 
                method, 
                path, 
                query != null ? "?" + query : "");
            
            long startTime = System.currentTimeMillis();
            
            return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    int statusCode = exchange.getResponse().getStatusCode() != null 
                        ? exchange.getResponse().getStatusCode().value() 
                        : 0;
                    
                    logger.info("【Gateway 响应】{} {} - 状态码: {}, 耗时: {}ms", 
                        method,
                        path,
                        statusCode,
                        endTime - startTime);
                })
            );
        };
    }
}

