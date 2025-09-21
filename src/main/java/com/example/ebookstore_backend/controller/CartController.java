package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.AddCartItemRequestDto;
import com.example.ebookstore_backend.dto.CartItemDto;
import com.example.ebookstore_backend.dto.UpdateCartItemQuantityDto;
import com.example.ebookstore_backend.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // 引入 Collections
import java.util.List;
import java.util.Map; // 引入 Map

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }


    @GetMapping("/items")
    public ResponseEntity<List<CartItemDto>> getCartItems() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Accessing /api/cart/items. Current authentication: {}", authentication);
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("/api/cart/items - User is not authenticated or anonymous. Returning 401.");
        }
        List<CartItemDto> cartItems = cartService.getCartItems();
        logger.debug("Returning {} cart items for user.", cartItems.size());
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemDto> addItemToCart(@Valid @RequestBody AddCartItemRequestDto addRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Attempting to add item to cart for user: {}. Request: {}", (authentication != null ? authentication.getName() : "UNKNOWN"), addRequest);
        CartItemDto cartItemDto = cartService.addItemToCart(addRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemDto);
    }

    @PutMapping("/items/{bookId}")
    public ResponseEntity<CartItemDto> updateCartItemQuantity(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateCartItemQuantityDto quantityRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Attempting to update cart item quantity for user: {}, bookId: {}, newQuantity: {}",
                (authentication != null ? authentication.getName() : "UNKNOWN"), bookId, quantityRequest.getQuantity());
        CartItemDto updatedItem = cartService.updateCartItemQuantity(bookId, quantityRequest.getQuantity());
        if (updatedItem == null && quantityRequest.getQuantity() <=0) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(updatedItem);
    }
    /**
     * 从购物车中删除指定的商品项 (通过书籍ID)
     */
    @DeleteMapping("/items/{bookId}")
    public ResponseEntity<?> removeItemFromCart(@PathVariable Long bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Attempting to remove item from cart for user: {}, bookId: {}",
                (authentication != null ? authentication.getName() : "UNKNOWN"), bookId);
        cartService.removeItemFromCart(bookId);
        // 返回一个包含消息的JSON对象
        Map<String, String> responseMessage = Collections.singletonMap("message", "商品已从购物车成功移除。");
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * 清空当前用户购物车
     */
    @DeleteMapping("/items") // 或者 /clear
    public ResponseEntity<?> clearCart() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Attempting to clear cart for user: {}", (authentication != null ? authentication.getName() : "UNKNOWN"));
        cartService.clearCart();
        Map<String, String> responseMessage = Collections.singletonMap("message", "购物车已清空。");
        return ResponseEntity.ok(responseMessage);
    }
}