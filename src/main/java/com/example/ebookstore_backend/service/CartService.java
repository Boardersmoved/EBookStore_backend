package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.dto.CartItemDto; // 我们需要一个CartItem的DTO
import com.example.ebookstore_backend.dto.AddCartItemRequestDto; // 添加商品到购物车的请求DTO

import java.util.List;

public interface CartService {

    /**
     * 获取当前登录用户的购物车内容
     * @return 当前用户的购物车项列表 (DTO格式)
     */
    List<CartItemDto> getCartItems();

    /**
     * 将书籍加入当前登录用户的购物车
     * @param addCartItemRequestDto 包含书籍ID和数量的请求DTO
     * @return 添加/更新后的购物车项 (DTO格式)
     */
    CartItemDto addItemToCart(AddCartItemRequestDto addCartItemRequestDto);

    /**
     * 更新当前登录用户购物车中某项商品的数量
     * @param bookId 书籍ID
     * @param quantity 新的数量
     * @return 更新后的购物车项 (DTO格式)
     */
    CartItemDto updateCartItemQuantity(Long bookId, Integer quantity);

    /**
     * 从当前登录用户的购物车中移除一项商品
     * @param bookId 要移除的书籍ID
     */
    void removeItemFromCart(Long bookId);

    /**
     * 清空当前登录用户的购物车
     */
    void clearCart();

    /**
     * 清除指定用户的购物车缓存
     * @param userId 用户ID
     */
    void evictCartCache(Long userId);  
}