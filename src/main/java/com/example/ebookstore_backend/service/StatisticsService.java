package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.dto.BookSalesStatisticsDto;
import com.example.ebookstore_backend.dto.UserConsumptionStatisticsDto;
import com.example.ebookstore_backend.dto.UserPurchaseStatisticsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    
    /**
     * 获取指定时间范围内的书籍销量统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 按销量降序排列的书籍统计列表
     */
    List<BookSalesStatisticsDto> getBookSalesStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取指定时间范围内的用户消费统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 按消费金额降序排列的用户统计列表
     */
    List<UserConsumptionStatisticsDto> getUserConsumptionStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取当前用户在指定时间范围内的购书统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户购书统计详情
     */
    UserPurchaseStatisticsDto getCurrentUserPurchaseStatistics(LocalDateTime startTime, LocalDateTime endTime);
}