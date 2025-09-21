package com.example.ebookstore_backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    /**
     * 创建订单请求Topic
     */
    @Bean
    public NewTopic orderRequestTopic() {
        return TopicBuilder.name("order-request-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 创建订单结果Topic
     */
    @Bean
    public NewTopic orderResultTopic() {
        return TopicBuilder.name("order-result-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }
}