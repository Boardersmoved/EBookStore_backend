package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConsumptionStatisticsDto {
    private Long userId;
    private String username;
    private String email;
    private Integer totalOrders; // 总订单数
    private BigDecimal totalConsumption; // 总消费金额
    private Integer totalBooksPurchased; // 总购买书籍数量
    private String avatarBase64; // 用户头像，用于前端展示
}