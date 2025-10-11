package com.example.ebookstore_backend.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    
    // 使用线程安全的集合维护Session
    private static final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // 从认证信息中获取用户名
        if (headerAccessor.getUser() != null) {
            String username = headerAccessor.getUser().getName();
            sessionUserMap.put(sessionId, username);
            logger.info("✅ WebSocket连接建立 - Session: {}, 用户: {}", sessionId, username);
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = sessionUserMap.remove(sessionId);
        
        if (username != null) {
            logger.info("❌ WebSocket连接断开 - Session: {}, 用户: {}", sessionId, username);
        }
    }
    
    public static ConcurrentHashMap<String, String> getSessionUserMap() {
        return sessionUserMap;
    }
}
