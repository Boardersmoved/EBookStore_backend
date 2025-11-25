package com.example.ebookstore_backend.dao.impl;

import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.dao.CartItemDao;
import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.CartItem;
import com.example.ebookstore_backend.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CartItemDaoImpl implements CartItemDao {

    private final CartItemRepository cartItemRepository;
    private final BookDao bookDao;

    @Autowired
    public CartItemDaoImpl(CartItemRepository cartItemRepository, BookDao bookDao) {
        this.cartItemRepository = cartItemRepository;
        this.bookDao = bookDao;
    }

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemRepository.save(cartItem);
    }


    @Override
    public List<CartItem> findByUserId(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);

        // 提取书籍并填充
        List<Book> books = items.stream().map(CartItem::getBook).collect(Collectors.toList());
        bookDao.populateBookDetails(books);

        return items;
    }

    @Override
    public Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId) {
        Optional<CartItem> itemOpt = cartItemRepository.findByUserIdAndBookId(userId, bookId);

        // 如果存在，填充书籍详情
        itemOpt.ifPresent(item -> bookDao.populateBookDetails(Collections.singletonList(item.getBook())));

        return itemOpt;
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