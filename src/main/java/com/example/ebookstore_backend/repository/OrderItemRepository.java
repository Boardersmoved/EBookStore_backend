package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    /**
     * 根据订单ID查找所有订单项
     */
    List<OrderItem> findByOrderId(Long orderId);
}