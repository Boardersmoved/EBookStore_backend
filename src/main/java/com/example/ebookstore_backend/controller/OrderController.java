package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.CreateOrderRequestDto;
import com.example.ebookstore_backend.dto.FlattenedOrderItemDto;
import com.example.ebookstore_backend.dto.OrderResponseDto;
import com.example.ebookstore_backend.dto.PayOrdersRequestDto;
import com.example.ebookstore_backend.dto.OrderRequestMessageDto;
import com.example.ebookstore_backend.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderController(OrderService orderService, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 异步处理订单创建（使用Kafka）
     * @param createOrderRequestDto 包含收货地址、联系电话和选中的商品项列表
     * @return 订单请求已提交的响应
     */
    @PostMapping("/async")
    public ResponseEntity<?> createOrderAsync(@Valid @RequestBody CreateOrderRequestDto createOrderRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous (error if reached here for createOrderAsync)";
        
        logger.info("User '{}' is attempting to create an order asynchronously with {} items.", 
                   currentUsername, createOrderRequestDto.getItems().size());

        try {
            // 生成唯一的消息ID
            String messageId = UUID.randomUUID().toString();
            
            // 创建Kafka消息包装
            OrderRequestMessageDto kafkaMessage = new OrderRequestMessageDto(
                messageId,
                currentUsername,
                LocalDateTime.now(),
                createOrderRequestDto  // 直接使用现有的DTO
            );
            
            // 序列化消息
            String messageJson = objectMapper.writeValueAsString(kafkaMessage);
            
            // 发送到Kafka
            kafkaTemplate.send("order-request-topic", messageId, messageJson);
            
            logger.info("Order request sent to Kafka with messageId: {} for user: {}", messageId, currentUsername);
            
            return ResponseEntity.ok(Map.of(
                "message", "订单请求已提交，正在异步处理中...",
                "messageId", messageId,
                "status", "PROCESSING"
            ));
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize order message for user: {}", currentUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "订单请求处理失败，请稍后重试。"));
        } catch (Exception e) {
            logger.error("Unexpected error while processing async order for user: {}", currentUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "系统错误，请稍后重试。"));
        }
    }

    /**
     * 获取当前用户的订单历史
     */
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

    /**
     * 管理员获取所有订单
     */
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
     * 同步创建订单（原有功能）
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody CreateOrderRequestDto createOrderRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous (error if reached here for createOrder)";
        logger.info("User '{}' is attempting to create an order with selected items.", currentUsername);

        OrderResponseDto createdOrder = orderService.createOrder(createOrderRequestDto);
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
