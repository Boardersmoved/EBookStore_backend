package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.CreateOrderRequestDto;
import com.example.ebookstore_backend.dto.OrderItemRequestDto;
import com.example.ebookstore_backend.dto.OrderResponseDto;
import com.example.ebookstore_backend.service.OrderService;
import com.example.ebookstore_backend.service.TransactionTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/transaction-test")
public class TransactionTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionTestController.class);
    
    @Autowired
    private TransactionTestService transactionTestService;
    
    /**
     * 测试不同事务传播属性的效果
     * @param testCase 测试案例编号 (1-9)
     * @return 测试结果
     */
    @PostMapping("/propagation/{testCase}")
    public ResponseEntity<Map<String, Object>> testTransactionPropagation(@PathVariable int testCase) {
        Map<String, Object> result = new HashMap<>();
        String propagationType = "";
        
        // 创建测试用的订单请求
        CreateOrderRequestDto orderRequest = createTestOrderRequest();
        
        try {
            switch (testCase) {
                case 1:
                    propagationType = "REQUIRED-正常情况";
                    transactionTestService.testCase1_Normal(orderRequest);
                    break;
                case 2:
                    propagationType = "REQUIRED-创建订单异常";
                    transactionTestService.testCase2_CreateOrderException(orderRequest);
                    break;
                case 3:
                    propagationType = "REQUIRED-添加订单项异常";
                    transactionTestService.testCase3_AddOrderItemException(orderRequest);
                    break;
                case 4:
                    propagationType = "REQUIRED-添加订单项后异常";
                    transactionTestService.testCase4_ExceptionAfterAddOrderItem(orderRequest);
                    break;
                case 5:
                    propagationType = "REQUIRES_NEW-正常情况";
                    transactionTestService.testCase5_RequiresNewNormal(orderRequest);
                    break;
                case 6:
                    propagationType = "REQUIRES_NEW-添加订单项前异常";
                    transactionTestService.testCase6_RequiresNewExceptionBefore(orderRequest);
                    break;
                case 7:
                    propagationType = "REQUIRES_NEW-添加订单项后异常";
                    transactionTestService.testCase7_RequiresNewExceptionAfter(orderRequest);
                    break;
                case 8:
                    propagationType = "REQUIRES_NEW-添加订单项异常";
                    transactionTestService.testCase8_RequiresNewOrderItemException(orderRequest);
                    break;
                case 9:
                    propagationType = "REQUIRES_NEW-创建订单后添加订单项异常";
                    transactionTestService.testCase9_RequiresNewBothOperationsWithException(orderRequest);
                    break;
                default:
                    result.put("error", "无效的测试案例编号");
                    return ResponseEntity.badRequest().body(result);
            }
            
            result.put("success", true);
            result.put("message", propagationType + " 测试完成");
            logger.info("{} 测试成功", propagationType);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", propagationType + " 测试异常: " + e.getMessage());
            result.put("exception", e.getClass().getSimpleName());
            logger.error("{} 测试失败: {}", propagationType, e.getMessage());
        }
        
        result.put("testCase", testCase);
        result.put("propagationType", propagationType);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 创建测试用的订单请求数据
     */
    private CreateOrderRequestDto createTestOrderRequest() {
        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setShippingAddress("测试地址123号");
        request.setContactPhone("13800138000");
        
        OrderItemRequestDto item = new OrderItemRequestDto();
        item.setBookId(1L); // 假设存在ID为1的书籍
        item.setQuantity(1);
        
        request.setItems(Arrays.asList(item));
        return request;
    }
}