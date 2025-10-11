package com.example.ebookstore_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，支持 /topic（广播）和 /queue（点对点）
        config.enableSimpleBroker("/topic", "/queue");
        
        // 设置用户目标前缀（用于 convertAndSendToUser）
        config.setUserDestinationPrefix("/user");
        
        // 应用消息前缀（客户端发送消息时使用）
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket/STOMP 端点：ws(s)://<host>/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}