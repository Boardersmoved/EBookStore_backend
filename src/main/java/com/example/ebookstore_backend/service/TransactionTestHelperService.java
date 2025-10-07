package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.dto.CreateOrderRequestDto;
import com.example.ebookstore_backend.dto.OrderItemRequestDto;
import com.example.ebookstore_backend.entity.Order;

import java.util.List;

public interface TransactionTestHelperService {
    
    /**
     * 创建订单 - REQUIRES_NEW传播
     */
    Order createOrderRequiresNew(CreateOrderRequestDto request);
    
    /**
     * 添加订单项 - REQUIRES_NEW传播
     */
    void addOrderItemsRequiresNew(Order order, List<OrderItemRequestDto> items);
    
    /**
     * 添加订单项时发生异常 - REQUIRES_NEW传播
     */
    void addOrderItemsRequiresNewWithException(Order order, List<OrderItemRequestDto> items);
}