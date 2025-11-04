package com.example.pricecalculatorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationResponse {
    
    /**
     * 计算得出的总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 响应消息
     */
    private String message;
}

