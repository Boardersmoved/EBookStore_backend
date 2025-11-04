package com.example.pricecalculatorservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceCalculationRequest {
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    private BigDecimal price;
    
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须至少为1")
    private Integer quantity;
}

