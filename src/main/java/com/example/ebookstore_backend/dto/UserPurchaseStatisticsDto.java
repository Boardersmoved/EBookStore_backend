package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPurchaseStatisticsDto {
    private Integer totalBooksPurchased; // 购书总本数
    private BigDecimal totalAmount; // 购书总金额
    private List<BookPurchaseDetailDto> bookDetails; // 每种书的购买详情
}