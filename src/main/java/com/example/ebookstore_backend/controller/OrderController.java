package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.CreateOrderRequestDto; // 使用更新后的DTO
import com.example.ebookstore_backend.dto.FlattenedOrderItemDto;
import com.example.ebookstore_backend.dto.OrderResponseDto;
import com.example.ebookstore_backend.dto.PayOrdersRequestDto;
import com.example.ebookstore_backend.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getCurrentUserOrderHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String bookName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous";
        logger.info("User '{}' is requesting their order history with filters: startTime={}, endTime={}, bookName={}", 
                currentUsername, startTime, endTime, bookName);

        List<OrderResponseDto> orderHistory = orderService.getOrderHistoryForCurrentUser(startTime, endTime, bookName);
        return ResponseEntity.ok(orderHistory);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) String bookName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous";
        logger.info("Admin user '{}' is requesting all orders with filters: startTime={}, endTime={}, bookName={}", 
                   currentUsername, startTime, endTime, bookName);
        List<OrderResponseDto> allOrders = orderService.getAllOrders(startTime, endTime, bookName);
        return ResponseEntity.ok(allOrders);
    }

    /**
     * 根据前端选中的商品项创建新订单
     * @param createOrderRequestDto 包含收货地址、联系电话和选中的商品项列表
     * @return 创建的订单详情
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto createOrderRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous (error if reached here for createOrder)";
        logger.info("User '{}' is attempting to create an order with selected items.", currentUsername);

        OrderResponseDto createdOrder = orderService.createOrder(createOrderRequestDto); // 调用更新后的service方法

        logger.info("Order (ID: {}) created successfully from selected items for user '{}'.", createdOrder.getOrderId(), currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }


    /**
     * 处理支付多个订单的请求
     * @param payOrdersRequestDto 包含要支付的订单ID列表
     * @return 支付成功的订单列表或错误信息
     */
    @PostMapping("/pay")
    public ResponseEntity<?> paySelectedOrders(@Valid @RequestBody PayOrdersRequestDto payOrdersRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous (error if reached here for payOrders)";
        logger.info("User '{}' attempting to pay for order IDs: {}", currentUsername, payOrdersRequestDto.getOrderIds());

        // OrderService.payOrders 会在不符合条件时抛出异常 (由GlobalExceptionHandler处理)
        // 例如，如果部分订单支付失败，它可能只返回成功支付的订单，或抛出包含错误详情的异常
        List<OrderResponseDto> paidOrders = orderService.payOrders(payOrdersRequestDto.getOrderIds());

        if (paidOrders.isEmpty() && !payOrdersRequestDto.getOrderIds().isEmpty()) {
            // 如果请求支付的订单列表不为空，但最终成功支付的列表为空，可能所有订单都支付失败了
            // 这通常意味着Service层内部处理了错误并返回空列表，或者所有订单都不符合支付条件
            logger.warn("User '{}' requested payment for orders but none were successfully paid.", currentUsername);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "支付失败，可能原因：库存不足、书籍已下架、余额不足。"));
        }

        logger.info("Successfully processed payment for {} orders for user '{}'.", paidOrders.size(), currentUsername);
        return ResponseEntity.ok(Map.of("message", "支付处理完成。", "paidOrders", paidOrders));
    }

    /**
     * 删除指定的订单
     * @param orderId 订单的ID
     * @return 成功或失败的响应
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous (error if reached here for deleteOrder)";
        logger.info("User '{}' attempting to delete order ID: {}", currentUsername, orderId);

        orderService.deleteOrder(orderId);

        Map<String, String> responseMessage = Collections.singletonMap("message", "订单已成功删除。");
        return ResponseEntity.ok(responseMessage);
    }
}
