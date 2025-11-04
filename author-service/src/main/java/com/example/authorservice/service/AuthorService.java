package com.example.authorservice.service;

import com.example.authorservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 作者查询服务
 */
@Service
@RequiredArgsConstructor
public class AuthorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorService.class);
    private final BookRepository bookRepository;
    
    /**
     * 根据书名获取作者（精确匹配）
     * @param title 书名
     * @return 作者名称，未找到返回 null
     */
    @Transactional(readOnly = true)
    public String getAuthorByBookTitle(String title) {
        
        Optional<String> author = bookRepository.findAuthorByTitle(title);
        
        if (author.isPresent()) {
            logger.info("【查询成功】书名: {}, 作者: {}", title, author.get());
        } else {
            logger.warn("【未找到】书名: {}", title);
        }
        
        return author.orElse(null);
    }
}

