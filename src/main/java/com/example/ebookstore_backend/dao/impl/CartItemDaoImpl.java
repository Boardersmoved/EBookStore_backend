package com.example.ebookstore_backend.dao.impl;

import com.example.ebookstore_backend.dao.CartItemDao;
import com.example.ebookstore_backend.entity.CartItem;
import com.example.ebookstore_backend.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CartItemDaoImpl implements CartItemDao {

    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartItemDaoImpl(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }


    @Override
    public List<CartItem> findByUserId(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    public Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId) {
        return cartItemRepository.findByUserIdAndBookId(userId, bookId);
    }

    @Override
    public void deleteByUserIdAndBookId(Long userId, Long bookId) {
        cartItemRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    public void deleteByUserIdAndBookIdIn(Long id, List<Long> orderedBookIds) {
        cartItemRepository.deleteByUserIdAndBookIdIn(id, orderedBookIds);
    }
}