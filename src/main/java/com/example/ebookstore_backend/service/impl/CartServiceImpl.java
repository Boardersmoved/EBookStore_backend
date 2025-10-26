package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.CartItem;
import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.dto.AddCartItemRequestDto;
import com.example.ebookstore_backend.dto.CartItemDto;
import com.example.ebookstore_backend.exception.ResourceNotFoundException;
import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.dao.CartItemDao;
import com.example.ebookstore_backend.service.AuthService;
import com.example.ebookstore_backend.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartItemDao cartItemDao;
    private final BookDao bookDao;
    private final AuthService authService;
    private final CartServiceImpl self;

    @Autowired
    public CartServiceImpl(CartItemDao cartItemDao, BookDao bookDao, AuthService authService,
                          @Lazy CartServiceImpl self) {
        this.cartItemDao = cartItemDao;
        this.bookDao = bookDao;
        this.authService = authService;
        this.self = self;
    }

    private User getCurrentUser() {
        User user = authService.getCurrentAuthenticatedUser();
        if (user == null) {
            throw new IllegalStateException("用户未登录，无法执行购物车操作。");
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems() {
        User currentUser = getCurrentUser();
        logger.info("【查询购物车】User ID: {}", currentUser.getId());
        return self.getCartItemsWithCache(currentUser.getId());
    }

    @Cacheable(value = "cart", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<CartItemDto> getCartItemsWithCache(Long userId) {
        logger.info("缓存未命中：从数据库查询购物车 - User ID: {}", userId);

        List<CartItemDto> cartItems = cartItemDao.findByUserId(userId)
                .stream()
                .map(CartItemDto::fromEntity)
                .collect(Collectors.toList());

        logger.info("写入购物车缓存 - User ID: {}, 商品数: {}", userId, cartItems.size());
        return cartItems;
    }

    @Override
    @Transactional
    public CartItemDto addItemToCart(AddCartItemRequestDto addRequest) {
        User currentUser = getCurrentUser();
        logger.info("【添加商品到购物车】User ID: {}, Book ID: {}, Quantity: {}",
                currentUser.getId(), addRequest.getBookId(), addRequest.getQuantity());

        Book book = bookDao.findById(addRequest.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("无法将书籍加入购物车：未找到ID为 " + addRequest.getBookId() + " 的书籍。"));

        if (book.getStockQuantity() < addRequest.getQuantity()) {
            throw new RuntimeException("库存不足，无法添加 " + addRequest.getQuantity() + " 件《" + book.getTitle() + "》到购物车。");
        }

        Optional<CartItem> existingCartItemOpt = cartItemDao.findByUserIdAndBookId(currentUser.getId(), book.getId());

        CartItem cartItemToSave;
        if (existingCartItemOpt.isPresent()) {
            cartItemToSave = existingCartItemOpt.get();
            int newQuantity = cartItemToSave.getQuantity() + addRequest.getQuantity();
            if (book.getStockQuantity() < newQuantity) {
                throw new RuntimeException("库存不足，无法将总计 " + newQuantity + " 件《" + book.getTitle() + "》加入购物车。");
            }
            cartItemToSave.setQuantity(newQuantity);
        } else {
            cartItemToSave = new CartItem(currentUser, book, addRequest.getQuantity());
        }

        CartItem savedCartItem = cartItemDao.save(cartItemToSave);
        logger.info("添加成功并清除购物车缓存 - User ID: {}", currentUser.getId());
        self.evictCartCache(currentUser.getId());

        return CartItemDto.fromEntity(savedCartItem);
    }

    @Override
    @Transactional
    public CartItemDto updateCartItemQuantity(Long bookId, Integer quantity) {
        User currentUser = getCurrentUser();
        logger.info("【更新购物车商品数量】User ID: {}, Book ID: {}, New Quantity: {}",
                currentUser.getId(), bookId, quantity);

        if (quantity <= 0) {
            removeItemFromCart(bookId);
            return null;
        }

        Book book = bookDao.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("更新购物车失败：未找到ID为 " + bookId + " 的书籍。"));

        if (book.getStockQuantity() < quantity) {
            throw new RuntimeException("库存不足，无法将《" + book.getTitle() + "》的数量更新为 " + quantity + "。");
        }

        CartItem cartItem = cartItemDao.findByUserIdAndBookId(currentUser.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("更新购物车失败：该商品不在您的购物车中。"));

        cartItem.setQuantity(quantity);
        CartItem updatedCartItem = cartItemDao.save(cartItem);

        logger.info("更新成功并清除购物车缓存 - User ID: {}", currentUser.getId());
        self.evictCartCache(currentUser.getId());

        return CartItemDto.fromEntity(updatedCartItem);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long bookId) {
        User currentUser = getCurrentUser();
        logger.info("【从购物车删除商品】User ID: {}, Book ID: {}", currentUser.getId(), bookId);

        if (!bookDao.existsById(bookId)) {
            throw new ResourceNotFoundException("从购物车移除失败：未找到ID为 " + bookId + " 的书籍。");
        }
        cartItemDao.findByUserIdAndBookId(currentUser.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("从购物车移除失败：该商品不在您的购物车中。"));

        cartItemDao.deleteByUserIdAndBookId(currentUser.getId(), bookId);

        logger.info("删除成功并清除购物车缓存 - User ID: {}", currentUser.getId());
        self.evictCartCache(currentUser.getId());
    }

    @Override
    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();
        logger.info("【清空购物车】User ID: {}", currentUser.getId());

        cartItemDao.deleteByUserId(currentUser.getId());

        logger.info("清空成功并清除购物车缓存 - User ID: {}", currentUser.getId());
        self.evictCartCache(currentUser.getId());
    }

    @CacheEvict(value = "cart", key = "#userId")
    public void evictCartCache(Long userId) {
    }
}