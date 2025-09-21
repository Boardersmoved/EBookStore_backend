package com.example.ebookstore_backend.dto;

import com.example.ebookstore_backend.entity.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class OrderItemResponseDto {
    private Long bookId;
    private String bookTitle;
    private String bookCoverImageBase64;
    private Integer quantity;
    private BigDecimal priceAtPurchase; // 从关联的书籍获取当前价格
    private BigDecimal subtotal; // 通过计算得出
    private Boolean isAvailable;

    public static OrderItemResponseDto fromEntity(OrderItem orderItem) {
        if (orderItem == null || orderItem.getBook() == null) {
            return null;
        }
        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setBookId(orderItem.getBook().getId());
        dto.setBookTitle(orderItem.getBook().getTitle());
        dto.setBookCoverImageBase64(orderItem.getBook().getCoverImageBase64());
        dto.setQuantity(orderItem.getQuantity());
        // 使用计算属性获取价格和小计
        dto.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setIsAvailable(orderItem.getBook().getIsAvailable());
        return dto;
    }
}