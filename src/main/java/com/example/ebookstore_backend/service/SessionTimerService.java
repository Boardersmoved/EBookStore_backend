package com.example.ebookstore_backend.service;

public interface SessionTimerService {
    /**
     * 开始计时
     */
    void startTimer();
    
    /**
     * 停止计时并返回会话时长（秒）
     * @return 会话时长（秒）
     */
    long stopTimer();
    
}