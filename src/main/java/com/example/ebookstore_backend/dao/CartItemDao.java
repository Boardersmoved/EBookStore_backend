package com.example.ebookstore_backend.dao;

import com.example.ebookstore_backend.entity.CartItem;
import java.util.List;
import java.util.Optional;

public interface CartItemDao {
    // 基础 CRUD 操作
    CartItem save(CartItem cartItem);

    // 从 CartItemRepository 中提取的特定查询方法
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);

    // 删除操作
    void deleteByUserIdAndBookId(Long userId, Long bookId);
    void deleteByUserId(Long userId);

    void deleteByUserIdAndBookIdIn(Long id, List<Long> orderedBookIds);
}