package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSalesStatisticsDto {
    private Long bookId;
    private String bookTitle;
    private String bookAuthor;
    private Integer totalSales; // 销售数量
    private BigDecimal totalRevenue; // 销售总金额
    private String coverImageBase64; // 书籍封面，用于前端展示
}