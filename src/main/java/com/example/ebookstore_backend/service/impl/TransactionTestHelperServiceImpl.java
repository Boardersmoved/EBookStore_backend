package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.dao.OrderDao;
import com.example.ebookstore_backend.dao.OrderItemDao;
import com.example.ebookstore_backend.dao.UserDao;
import com.example.ebookstore_backend.dto.CreateOrderRequestDto;
import com.example.ebookstore_backend.dto.OrderItemRequestDto;
import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.Order;
import com.example.ebookstore_backend.entity.OrderItem;
import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.exception.ResourceNotFoundException;
import com.example.ebookstore_backend.service.AuthService;
import com.example.ebookstore_backend.service.TransactionTestHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionTestHelperServiceImpl implements TransactionTestHelperService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionTestHelperServiceImpl.class);
    
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private OrderItemDao orderItemDao;
    
    @Autowired
    private BookDao bookDao;
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private AuthService authService;
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order createOrderRequiresNew(CreateOrderRequestDto request) {
        User currentUser = getCurrentUserEntity();
        logger.info("创建订单 - REQUIRES_NEW传播（独立服务），用户: {}", currentUser.getUsername());
        
        Order order = new Order();
        order.setUser(currentUser);
        order.setShippingAddress(request.getShippingAddress());
        order.setContactPhone(request.getContactPhone());
        order.setStatus("PENDING_PAYMENT");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        
        Order savedOrder = orderDao.save(order);
        logger.info("订单创建成功 - REQUIRES_NEW（独立服务），ID: {}", savedOrder.getId());
        return savedOrder;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addOrderItemsRequiresNew(Order order, List<OrderItemRequestDto> items) {
        logger.info("添加订单项 - REQUIRES_NEW传播（独立服务），订单ID: {}", order.getId());
        
        // 直接使用传入的order对象，不重新查询
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : items) {
            Book book = bookDao.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("书籍未找到: " + itemRequest.getBookId()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);  // 直接使用传入的order
            orderItem.setBook(book);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(book.getPrice());
            
            orderItems.add(orderItem);
        }
        
        orderItemDao.saveAll(orderItems);
        logger.info("订单项添加成功 - REQUIRES_NEW（独立服务），数量: {}，事务即将提交", orderItems.size());
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addOrderItemsRequiresNewWithException(Order order, List<OrderItemRequestDto> items) {
        logger.info("添加订单项 - REQUIRES_NEW传播（独立服务，带异常），订单ID: {}", order.getId());
        
        // 故意触发异常
        int result = 10 / 0;
        
        // 这部分代码不会执行
        Order managedOrder = orderDao.findById(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("订单未找到: " + order.getId()));
        
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : items) {
            Book book = bookDao.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("书籍未找到: " + itemRequest.getBookId()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(managedOrder);
            orderItem.setBook(book);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(book.getPrice());
            
            orderItems.add(orderItem);
        }
        
        orderItemDao.saveAll(orderItems);
    }
    
    private User getCurrentUserEntity() {
        User user = authService.getCurrentAuthenticatedUser();
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }
        return userDao.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("当前登录用户在数据库中未找到"));
    }
}