package com.example.ebookstore_backend.dto;

import com.example.ebookstore_backend.entity.Order;
import com.example.ebookstore_backend.entity.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FlattenedOrderItemDto {
    private Long orderItemId;       // OrderItem的ID
    private Long orderId;           // Order的ID
    private String bookName;
    private String bookCoverImageBase64;
    private BigDecimal unitPrice;     // priceAtPurchase from OrderItem
    private Integer quantity;
    private BigDecimal subtotal;      // OrderItem的subtotal
    private String shippingAddress; // from Order
    private LocalDateTime orderTime;  // orderDate from Order
    private String status;            // status from Order
    private Long userId;
    private Long bookId;
    private Boolean isAvailable;


    public static FlattenedOrderItemDto fromEntities(Order order, OrderItem orderItem) {
        if (order == null || orderItem == null || orderItem.getBook() == null) {
            return null; // 或者抛出异常，或者返回一个包含错误信息的DTO
        }
        FlattenedOrderItemDto dto = new FlattenedOrderItemDto();
        dto.setOrderItemId(orderItem.getId());
        dto.setOrderId(order.getId());

        dto.setBookId(orderItem.getBook().getId()); // 设置书籍ID
        dto.setBookName(orderItem.getBook().getTitle());
        dto.setBookCoverImageBase64(orderItem.getBook().getCoverImageBase64());
        // 使用计算属性获取价格和小计
        dto.setUnitPrice(orderItem.getPriceAtPurchase());
        dto.setQuantity(orderItem.getQuantity());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setUserId(order.getUser().getId());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setOrderTime(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setIsAvailable(orderItem.getBook().getIsAvailable());
        return dto;
    }
}
