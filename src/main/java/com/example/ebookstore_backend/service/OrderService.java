package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.dto.CreateOrderRequestDto; // 使用更新后的DTO
import com.example.ebookstore_backend.dto.OrderResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    /**
     * 获取当前用户的订单历史，支持按时间范围和书籍名称搜索
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param bookName 书籍名称（可选）
     * @return 符合条件的订单列表
     */
    List<OrderResponseDto> getOrderHistoryForCurrentUser(LocalDateTime startTime, LocalDateTime endTime, String bookName);

    /**
     * 获取当前用户的订单历史（不包含搜索条件）
     * @return 当前用户的所有订单
     */
    List<OrderResponseDto> getOrderHistoryForCurrentUser();

    /**
     * 根据前端选中的商品项创建新订单
     * @param requestDto 包含收货地址、联系电话和选中的商品项列表
     * @return 创建成功的订单详情 (OrderResponseDto)
     * @throws com.example.ebookstore_backend.exception.EmptyCartException (现在应改为更通用的，如 "No items selected")
     * @throws com.example.ebookstore_backend.exception.InsufficientStockException
     * @throws com.example.ebookstore_backend.exception.ResourceNotFoundException 如果书籍未找到
     */
    OrderResponseDto createOrder(CreateOrderRequestDto requestDto); // 方法名可以改为更通用的 createOrder

    /**
     * 删除指定ID的订单
     * @param orderId 要删除的订单ID
     * @throws com.example.ebookstore_backend.exception.ResourceNotFoundException 如果订单未找到
     * @throws com.example.ebookstore_backend.exception.UnauthorizedOperationException 如果用户无权删除或订单状态不允许
     */
    void deleteOrder(Long orderId);

    /**
     * 支付指定的多个订单
     * @param orderIds 要支付的订单ID列表
     * @return 包含支付结果信息的列表或摘要，例如哪些成功，哪些失败
     * @throws com.example.ebookstore_backend.exception.ResourceNotFoundException 如果订单未找到
     * @throws com.example.ebookstore_backend.exception.InsufficientBalanceException 如果余额不足
     * @throws com.example.ebookstore_backend.exception.UnauthorizedOperationException 如果订单不属于用户或状态不正确
     */
    List<OrderResponseDto> payOrders(List<Long> orderIds); // 返回更新后的订单列表

    /**
     * 获取所有订单（管理员功能），支持按时间范围和书籍名称筛选
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param bookName 书籍名称（可选）
     * @return 符合条件的所有订单列表
     */
    List<OrderResponseDto> getAllOrders(LocalDateTime startTime, LocalDateTime endTime, String bookName);

    /**
     * 获取所有订单（管理员功能）- 无过滤条件
     * @return 所有订单列表
     */
    List<OrderResponseDto> getAllOrders();
}
