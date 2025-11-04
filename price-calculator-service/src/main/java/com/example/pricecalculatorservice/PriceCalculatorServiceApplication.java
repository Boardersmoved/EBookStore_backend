package com.example.pricecalculatorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class PriceCalculatorServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PriceCalculatorServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("Price Calculator Service Started!");
        System.out.println("Port: 8084");
        System.out.println("========================================");
    }
}

