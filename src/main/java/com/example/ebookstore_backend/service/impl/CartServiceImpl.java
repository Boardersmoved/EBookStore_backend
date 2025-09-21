package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.CartItem;
import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.dto.AddCartItemRequestDto;
import com.example.ebookstore_backend.dto.CartItemDto;
import com.example.ebookstore_backend.exception.ResourceNotFoundException; // 需要创建此异常
import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.dao.CartItemDao;
import com.example.ebookstore_backend.service.AuthService; // 用于获取当前用户
import com.example.ebookstore_backend.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemDao cartItemDao;
    private final BookDao bookDao;
    private final AuthService authService; // 用于获取当前登录用户

    public CartServiceImpl(CartItemDao cartItemDao, BookDao bookDao, AuthService authService) {
        this.cartItemDao = cartItemDao;
        this.bookDao = bookDao;
        this.authService = authService;
    }


    private User getCurrentUser() {
        User user = authService.getCurrentAuthenticatedUser();
        if (user == null) {
            // 这个异常应该由Spring Security的未认证访问处理机制捕获，
            // 但作为服务层防御性编程，可以抛出。
            throw new IllegalStateException("用户未登录，无法执行购物车操作。");
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems() {
        User currentUser = getCurrentUser();
        return cartItemDao.findByUserId(currentUser.getId())
                .stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItemDto addItemToCart(AddCartItemRequestDto addRequest) {
        User currentUser = getCurrentUser();
        Book book = bookDao.findById(addRequest.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("无法将书籍加入购物车：未找到ID为 " + addRequest.getBookId() + " 的书籍。"));

        // 检查库存 (可选，但推荐)
        if (book.getStockQuantity() < addRequest.getQuantity()) {
            throw new RuntimeException("库存不足，无法添加 " + addRequest.getQuantity() + " 件《" + book.getTitle() + "》到购物车。");
        }

        Optional<CartItem> existingCartItemOpt = cartItemDao.findByUserIdAndBookId(currentUser.getId(), book.getId());

        CartItem cartItemToSave;
        if (existingCartItemOpt.isPresent()) {
            // 商品已在购物车，更新数量
            cartItemToSave = existingCartItemOpt.get();
            int newQuantity = cartItemToSave.getQuantity() + addRequest.getQuantity();
            if (book.getStockQuantity() < newQuantity) {
                throw new RuntimeException("库存不足，无法将总计 " + newQuantity + " 件《" + book.getTitle() + "》加入购物车。");
            }
            cartItemToSave.setQuantity(newQuantity);
        } else {
            // 商品不在购物车，新增
            cartItemToSave = new CartItem(currentUser, book, addRequest.getQuantity());
        }
        CartItem savedCartItem = cartItemDao.save(cartItemToSave);
        return CartItemDto.fromEntity(savedCartItem);
    }

    @Override
    @Transactional
    public CartItemDto updateCartItemQuantity(Long bookId, Integer quantity) {
        if (quantity <= 0) {
            // 如果数量小于等于0，则视为删除该项
            removeItemFromCart(bookId);
            return null; // 或者返回一个表示已删除的特定DTO/消息
        }
        User currentUser = getCurrentUser();
        Book book = bookDao.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("更新购物车失败：未找到ID为 " + bookId + " 的书籍。"));

        if (book.getStockQuantity() < quantity) {
            throw new RuntimeException("库存不足，无法将《" + book.getTitle() + "》的数量更新为 " + quantity + "。");
        }

        CartItem cartItem = cartItemDao.findByUserIdAndBookId(currentUser.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("更新购物车失败：该商品不在您的购物车中。"));

        cartItem.setQuantity(quantity);
        CartItem updatedCartItem = cartItemDao.save(cartItem);
        return CartItemDto.fromEntity(updatedCartItem);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long bookId) {
        User currentUser = getCurrentUser();
        // 先校验书籍是否存在，以及是否在用户购物车中，防止无效删除
        if (!bookDao.existsById(bookId)) {
            throw new ResourceNotFoundException("从购物车移除失败：未找到ID为 " + bookId + " 的书籍。");
        }
        cartItemDao.findByUserIdAndBookId(currentUser.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("从购物车移除失败：该商品不在您的购物车中。"));

        cartItemDao.deleteByUserIdAndBookId(currentUser.getId(), bookId);
    }

    @Override
    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();
        cartItemDao.deleteByUserId(currentUser.getId());
    }
}