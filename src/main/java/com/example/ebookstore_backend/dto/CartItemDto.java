package com.example.ebookstore_backend.dto;

import com.example.ebookstore_backend.entity.CartItem;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CartItemDto {
    private Long id; // CartItem的ID
    private BookDto book; // 嵌套的书籍信息DTO
    private Integer quantity;


    public static CartItemDto fromEntity(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        CartItemDto dto = new CartItemDto();
        dto.setId(cartItem.getId());
        dto.setBook(BookDto.fromEntity(cartItem.getBook())); // 将Book实体转换为BookDto
        dto.setQuantity(cartItem.getQuantity());
        return dto;
    }
}