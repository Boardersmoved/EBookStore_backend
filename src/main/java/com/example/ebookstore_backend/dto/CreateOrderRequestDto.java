package com.example.ebookstore_backend.dto;

import jakarta.validation.Valid; // 用于嵌套校验
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty; // 用于集合校验
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequestDto {

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 500, message = "收货地址长度不能超过500个字符")
    private String shippingAddress;

    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone; // 可选，或添加更严格校验

    @NotEmpty(message = "订单中必须至少包含一件商品")
    @Valid // 确保对列表中的每个OrderItemRequestDto也进行校验
    private List<OrderItemRequestDto> items; // 选中的商品项列表
}
    