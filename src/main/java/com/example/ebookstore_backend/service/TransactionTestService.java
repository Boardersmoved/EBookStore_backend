package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.dto.CreateOrderRequestDto;
import com.example.ebookstore_backend.dto.OrderResponseDto;

public interface TransactionTestService {
    
    // 测试案例1：正常情况
    OrderResponseDto testCase1_Normal(CreateOrderRequestDto request);
    
    // 测试案例2：创建订单时发生异常
    OrderResponseDto testCase2_CreateOrderException(CreateOrderRequestDto request);
    
    // 测试案例3：添加订单项时发生异常
    OrderResponseDto testCase3_AddOrderItemException(CreateOrderRequestDto request);
    
    // 测试案例4：添加订单项后发生异常
    OrderResponseDto testCase4_ExceptionAfterAddOrderItem(CreateOrderRequestDto request);
    
    // 测试案例5：REQUIRES_NEW 正常情况
    OrderResponseDto testCase5_RequiresNewNormal(CreateOrderRequestDto request);
    
    // 测试案例6：REQUIRES_NEW 在添加订单项之前发生异常
    OrderResponseDto testCase6_RequiresNewExceptionBefore(CreateOrderRequestDto request);
    
    // 测试案例7：REQUIRES_NEW 在添加订单项之后发生异常
    OrderResponseDto testCase7_RequiresNewExceptionAfter(CreateOrderRequestDto request);
    
    // 测试案例8：REQUIRES_NEW 添加订单项时发生异常
    OrderResponseDto testCase8_RequiresNewOrderItemException(CreateOrderRequestDto request);
    
    // 测试案例9：REQUIRES_NEW 创建订单和添加订单项都正常，但最后发生异常
    OrderResponseDto testCase9_RequiresNewBothOperationsWithException(CreateOrderRequestDto request);
}