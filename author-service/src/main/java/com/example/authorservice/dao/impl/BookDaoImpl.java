package com.example.authorservice.dao.impl;

import com.example.authorservice.dao.BookDao;
import com.example.authorservice.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookDaoImpl implements BookDao {
    
    private static final Logger logger = LoggerFactory.getLogger(BookDaoImpl.class);
    private final BookRepository bookRepository;
    
    /**
     * 根据书名查询作者
     * @param title 书名
     * @return 作者名称
     */
    @Override
    public Optional<String> findAuthorByTitle(String title) {
        return bookRepository.findAuthorByTitle(title);
    }
}

