package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.dao.OrderDao;
import com.example.ebookstore_backend.dto.BookSalesStatisticsDto;
import com.example.ebookstore_backend.dto.UserConsumptionStatisticsDto;
import com.example.ebookstore_backend.dto.UserPurchaseStatisticsDto;
import com.example.ebookstore_backend.dto.BookPurchaseDetailDto;
import com.example.ebookstore_backend.entity.Order;
import com.example.ebookstore_backend.entity.OrderItem;
import com.example.ebookstore_backend.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    
    private final OrderDao orderDao;
    
    @Autowired
    public StatisticsServiceImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BookSalesStatisticsDto> getBookSalesStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Generating book sales statistics from {} to {}", startTime, endTime);
        
        // 获取所有已支付的订单
        List<Order> paidOrders = orderDao.findAll().stream()
                .filter(order -> "PAID".equalsIgnoreCase(order.getStatus()))
                .filter(order -> {

                    if (startTime != null && order.getOrderDate().isBefore(startTime.plusHours(8))) {
                        return false;
                    }
                    if (endTime != null && order.getOrderDate().isAfter(endTime.plusHours(8))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // 统计每本书的销量
        Map<Long, BookSalesStatisticsDto> bookStatsMap = new HashMap<>();
        
        for (Order order : paidOrders) {
            for (OrderItem item : order.getOrderItems()) {
                Long bookId = item.getBook().getId();
                
                BookSalesStatisticsDto stats = bookStatsMap.computeIfAbsent(bookId, 
                    k -> new BookSalesStatisticsDto(
                        bookId,
                        item.getBook().getTitle(),
                        item.getBook().getAuthor(),
                        0,
                        BigDecimal.ZERO,
                        item.getBook().getCoverImageBase64()
                    ));
                
                stats.setTotalSales(stats.getTotalSales() + item.getQuantity());
                // 使用计算属性获取小计
                stats.setTotalRevenue(stats.getTotalRevenue().add(item.getSubtotal()));
            }
        }
        
        // 按销量降序排序
        List<BookSalesStatisticsDto> result = bookStatsMap.values().stream()
                .sorted((a, b) -> b.getTotalSales().compareTo(a.getTotalSales()))
                .collect(Collectors.toList());
        
        logger.info("Generated book sales statistics for {} books", result.size());
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserConsumptionStatisticsDto> getUserConsumptionStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Generating user consumption statistics from {} to {}", startTime, endTime);
        
        // 获取所有已支付的订单
        List<Order> paidOrders = orderDao.findAll().stream()
                .filter(order -> "PAID".equalsIgnoreCase(order.getStatus()))
                .filter(order -> {
                    if (startTime != null && order.getOrderDate().isBefore(startTime.plusHours(8))) {
                        return false;
                    }
                    if (endTime != null && order.getOrderDate().isAfter(endTime.plusHours(8))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // 统计每个用户的消费情况
        Map<Long, UserConsumptionStatisticsDto> userStatsMap = new HashMap<>();
        
        for (Order order : paidOrders) {
            Long userId = order.getUser().getId();
            
            UserConsumptionStatisticsDto stats = userStatsMap.computeIfAbsent(userId,
                k -> new UserConsumptionStatisticsDto(
                    userId,
                    order.getUser().getUsername(),
                    order.getUser().getEmail(),
                    0,
                    BigDecimal.ZERO,
                    0,
                    order.getUser().getAvatarBase64()
                ));
            
            stats.setTotalOrders(stats.getTotalOrders() + 1);
            stats.setTotalConsumption(stats.getTotalConsumption().add(order.getTotalAmount()));
            
            // 计算购买的书籍总数量
            int bookCount = order.getOrderItems().stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
            stats.setTotalBooksPurchased(stats.getTotalBooksPurchased() + bookCount);
        }
        
        // 按消费金额降序排序
        List<UserConsumptionStatisticsDto> result = userStatsMap.values().stream()
                .sorted((a, b) -> b.getTotalConsumption().compareTo(a.getTotalConsumption()))
                .collect(Collectors.toList());
        
        logger.info("Generated user consumption statistics for {} users", result.size());
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserPurchaseStatisticsDto getCurrentUserPurchaseStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("用户未登录");
        }
        
        String currentUsername = authentication.getName();
        logger.info("Generating purchase statistics for user '{}' from {} to {}", currentUsername, startTime, endTime);
        
        // 获取当前用户的所有已支付订单
        List<Order> userPaidOrders = orderDao.findAll().stream()
                .filter(order -> "PAID".equalsIgnoreCase(order.getStatus()))
                .filter(order -> currentUsername.equals(order.getUser().getUsername()))
                .filter(order -> {
                    if (startTime != null && order.getOrderDate().isBefore(startTime.plusHours(8))) {
                        return false;
                    }
                    if (endTime != null && order.getOrderDate().isAfter(endTime.plusHours(8))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // 统计每本书的购买情况
        Map<Long, BookPurchaseDetailDto> bookDetailsMap = new HashMap<>();
        int totalBooksPurchased = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Order order : userPaidOrders) {
            for (OrderItem item : order.getOrderItems()) {
                Long bookId = item.getBook().getId();
                
                BookPurchaseDetailDto detail = bookDetailsMap.computeIfAbsent(bookId,
                    k -> new BookPurchaseDetailDto(
                        bookId,
                        item.getBook().getTitle(),
                        item.getBook().getAuthor(),
                        item.getBook().getCoverImageBase64(),
                        0,
                        BigDecimal.ZERO,
                        order.getOrderDate(), // 初始设为当前订单时间
                        order.getOrderDate(), // 初始设为当前订单时间
                        0
                    ));
                
                // 更新统计数据
                detail.setTotalQuantity(detail.getTotalQuantity() + item.getQuantity());
                // 使用计算属性获取小计
                detail.setTotalSpent(detail.getTotalSpent().add(item.getSubtotal()));
                detail.setOrderCount(detail.getOrderCount() + 1);
                
                // 更新首次和最近购买时间
                if (order.getOrderDate().isBefore(detail.getFirstPurchaseDate())) {
                    detail.setFirstPurchaseDate(order.getOrderDate());
                }
                if (order.getOrderDate().isAfter(detail.getLastPurchaseDate())) {
                    detail.setLastPurchaseDate(order.getOrderDate());
                }
                
                // 累计总数
                totalBooksPurchased += item.getQuantity();
                // 使用计算属性获取小计
                totalAmount = totalAmount.add(item.getSubtotal());
            }
        }
        
        // 按购买数量降序排序
        List<BookPurchaseDetailDto> bookDetailsList = bookDetailsMap.values().stream()
                .sorted((a, b) -> b.getTotalQuantity().compareTo(a.getTotalQuantity()))
                .collect(Collectors.toList());
        
        UserPurchaseStatisticsDto result = new UserPurchaseStatisticsDto(
            totalBooksPurchased,
            totalAmount,
            bookDetailsList
        );
        
        logger.info("Generated purchase statistics for user '{}': {} books, {} yuan", 
                   currentUsername, totalBooksPurchased, totalAmount);
        
        return result;
    }
}