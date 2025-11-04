package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.client.AuthorServiceClient;
import com.example.ebookstore_backend.dto.AuthorResponseDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/books/author")
@RequiredArgsConstructor
public class BookAuthorController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookAuthorController.class);
    private final AuthorServiceClient authorServiceClient;
    
    /**
     * 根据书名查询作者
     * 
     * @param title 书名
     * @return 作者信息
     */
    @GetMapping
    public ResponseEntity<AuthorResponseDto> getAuthorByTitle(
            @RequestParam("title") String title) {
        
        try {
            AuthorResponseDto response = authorServiceClient.getAuthorByBookTitle(title);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("【微服务调用失败】", e);
            return ResponseEntity.ok(new AuthorResponseDto(
                title, 
                null, 
                false, 
                "服务调用失败: " + e.getMessage()
            ));
        }
    }
}

