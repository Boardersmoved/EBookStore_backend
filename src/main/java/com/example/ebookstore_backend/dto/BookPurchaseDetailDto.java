package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookPurchaseDetailDto {
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private String coverImageBase64;
    private Integer totalQuantity; // 该书购买总数量
    private BigDecimal totalSpent; // 该书花费总金额
    private LocalDateTime firstPurchaseDate; // 首次购买时间
    private LocalDateTime lastPurchaseDate; // 最近购买时间
    private Integer orderCount; // 购买该书的订单数量
}