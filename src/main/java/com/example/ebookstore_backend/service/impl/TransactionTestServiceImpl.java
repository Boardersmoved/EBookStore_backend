package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.dao.OrderDao;
import com.example.ebookstore_backend.dao.OrderItemDao;
import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.dao.UserDao;
import com.example.ebookstore_backend.dto.CreateOrderRequestDto;
import com.example.ebookstore_backend.dto.OrderItemRequestDto;
import com.example.ebookstore_backend.dto.OrderResponseDto;
import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.Order;
import com.example.ebookstore_backend.entity.OrderItem;
import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.exception.ResourceNotFoundException;
import com.example.ebookstore_backend.service.AuthService;
import com.example.ebookstore_backend.service.TransactionTestHelperService;
import com.example.ebookstore_backend.service.TransactionTestService;
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
public class TransactionTestServiceImpl implements TransactionTestService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionTestServiceImpl.class);
    
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
    
    @Autowired
    private TransactionTestHelperService helperService;
    
    // 测试案例1：正常情况 - 所有操作都正常
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase1_Normal(CreateOrderRequestDto request) {
        logger.info("开始测试案例1：正常情况 - 所有操作都正常");
        
        Order order = createOrderRequired(request);
        addOrderItemsRequired(order, request.getItems());
        
        OrderResponseDto result = OrderResponseDto.fromEntity(order);
        logger.info("测试案例1完成，订单ID: {}", result.getOrderId());
        return result;
    }
    
    // 测试案例2：transfer中异常 - 在调用withdraw和deposit之前异常
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase2_CreateOrderException(CreateOrderRequestDto request) {
        logger.info("开始测试案例2：transfer中异常 - 在调用任何操作之前异常");
        
        // 在调用任何操作之前就异常
        int result = 10 / 0;
        
        Order order = createOrderRequired(request);
        addOrderItemsRequired(order, request.getItems());
        
        return OrderResponseDto.fromEntity(order);
    }
    
    // 测试案例3：withdraw中异常 - 创建订单时异常
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase3_AddOrderItemException(CreateOrderRequestDto request) {
        logger.info("开始测试案例3：withdraw中异常 - 创建订单时异常");
        
        Order order = createOrderRequiredWithException(request);
        addOrderItemsRequired(order, request.getItems());
        
        return OrderResponseDto.fromEntity(order);
    }
    
    // 测试案例4：deposit中异常 - 添加订单项时异常
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase4_ExceptionAfterAddOrderItem(CreateOrderRequestDto request) {
        logger.info("开始测试案例4：deposit中异常 - 添加订单项时异常");
        
        Order order = createOrderRequired(request);
        addOrderItemsRequiredWithException(order, request.getItems());
        
        return OrderResponseDto.fromEntity(order);
    }
    
    // 测试案例5：正常情况，deposit使用REQUIRES_NEW
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase5_RequiresNewNormal(CreateOrderRequestDto request) {
        logger.info("开始测试案例5：正常情况，deposit使用REQUIRES_NEW");
        
        Order order = createOrderRequired(request);
        helperService.addOrderItemsRequiresNew(order, request.getItems());
        
        OrderResponseDto result = OrderResponseDto.fromEntity(order);
        logger.info("测试案例5完成，订单ID: {}", result.getOrderId());
        return result;
    }
    
    // 测试案例6：transfer中异常，在deposit之前，deposit使用REQUIRES_NEW
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase6_RequiresNewExceptionBefore(CreateOrderRequestDto request) {
        logger.info("开始测试案例6：transfer中异常，在deposit之前");
        
        // 在调用deposit之前异常
        int result = 10 / 0;
        
        Order order = createOrderRequired(request);
        helperService.addOrderItemsRequiresNew(order, request.getItems());
        
        return OrderResponseDto.fromEntity(order);
    }
    
    // 测试案例7：transfer中异常，在deposit之后，deposit使用REQUIRES_NEW
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase7_RequiresNewExceptionAfter(CreateOrderRequestDto request) {
        logger.info("开始测试案例7：transfer中异常，在deposit之后");
        
        Order order = createOrderRequired(request);
        helperService.addOrderItemsRequiresNew(order, request.getItems());
        
        // 在deposit之后异常
        int result = 10 / 0;
        
        return OrderResponseDto.fromEntity(order);
    }
    
    // 测试案例8：deposit中异常，deposit使用REQUIRES_NEW
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase8_RequiresNewOrderItemException(CreateOrderRequestDto request) {
        logger.info("开始测试案例8：deposit中异常，deposit使用REQUIRES_NEW");
        
        Order order = createOrderRequired(request);
        helperService.addOrderItemsRequiresNewWithException(order, request.getItems());
        
        return OrderResponseDto.fromEntity(order);
    }
    
    // 测试案例9：正常情况，withdraw和deposit都使用REQUIRES_NEW，但transfer中最后异常
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public OrderResponseDto testCase9_RequiresNewBothOperationsWithException(CreateOrderRequestDto request) {
        logger.info("开始测试案例9：withdraw和deposit都使用REQUIRES_NEW，transfer最后异常");
        
        Order order = helperService.createOrderRequiresNew(request);
        helperService.addOrderItemsRequiresNew(order, request.getItems());
        
        // transfer中最后异常
        int result = 10 / 0;
        
        return OrderResponseDto.fromEntity(order);
    }
    
    // ===================== 内部方法 - REQUIRED传播 =====================
    
    @Transactional(propagation = Propagation.REQUIRED)
    private Order createOrderRequired(CreateOrderRequestDto request) {
        User currentUser = getCurrentUserEntity();
        logger.info("创建订单 - REQUIRED传播，用户: {}", currentUser.getUsername());
        
        Order order = new Order();
        order.setUser(currentUser);
        order.setShippingAddress(request.getShippingAddress());
        order.setContactPhone(request.getContactPhone());
        order.setStatus("PENDING_PAYMENT");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        
        Order savedOrder = orderDao.save(order);
        logger.info("订单创建成功 - REQUIRED，ID: {}", savedOrder.getId());
        return savedOrder;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    private Order createOrderRequiredWithException(CreateOrderRequestDto request) {
        User currentUser = getCurrentUserEntity();
        logger.info("创建订单 - REQUIRED传播（带异常），用户: {}", currentUser.getUsername());
        
        // 在创建订单时异常
        int result = 10 / 0;
        
        Order order = new Order();
        order.setUser(currentUser);
        order.setShippingAddress(request.getShippingAddress());
        order.setContactPhone(request.getContactPhone());
        order.setStatus("PENDING_PAYMENT");
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        
        Order savedOrder = orderDao.save(order);
        return savedOrder;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    private void addOrderItemsRequired(Order order, List<OrderItemRequestDto> items) {
        logger.info("添加订单项 - REQUIRED传播，订单ID: {}", order.getId());
        
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : items) {
            Book book = bookDao.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("书籍未找到: " + itemRequest.getBookId()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(book.getPrice());
            
            orderItems.add(orderItem);
        }
        
        orderItemDao.saveAll(orderItems);
        logger.info("订单项添加成功 - REQUIRED，数量: {}", orderItems.size());
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    private void addOrderItemsRequiredWithException(Order order, List<OrderItemRequestDto> items) {
        logger.info("添加订单项 - REQUIRED传播（带异常），订单ID: {}", order.getId());
        
        // 在添加订单项时异常
        int result = 10 / 0;
        
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : items) {
            Book book = bookDao.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("书籍未找到: " + itemRequest.getBookId()));
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(book.getPrice());
            
            orderItems.add(orderItem);
        }
        
        orderItemDao.saveAll(orderItems);
    }
    
    // 获取当前用户
    private User getCurrentUserEntity() {
        User user = authService.getCurrentAuthenticatedUser();
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }
        return userDao.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("当前登录用户在数据库中未找到"));
    }
}