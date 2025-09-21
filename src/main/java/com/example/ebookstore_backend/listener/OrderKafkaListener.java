package com.example.ebookstore_backend.listener;

import com.example.ebookstore_backend.dto.CreateOrderRequestDto;
import com.example.ebookstore_backend.dto.OrderRequestMessageDto;
import com.example.ebookstore_backend.dto.OrderResponseDto;
import com.example.ebookstore_backend.dto.OrderResultMessageDto;
import com.example.ebookstore_backend.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderKafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderKafkaListener.class);
    
    private final OrderService orderService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;

    @Autowired
    public OrderKafkaListener(OrderService orderService, 
                             KafkaTemplate<String, String> kafkaTemplate,
                             ObjectMapper objectMapper,
                             UserDetailsService userDetailsService) {
        this.orderService = orderService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
    }

    @KafkaListener(topics = "order-request-topic", groupId = "order-processing-group")
    public void processOrderRequest(ConsumerRecord<String, String> record) {
        String messageId = record.key();
        String messageValue = record.value();
        
        logger.info("📨 接收到订单请求消息，ID: {}", messageId);
        
        OrderResultMessageDto result = new OrderResultMessageDto();
        result.setMessageId(messageId);
        result.setProcessTime(LocalDateTime.now());
        
        try {
            // 解析消息
            OrderRequestMessageDto requestMessage = objectMapper.readValue(messageValue, OrderRequestMessageDto.class);
            result.setUsername(requestMessage.getUsername());
            
            logger.info("🔄 开始处理用户 {} 的订单，包含 {} 件商品", 
                       requestMessage.getUsername(), requestMessage.getOrderRequest().getItems().size());
            
            // 设置用户认证上下文
            setAuthenticationContext(requestMessage.getUsername());
            
            // 直接使用现有的DTO调用订单服务
            OrderResponseDto createdOrder = orderService.createOrder(requestMessage.getOrderRequest());
            
            // 设置成功结果
            result.setSuccess(true);
            result.setMessage("订单创建成功");
            result.setOrderId(createdOrder.getOrderId());
            result.setTotalAmount(createdOrder.getTotalAmount());
            
            logger.info("✅ 订单创建成功，订单ID: {}，用户: {}，通过Kafka异步处理", 
                       createdOrder.getOrderId(), requestMessage.getUsername());
            
        } catch (JsonProcessingException e) {
            logger.error("❌ 解析订单消息失败，消息ID: {}", messageId, e);
            result.setSuccess(false);
            result.setMessage("消息解析失败");
            result.setErrorDetails(e.getMessage());
            
        } catch (Exception e) {
            logger.error("❌ 处理订单请求失败，消息ID: {}", messageId, e);
            result.setSuccess(false);
            result.setMessage("订单处理失败: " + e.getMessage());
            result.setErrorDetails(e.getClass().getSimpleName() + ": " + e.getMessage());
            
        } finally {
            // 清除认证上下文
            SecurityContextHolder.clearContext();
            
            // 发送处理结果到结果Topic
            sendOrderResult(result);
        }
    }

    /**
     * 设置用户认证上下文
     */
    private void setAuthenticationContext(String username) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("🔐 为用户 {} 设置认证上下文", username);
        } catch (Exception e) {
            logger.error("❌ 为用户 {} 设置认证上下文失败", username, e);
            throw new RuntimeException("无法认证用户: " + username, e);
        }
    }

    /**
     * 发送订单处理结果到结果Topic
     */
    private void sendOrderResult(OrderResultMessageDto result) {
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            kafkaTemplate.send("order-result-topic", result.getMessageId(), resultJson);
            logger.info("📤 发送订单处理结果，消息ID: {}，处理结果: {}", 
                       result.getMessageId(), result.isSuccess() ? "成功" : "失败");
        } catch (JsonProcessingException e) {
            logger.error("❌ 序列化订单结果失败，消息ID: {}", result.getMessageId(), e);
        } catch (Exception e) {
            logger.error("❌ 发送订单结果失败，消息ID: {}", result.getMessageId(), e);
        }
    }
}