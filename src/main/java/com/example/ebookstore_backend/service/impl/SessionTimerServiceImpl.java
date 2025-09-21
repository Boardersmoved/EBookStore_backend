package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.service.SessionTimerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionTimerServiceImpl implements SessionTimerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionTimerServiceImpl.class);
    
    private Long startTime;
    private Long endTime;
    
    @Override
    public void startTimer() {
        this.startTime = System.currentTimeMillis();
        this.endTime = null;
        logger.info("Session timer started at: {}", startTime);
    }
    
    @Override
    public long stopTimer() {
        if (startTime == null) {
            logger.warn("Timer was not started, returning 0");
            return 0;
        }
        
        this.endTime = System.currentTimeMillis();
        long sessionDuration = (endTime - startTime) / 1000; // 转换为秒
        logger.info("Session timer stopped. Duration: {} seconds", sessionDuration);
        return sessionDuration;
    }
    
}