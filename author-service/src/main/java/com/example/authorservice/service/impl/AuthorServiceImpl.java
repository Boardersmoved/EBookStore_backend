package com.example.authorservice.service.impl;

import com.example.authorservice.dao.BookDao;
import com.example.authorservice.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorServiceImpl.class);
    private final BookDao bookDao;

    @Override
    @Transactional(readOnly = true)
    public String getAuthorByBookTitle(String title) {
        Optional<String> author = bookDao.findAuthorByTitle(title);
        return author.orElse(null);
    }
}

