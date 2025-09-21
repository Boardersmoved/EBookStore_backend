package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.dto.BookDto; // 引入BookDto
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    /**
     * 获取书籍分页列表，支持按标签和关键词筛选
     * @param pageable 分页参数
     * @param tagName 标签名称 (可选)
     * @param keyword 搜索关键词 (可选, 搜索标题、作者等)
     * @return 分页的 BookDto 数据
     */
    Page<BookDto> getAllBooks(Pageable pageable, String tagName, String keyword);

    Page<BookDto> getBooks(Pageable pageable, String tagName, String keyword);

    /**
     * 根据ID获取书籍详情
     * @param id 书籍ID
     * @return BookDto 对象，如果不存在则抛出异常
     */
    BookDto getBookById(Long id);

    /**
     * 创建新书籍
     * @param bookDto 书籍数据
     * @return 创建后的书籍DTO
     */
    BookDto createBook(BookDto bookDto);

    /**
     * 更新书籍信息
     * @param bookDto 更新的书籍数据
     * @return 更新后的书籍DTO
     */
    BookDto updateBook(BookDto bookDto);

    /**
     * 删除指定ID的书籍（软删除：标记为下架）
     * @param id 要下架的书籍ID
     */
    void deleteBook(Long id);

    /**
     * 恢复指定ID的书籍（重新上架）
     * @param id 要恢复的书籍ID
     */
    void restoreBook(Long id);
}