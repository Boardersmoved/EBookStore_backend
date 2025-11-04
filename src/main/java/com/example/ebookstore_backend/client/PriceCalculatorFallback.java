package com.example.ebookstore_backend.client;

import com.example.ebookstore_backend.dto.PriceCalculationRequest;
import com.example.ebookstore_backend.dto.PriceCalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 价格计算服务的降级处理
 * 
 * 当价格计算函数式服务不可用时，使用本地计算作为降级方案
 * 确保订单创建功能不会因为价格计算服务故障而完全失败
 */
@Component
public class PriceCalculatorFallback implements PriceCalculatorClient {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceCalculatorFallback.class);
    

    @Override
    public PriceCalculationResponse calculatePrice(PriceCalculationRequest request) {
        logger.warn("价格计算服务不可用，使用本地降级计算 - 单价: {}, 数量: {}", 
                   request.getPrice(), request.getQuantity());
        
        try {
            BigDecimal totalPrice = request.getPrice()
                    .multiply(BigDecimal.valueOf(request.getQuantity()));
            
            logger.info("本地降级计算完成 - 总价: {}", totalPrice);
            
            return new PriceCalculationResponse(
                    totalPrice, 
                    "使用本地降级计算（价格计算服务暂时不可用）"
            );
        } catch (Exception e) {
            logger.error("本地降级计算失败: {}", e.getMessage());
            return new PriceCalculationResponse(
                    BigDecimal.ZERO,
                    "计算失败: " + e.getMessage()
            );
        }
    }
}

