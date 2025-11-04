package com.example.pricecalculatorservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class PriceCalculatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(PriceCalculatorService.class);
    

    public BigDecimal calculateTotalPrice(BigDecimal price, Integer quantity) {
        if (price == null || quantity == null) {
            logger.error("计算失败：价格或数量为空");
            throw new IllegalArgumentException("价格和数量不能为空");
        }
        
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("计算失败：价格必须大于0，当前价格: {}", price);
            throw new IllegalArgumentException("价格必须大于0");
        }
        
        if (quantity <= 0) {
            logger.error("计算失败：数量必须大于0，当前数量: {}", quantity);
            throw new IllegalArgumentException("数量必须大于0");
        }
        
        BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        
        logger.info("价格计算完成 - 单价: {}, 数量: {}, 总价: {}", price, quantity, totalPrice);
        
        return totalPrice;
    }
}

