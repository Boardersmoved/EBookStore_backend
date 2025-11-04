package com.example.pricecalculatorservice.controller;

import com.example.pricecalculatorservice.dto.PriceCalculationRequest;
import com.example.pricecalculatorservice.dto.PriceCalculationResponse;
import com.example.pricecalculatorservice.service.PriceCalculatorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/price")
public class PriceCalculatorController {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceCalculatorController.class);
    private final PriceCalculatorService priceCalculatorService;
    
    public PriceCalculatorController(PriceCalculatorService priceCalculatorService) {
        this.priceCalculatorService = priceCalculatorService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<PriceCalculationResponse> calculatePrice(
            @Valid @RequestBody PriceCalculationRequest request) {
        
        logger.info("收到价格计算请求 - 单价: {}, 数量: {}", 
                   request.getPrice(), request.getQuantity());
        
        try {
            // 调用无状态服务进行计算
            BigDecimal totalPrice = priceCalculatorService.calculateTotalPrice(
                    request.getPrice(), 
                    request.getQuantity()
            );
            
            PriceCalculationResponse response = new PriceCalculationResponse(
                    totalPrice,
                    "计算成功"
            );
            
            logger.info("价格计算成功 - 总价: {}", totalPrice);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("价格计算失败: {}", e.getMessage());
            PriceCalculationResponse errorResponse = new PriceCalculationResponse(
                    BigDecimal.ZERO,
                    "计算失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 健康检查端点
     * 用于监控服务是否正常运行
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("健康检查请求");
        return ResponseEntity.ok("Price Calculator Service is running - Stateless & Scalable");
    }
    
    /**
     * 获取服务信息
     */
    @GetMapping("/info")
    public ResponseEntity<String> info() {
        String info = """
                {
                  "serviceName": "Price Calculator Service",
                  "version": "1.0.0",
                  "description": "无状态函数式服务，用于计算订单项总价",
                  "features": [
                    "无状态设计",
                    "纯函数计算",
                    "易于水平扩展",
                    "线程安全",
                    "高性能"
                  ]
                }
                """;
        return ResponseEntity.ok(info);
    }
}

