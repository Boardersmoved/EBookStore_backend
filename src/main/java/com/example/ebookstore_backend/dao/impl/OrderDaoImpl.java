package com.example.ebookstore_backend.dao.impl;

import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.dao.OrderDao;
import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.Order;
import com.example.ebookstore_backend.entity.OrderItem;
import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.repository.OrderItemRepository;
import com.example.ebookstore_backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class OrderDaoImpl implements OrderDao {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookDao bookDao;

    @Autowired
    public OrderDaoImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, BookDao bookDao) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.bookDao = bookDao;
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        orderOpt.ifPresent(order -> populateOrders(Collections.singletonList(order)));
        return orderOpt;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders = orderRepository.findAll();
        populateOrders(orders);
        return orders;
    }


    @Override
    public void delete(Order order) {
        orderRepository.delete(order);
    }

    @Override
    public List<Order> findByUserWithItemsAndBooks(User user) {
        List<Order> orders = orderRepository.findByUserWithItemsAndBooks(user);
        populateOrders(orders);
        return orders;
    }


    @Override
    public Optional<OrderItem> findOrderItemById(Long id) {
        Optional<OrderItem> itemOpt = orderItemRepository.findById(id);
        itemOpt.ifPresent(item -> bookDao.populateBookDetails(Collections.singletonList(item.getBook())));
        return itemOpt;
    }

    // 辅助方法：提取订单列表中的所有书籍并填充
    private void populateOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) return;

        List<Book> books = orders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .map(OrderItem::getBook)
                .collect(Collectors.toList());

        bookDao.populateBookDetails(books);
    }

    @Override
    public void deleteOrderItem(OrderItem orderItem) {
        orderItemRepository.delete(orderItem);
    }
}