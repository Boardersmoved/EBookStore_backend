package com.example.authorservice.controller;

import com.example.authorservice.dto.AuthorResponse;
import com.example.authorservice.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 作者查询控制器
 * 提供根据书名查询作者的RESTful API
 */
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthorController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);
    private final AuthorService authorService;
    
    /**
     * 根据书名获取作者（精确匹配）
     * 
     * @param title 书名
     * @return 作者信息
     * 
     * 示例：GET /api/authors/by-book?title=三体
     */
    @GetMapping("/by-book")
    public ResponseEntity<AuthorResponse> getAuthorByBookTitle(
            @RequestParam("title") String title) {
        
        logger.info("【API请求】精确查询作者 - 书名: {}", title);
        
        String author = authorService.getAuthorByBookTitle(title);
        
        if (author != null) {
            logger.info("【API响应】查询成功 - 书名: {}, 作者: {}", title, author);
            return ResponseEntity.ok(AuthorResponse.success(title, author));
        } else {
            logger.warn("【API响应】未找到 - 书名: {}", title);
            return ResponseEntity.ok(AuthorResponse.notFound(title));
        }
    }
    
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Author Service is running!");
    }
}

