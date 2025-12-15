package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.BookPageResponse;
import com.example.ebookstore_backend.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL 查询解析器
 * 复用现有的 BookService 实现按书籍名称查找功能
 */
@Controller
public class BookGraphQLController {
    private static final Logger logger = LoggerFactory.getLogger(BookGraphQLController.class);
    private final BookService bookService;
    @Autowired
    public BookGraphQLController(BookService bookService) {
        this.bookService = bookService;
    }
    @QueryMapping
    public BookPageResponse searchBooksByName(
            @Argument String name,
            @Argument Integer page,
            @Argument Integer size) {
        int pageNum = (page != null) ? page : 0;
        int pageSize = (size != null) ? size : 10;
        logger.info("========== GraphQL查询开始 ==========");
        logger.info("GraphQL查询类型: searchBooksByName");
        logger.info("查询参数 - name: {}, page: {}, size: {}", name, pageNum, pageSize);
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        // 复用现有的 BookService.getAllBooks 方法
        BookPageResponse response = BookPageResponse.fromPage(bookService.getAllBooks(pageable, null, name));
        logger.info("GraphQL查询结果 - 返回书籍数量: {}, 总数: {}", 
                    response.getContent().size(), 
                    response.getPageInfo().getTotalElements());
        logger.info("========== GraphQL查询结束 ==========");
        return response;
    }
}
