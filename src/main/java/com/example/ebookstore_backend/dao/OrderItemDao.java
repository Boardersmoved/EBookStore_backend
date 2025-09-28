package com.example.ebookstore_backend.dao;

import com.example.ebookstore_backend.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemDao {
    
    /**
     * 保存单个订单项
     */
    OrderItem save(OrderItem orderItem);
    
    /**
     * 批量保存订单项
     */
    List<OrderItem> saveAll(List<OrderItem> orderItems);
    
    /**
     * 根据ID查找订单项
     */
    Optional<OrderItem> findById(Long id);
    
    /**
     * 删除订单项
     */
    void delete(OrderItem orderItem);
    
    /**
     * 根据订单ID查找所有订单项
     */
    List<OrderItem> findByOrderId(Long orderId);
}