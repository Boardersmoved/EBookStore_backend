package com.example.ebookstore_backend.client;

import com.example.ebookstore_backend.dto.AuthorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class AuthorServiceFallback implements AuthorServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorServiceFallback.class);
    
    @Override
    public AuthorResponseDto getAuthorByBookTitle(String title) {
        logger.warn("【服务降级】Author Service 不可用，书名: {}", title);
        return new AuthorResponseDto(
            title, 
            null, 
            false, 
            "作者服务暂时不可用，请稍后重试"
        );
    }
}

