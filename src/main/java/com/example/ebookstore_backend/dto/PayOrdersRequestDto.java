package com.example.ebookstore_backend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class PayOrdersRequestDto {
    @NotEmpty(message = "请至少选择一个订单进行支付。")
    private List<Long> orderIds; // 要支付的订单ID列表
}
