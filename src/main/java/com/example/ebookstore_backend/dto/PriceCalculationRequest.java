package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequest {
    
    /**
     * 单价
     */
    private BigDecimal price;
    
    /**
     * 数量
     */
    private Integer quantity;
}

