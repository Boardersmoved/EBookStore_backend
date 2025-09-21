package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.BookSalesStatisticsDto;
import com.example.ebookstore_backend.dto.UserConsumptionStatisticsDto;
import com.example.ebookstore_backend.dto.UserPurchaseStatisticsDto;
import com.example.ebookstore_backend.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);
    
    private final StatisticsService statisticsService;
    
    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    
    /**
     * 获取书籍销量统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 书籍销量统计列表
     */
    @GetMapping("/book-sales")
    public ResponseEntity<List<BookSalesStatisticsDto>> getBookSalesStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous";
        
        logger.info("Admin user '{}' is requesting book sales statistics from {} to {}", 
                   currentUsername, startTime, endTime);
        
        List<BookSalesStatisticsDto> statistics = statisticsService.getBookSalesStatistics(startTime, endTime);
        
        logger.info("Returning {} book sales statistics records for admin user '{}'", 
                   statistics.size(), currentUsername);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取用户消费统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户消费统计列表
     */
    @GetMapping("/user-consumption")
    public ResponseEntity<List<UserConsumptionStatisticsDto>> getUserConsumptionStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous";
        
        logger.info("Admin user '{}' is requesting user consumption statistics from {} to {}", 
                   currentUsername, startTime, endTime);
        
        List<UserConsumptionStatisticsDto> statistics = statisticsService.getUserConsumptionStatistics(startTime, endTime);
        
        logger.info("Returning {} user consumption statistics records for admin user '{}'", 
                   statistics.size(), currentUsername);
        
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取当前用户的购书统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 当前用户的购书统计详情
     */
    @GetMapping("/my-purchases")
    public ResponseEntity<UserPurchaseStatisticsDto> getMyPurchaseStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous";
        
        logger.info("User '{}' is requesting personal purchase statistics from {} to {}", 
                   currentUsername, startTime, endTime);
        
        UserPurchaseStatisticsDto statistics = statisticsService.getCurrentUserPurchaseStatistics(startTime, endTime);
        
        logger.info("Returning purchase statistics for user '{}': {} books purchased, total amount: {}", 
                   currentUsername, statistics.getTotalBooksPurchased(), statistics.getTotalAmount());
        
        return ResponseEntity.ok(statistics);
    }
}