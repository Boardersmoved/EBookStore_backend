package com.example.ebookstore_backend.dao;

import com.example.ebookstore_backend.entity.Order;
import com.example.ebookstore_backend.entity.OrderItem;
import com.example.ebookstore_backend.entity.User;

import java.util.List;
import java.util.Optional;

public interface OrderDao {
    // 保存订单
    Order save(Order order);

    // 根据ID查找订单
    Optional<Order> findById(Long id);

    // 删除订单
    void delete(Order order);

    // 根据用户查找订单及其关联的订单项和书籍
    List<Order> findByUserWithItemsAndBooks(User user);


    // 根据ID查找订单项
    Optional<OrderItem> findOrderItemById(Long id);

    // 删除订单项
    void deleteOrderItem(OrderItem orderItem);

    List<Order> findAll();
}