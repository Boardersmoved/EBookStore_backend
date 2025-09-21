package com.example.ebookstore_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartItemQuantityDto {
    @NotNull(message = "数量不能为空")
    @Min(value = 0, message = "数量不能为负") // 允许为0，表示删除
    private Integer quantity;
}