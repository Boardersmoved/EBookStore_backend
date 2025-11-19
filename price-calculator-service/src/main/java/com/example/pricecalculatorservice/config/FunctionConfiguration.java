package com.example.pricecalculatorservice.config;

import com.example.pricecalculatorservice.dto.PriceCalculationRequest;
import com.example.pricecalculatorservice.dto.PriceCalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.function.Function;


@Configuration
public class FunctionConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(FunctionConfiguration.class);
    
    @Bean
    public Function<PriceCalculationRequest, PriceCalculationResponse> calculatePrice() {
        return request -> {
            logger.info("收到价格计算请求 - 单价: {}, 数量: {}", 
                       request.getPrice(), request.getQuantity());
            
            try {
                // 参数验证
                validateRequest(request);
                
                // 纯函数计算
                BigDecimal totalPrice = request.getPrice()
                        .multiply(BigDecimal.valueOf(request.getQuantity()));
                
                logger.info("价格计算成功 - 总价: {}", totalPrice);
                
                return new PriceCalculationResponse(totalPrice, "计算成功");
                
            } catch (IllegalArgumentException e) {
                logger.error("价格计算失败: {}", e.getMessage());
                return new PriceCalculationResponse(
                        BigDecimal.ZERO, 
                        "计算失败: " + e.getMessage()
                );
            }
        };
    }
    
    @Bean
    public Function<PriceCalculationRequest, BigDecimal> simplePriceCalculator() {
        return request -> request.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));
    }
    
    private void validateRequest(PriceCalculationRequest request) {
        if (request.getPrice() == null || request.getQuantity() == null) {
            throw new IllegalArgumentException("价格和数量不能为空");
        }
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("价格必须大于0");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
    }
}

