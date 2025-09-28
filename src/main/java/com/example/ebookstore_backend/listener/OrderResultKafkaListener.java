package com.example.ebookstore_backend.listener;

import com.example.ebookstore_backend.dto.OrderResultMessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderResultKafkaListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderResultKafkaListener.class);
    
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public OrderResultKafkaListener(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "order-result-topic", groupId = "order-result-monitoring-group")
    public void monitorOrderResult(ConsumerRecord<String, String> record) {
        String messageId = record.key();
        String messageValue = record.value();
        
        try {
            OrderResultMessageDto result = objectMapper.readValue(messageValue, OrderResultMessageDto.class);

            // 推送到以 messageId 为路由键的主题，前端按 messageId 订阅
            String destination = "/topic/order-results/" + messageId;
            messagingTemplate.convertAndSend(destination, result);

            if (result.isSuccess()) {
                logger.info("✅ 订单处理成功 - 用户: {}, 订单ID: {}, 金额: {}, 消息ID: {}", 
                        result.getUsername(), result.getOrderId(), result.getTotalAmount(), messageId);
                
                // 控制台输出成功信息
                System.out.println("========================================");
                System.out.println("🎉 订单处理成功！");
                System.out.println("用户: " + result.getUsername());
                System.out.println("订单ID: " + result.getOrderId());
                System.out.println("订单金额: ¥" + result.getTotalAmount());
                System.out.println("处理时间: " + result.getProcessTime());
                System.out.println("消息ID: " + messageId);
                System.out.println("========================================");
                
            } else {
                logger.error("❌ 订单处理失败 - 用户: {}, 错误: {}, 消息ID: {}", 
                        result.getUsername(), result.getMessage(), messageId);
                
                // 控制台输出失败信息
                System.out.println("========================================");
                System.out.println("❌ 订单处理失败！");
                System.out.println("用户: " + result.getUsername());
                System.out.println("失败原因: " + result.getMessage());
                System.out.println("错误详情: " + result.getErrorDetails());
                System.out.println("处理时间: " + result.getProcessTime());
                System.out.println("消息ID: " + messageId);
                System.out.println("========================================");
            }
        } catch (JsonProcessingException e) {
            logger.error("❌ 解析订单结果消息失败，消息ID: {}", messageId, e);
        } catch (Exception e) {
            logger.error("❌ 处理订单结果时发生异常，消息ID: {}", messageId, e);
        }
    }
}