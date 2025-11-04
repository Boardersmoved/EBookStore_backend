package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.*;
import com.example.ebookstore_backend.dto.CreateOrderRequestDto;
import com.example.ebookstore_backend.dto.FlattenedOrderItemDto;
import com.example.ebookstore_backend.dto.OrderItemRequestDto;
import com.example.ebookstore_backend.dto.OrderResponseDto;
import com.example.ebookstore_backend.dto.PriceCalculationRequest;
import com.example.ebookstore_backend.dto.PriceCalculationResponse;
import com.example.ebookstore_backend.exception.InsufficientStockException;
import com.example.ebookstore_backend.exception.ResourceNotFoundException;
import com.example.ebookstore_backend.exception.UnauthorizedOperationException;
import com.example.ebookstore_backend.dao.UserDao;
import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.dao.CartItemDao;
import com.example.ebookstore_backend.dao.OrderDao;
import com.example.ebookstore_backend.client.PriceCalculatorClient;
import com.example.ebookstore_backend.service.AuthService;
import com.example.ebookstore_backend.service.CartService;
import com.example.ebookstore_backend.service.OrderService;
import org.aspectj.weaver.ast.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderDao orderDao;
    private final AuthService authService;
    private final CartItemDao cartItemDao;
    private final BookDao bookDao;
    private final UserDao userDao;
    private final CartService cartService;
    private final PriceCalculatorClient priceCalculatorClient;

    public OrderServiceImpl(OrderDao orderDao, AuthService authService, CartItemDao cartItemDao, BookDao bookDao, UserDao userDao, CartService cartService, PriceCalculatorClient priceCalculatorClient) {
        this.orderDao = orderDao;
        this.authService = authService;
        this.cartItemDao = cartItemDao;
        this.bookDao = bookDao;
        this.userDao = userDao;
        this.cartService = cartService;
        this.priceCalculatorClient = priceCalculatorClient;
    }


    private User getCurrentUserEntity() {
        User user = authService.getCurrentAuthenticatedUser();
        if (user == null) {
            throw new IllegalStateException("用户未登录，无法执行订单操作。");
        }
        return userDao.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("当前登录用户在数据库中未找到。"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrderHistoryForCurrentUser() {
        // 调用新的搜索方法，不设置任何过滤条件
        return getOrderHistoryForCurrentUser(null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrderHistoryForCurrentUser(LocalDateTime startTime, LocalDateTime endTime, String bookName) {
        User currentUser = getCurrentUserEntity();
        List<Order> orders = orderDao.findByUserWithItemsAndBooks(currentUser);
        logger.info("Fetching order history for user: {} with filters: startTime={}, endTime={}, bookName={}", 
                currentUser.getUsername(), startTime, endTime, bookName);
        return orders.stream()
                .filter(order -> {
                    // 检查时间范围 
                    boolean timeMatch = true;
                    if (startTime != null) {
                        LocalDateTime localStartTime = startTime.plusHours(8);
                        timeMatch = timeMatch && !order.getOrderDate().isBefore(localStartTime);
                    }
                    if (endTime != null) {
                        LocalDateTime localEndTime = endTime.plusHours(8);
                        timeMatch = timeMatch && order.getOrderDate().isBefore(localEndTime);
                    }
                    
                    // 检查订单中是否包含匹配书籍名称的商品
                    boolean bookMatch = true;
                    if (bookName != null && !bookName.trim().isEmpty()) {
                        bookMatch = order.getOrderItems().stream()
                                .anyMatch(item -> item.getBook() != null && 
                                        item.getBook().getTitle().toLowerCase()
                                        .contains(bookName.toLowerCase().trim()));
                    }
                    
                    return timeMatch && bookMatch;
                })
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        // 调用带参数的方法，不设置任何过滤条件
        return getAllOrders(null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders(LocalDateTime startTime, LocalDateTime endTime, String bookName) {
        logger.info("Fetching all orders with filters: startTime={}, endTime={}, bookName={}", startTime, endTime, bookName);
        
        List<Order> allOrders = orderDao.findAll();
        
        return allOrders.stream()
                .filter(order -> {
                    // 检查时间范围
                    boolean timeMatch = true;
                    if (startTime != null) {
                        LocalDateTime localStartTime = startTime.plusHours(8);
                        timeMatch = timeMatch && !order.getOrderDate().isBefore(localStartTime);
                    }
                    if (endTime != null) {
                        LocalDateTime localEndTime = endTime.plusHours(8);
                        timeMatch = timeMatch && order.getOrderDate().isBefore(localEndTime);
                    }
                    
                    // 检查订单中是否包含匹配书籍名称的商品
                    boolean bookMatch = true;
                    if (bookName != null && !bookName.trim().isEmpty()) {
                        bookMatch = order.getOrderItems().stream()
                                .anyMatch(item -> item.getBook() != null && 
                                        item.getBook().getTitle().toLowerCase()
                                        .contains(bookName.toLowerCase().trim()));
                    }
                    
                    return timeMatch && bookMatch;
                })
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto requestDto) {
        User currentUser = getCurrentUserEntity();
        if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
            throw new IllegalArgumentException("订单中必须至少选择一件商品。");
        }
        
        Order newOrder = new Order();
        newOrder.setUser(currentUser);
        newOrder.setShippingAddress(requestDto.getShippingAddress());
        newOrder.setContactPhone(requestDto.getContactPhone());
        newOrder.setStatus("PENDING_PAYMENT");
        newOrder.setOrderDate(LocalDateTime.now());
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        List<Long> orderedBookIds = new ArrayList<>();

        for (OrderItemRequestDto itemRequest : requestDto.getItems()) {
            Book book = bookDao.findById(itemRequest.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("创建订单失败：选择的书籍ID " + itemRequest.getBookId() + " 未找到。"));
            
            // 检查书籍是否已下架
            if (!book.getIsAvailable()) {
                throw new RuntimeException("无法下单已下架的书籍《" + book.getTitle() + "》。");
            }
            
            // 详细的库存检查逻辑
            if (book.getStockQuantity() <= 0) {
                throw new InsufficientStockException("书籍《" + book.getTitle() + "》已售罄，无法完成下单。");
            }
            
            if (book.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                    String.format("书籍《%s》库存不足，您选择了 %d 件，但库存仅剩 %d 件，无法完成下单。", 
                        book.getTitle(), 
                        itemRequest.getQuantity(), 
                        book.getStockQuantity())
                );
            }
            
            // 验证数量的合理性
            if (itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("商品数量必须大于0。");
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setBook(book);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(book.getPrice());
            newOrder.addOrderItem(orderItem);
            
            // 调用函数式服务计算小计
            PriceCalculationRequest priceRequest = new PriceCalculationRequest(
                book.getPrice(), 
                itemRequest.getQuantity()
            );
            PriceCalculationResponse priceResponse = priceCalculatorClient.calculatePrice(priceRequest);
            BigDecimal subtotal = priceResponse.getTotalPrice();
            
            totalOrderAmount = totalOrderAmount.add(subtotal);
            orderedBookIds.add(book.getId());
            
            logger.info("Added book '{}' to order: quantity={}, price={}, subtotal={} (calculated by price-calculator-service)", 
                       book.getTitle(), itemRequest.getQuantity(), book.getPrice(), subtotal);
        }
        
        newOrder.setTotalAmount(totalOrderAmount);
        Order savedOrder = orderDao.save(newOrder);
        
        // 从购物车中移除已下单的商品
        if (!orderedBookIds.isEmpty()) {
            cartItemDao.deleteByUserIdAndBookIdIn(currentUser.getId(), orderedBookIds);
            cartService.evictCartCache(currentUser.getId());
            logger.info("购物车商品已删除并清除缓存 - User ID: {}, 删除 {} 件商品", 
                    currentUser.getId(), orderedBookIds.size());
        }
        
        logger.info("Order created successfully with ID: {} for user: {}, total amount: {}", 
                   savedOrder.getId(), currentUser.getUsername(), totalOrderAmount);
        
        return OrderResponseDto.fromEntity(savedOrder);
    }

    @Override
    @Transactional
    public List<OrderResponseDto> payOrders(List<Long> orderIds) {
        User currentUser = getCurrentUserEntity();
        logger.info("User '{}' attempting to pay for order IDs: {}", currentUser.getUsername(), orderIds);
        List<OrderResponseDto> paidOrderDtos = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Long orderId : orderIds) {
            Order order = orderDao.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("订单 ID: " + orderId + " 未找到。"));
            if (!Objects.equals(order.getUser().getId(), currentUser.getId())) {
                errors.add("订单 ID: " + orderId + " 不属于您或支付失败。");
                continue;
            }
            List<String> payableStatuses = Arrays.asList("PENDING_PAYMENT", "UNPAID");
            if (!payableStatuses.contains(order.getStatus().toUpperCase())) {
                errors.add("订单 ID: " + orderId + " 状态为 '" + order.getStatus() + "'，无法支付。");
                continue;
            }
            if (currentUser.getBalance().compareTo(order.getTotalAmount()) < 0) {
                errors.add("订单 ID: " + orderId + " 支付失败：账户余额不足。");
                continue;
            }
            
            // 支付前再次检查库存并减少库存、增加销量
            boolean stockCheckPassed = true;
            for (OrderItem orderItem : order.getOrderItems()) {
                Book book = orderItem.getBook();
                if (book.getStockQuantity() < orderItem.getQuantity()) {
                    errors.add("订单 ID: " + orderId + " 支付失败：书籍《" + book.getTitle() + "》库存不足。");
                    stockCheckPassed = false;
                    break;
                }
            }
            
            if (!stockCheckPassed) {
                continue;
            }
            
            // 扣除用户余额
            currentUser.setBalance(currentUser.getBalance().subtract(order.getTotalAmount()));
            
            // 减少库存并增加销量
            for (OrderItem orderItem : order.getOrderItems()) {
                Book book = orderItem.getBook();
                book.setStockQuantity(book.getStockQuantity() - orderItem.getQuantity());
                book.setSales(book.getSales() + orderItem.getQuantity());
                logger.info("Book '{}' stock reduced by {}, sales increased by {}. New stock: {}, new sales: {}", 
                           book.getTitle(), orderItem.getQuantity(), orderItem.getQuantity(), 
                           book.getStockQuantity(), book.getSales());
            }
            
            order.setStatus("PAID");
            paidOrderDtos.add(OrderResponseDto.fromEntity(order));
        }
        
        userDao.save(currentUser); // 确保余额更新被保存
        if (!errors.isEmpty()) {
            logger.warn("Some orders could not be paid: {}", String.join("; ", errors));
        }
        return paidOrderDtos;
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        User currentUser = getCurrentUserEntity();
        logger.info("User '{}' attempting to delete order with ID: {}", currentUser.getUsername(), orderId);

        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("订单 ID: " + orderId + " 未找到。"));

        // 验证订单归属
        if (!Objects.equals(order.getUser().getId(), currentUser.getId())) {
            logger.warn("User '{}' attempted to delete order ID {} which does not belong to them.", 
                    currentUser.getUsername(), orderId);
            throw new UnauthorizedOperationException("您无权删除此订单。");
        }

        // 检查订单状态是否允许删除
        List<String> deletableStatuses = Arrays.asList("PENDING_PAYMENT", "UNPAID");
        if (!deletableStatuses.contains(order.getStatus().toUpperCase())) {
            logger.warn("User '{}' attempted to delete order ID {} with status '{}'. Operation denied.",
                    currentUser.getUsername(), orderId, order.getStatus());
            throw new UnauthorizedOperationException("订单状态为 '" + order.getStatus() + "'，不允许删除订单。");
        }

        // 获取订单中的所有订单项，并从购物车中删除对应的商品
        for (OrderItem orderItem : order.getOrderItems()) {
            Book book = orderItem.getBook();
            // 如果订单状态是已支付，则需要恢复库存和销量
            if (book != null && "PAID".equalsIgnoreCase(order.getStatus())) {
                book.setStockQuantity(book.getStockQuantity() + orderItem.getQuantity());
                book.setSales(Math.max(0, book.getSales() - orderItem.getQuantity())); // 确保销量不会变为负数
                logger.info("Book '{}' stock restored by {}, sales reduced by {} due to order deletion. New stock: {}, new sales: {}", 
                        book.getTitle(), orderItem.getQuantity(), orderItem.getQuantity(), 
                        book.getStockQuantity(), book.getSales());
            }
        }

        // 删除整个订单
        orderDao.delete(order);
        logger.info("Order ID {} has been deleted successfully.", orderId);
    }
}
