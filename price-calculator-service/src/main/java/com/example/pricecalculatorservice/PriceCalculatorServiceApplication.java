package com.example.pricecalculatorservice;

import com.example.pricecalculatorservice.dto.PriceCalculationRequest;
import com.example.pricecalculatorservice.dto.PriceCalculationResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.function.Function;

@SpringBootApplication
@EnableDiscoveryClient
public class PriceCalculatorServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PriceCalculatorServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("Price Calculator Function Service Started!");
        System.out.println("Port: 8084");
        System.out.println("Available Functions:");
        System.out.println("  - POST /calculatePrice (同步版本)");
        System.out.println("  - POST /calculatePriceReactive (响应式版本)");
        System.out.println("========================================");
    }
    
}

