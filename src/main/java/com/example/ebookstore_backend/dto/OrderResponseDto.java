package com.example.ebookstore_backend.dto;

import com.example.ebookstore_backend.entity.Order;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private String shippingAddress;
    private String contactPhone;
    private List<OrderItemResponseDto> items;
    private UserDto user; // 下单用户信息

    public static OrderResponseDto fromEntity(Order order) {
        if (order == null) {
            return null;
        }
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setContactPhone(order.getContactPhone());
        if (order.getOrderItems() != null) {
            dto.setItems(order.getOrderItems().stream()
                    .map(OrderItemResponseDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        if (order.getUser() != null) {
            dto.setUser(UserDto.fromEntity(order.getUser())); // 假设您有 UserDto.fromEntity()
        }
        return dto;
    }
}