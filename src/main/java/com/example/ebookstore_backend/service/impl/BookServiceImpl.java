package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.Tag;
import com.example.ebookstore_backend.dto.BookDto;
import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.service.BookService;
import com.example.ebookstore_backend.service.TagService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookServiceImpl implements BookService {

    private final BookDao bookDao;
    private final TagService tagService;

    @Autowired
    public BookServiceImpl(BookDao bookDao, TagService tagService) { // 修改构造函数参数
        this.bookDao = bookDao;
        this.tagService = tagService;
    }


    // 将Book实体转换为BookDto
    private BookDto convertToDto(Book book) {
        return BookDto.fromEntity(book); 
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> getAllBooks(Pageable pageable, String tagName, String keyword) {
        Specification<Book> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 只显示上架的书籍
            predicates.add(criteriaBuilder.isTrue(root.get("isAvailable")));

            if (StringUtils.hasText(keyword)) {
                String keywordPattern = "%" + keyword.toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keywordPattern);
                Predicate authorPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), keywordPattern);
                predicates.add(criteriaBuilder.or(titlePredicate, authorPredicate));
            }

            if (StringUtils.hasText(tagName)) {
                Join<Book, Tag> tagJoin = root.join("tags");
                predicates.add(criteriaBuilder.equal(tagJoin.get("name"), tagName));
                query.distinct(true); 
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Book> bookPage = bookDao.findAll(spec, pageable);
        // 将 Page<Book> 转换为 Page<BookDto>
        return bookPage.map(this::convertToDto); 
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> getBooks(Pageable pageable, String tagName, String keyword) {
        Specification<Book> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();


            if (StringUtils.hasText(keyword)) {
                String keywordPattern = "%" + keyword.toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keywordPattern);
                Predicate authorPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), keywordPattern);
                predicates.add(criteriaBuilder.or(titlePredicate, authorPredicate));
            }

            if (StringUtils.hasText(tagName)) {
                Join<Book, Tag> tagJoin = root.join("tags");
                predicates.add(criteriaBuilder.equal(tagJoin.get("name"), tagName));
                query.distinct(true); 
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Book> bookPage = bookDao.findAll(spec, pageable);
        // 将 Page<Book> 转换为 Page<BookDto>
        return bookPage.map(this::convertToDto); 
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto getBookById(Long id) {
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return convertToDto(book);
    }

    @Override
    @Transactional
    public BookDto createBook(BookDto bookDto) {
        Book book = new Book();
        updateBookFromDto(book, bookDto);
        Book savedBook = bookDao.save(book);
        return convertToDto(savedBook);
    }

    @Override
    @Transactional
    public BookDto updateBook(BookDto bookDto) {
        Book book = bookDao.findById(bookDto.getId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookDto.getId()));
        updateBookFromDto(book, bookDto);
        Book updatedBook = bookDao.save(book);
        return convertToDto(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        // 软删除：标记为下架
        book.setIsAvailable(false);
        bookDao.save(book);
    }

    @Override
    @Transactional
    public void restoreBook(Long id) {
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        // 恢复上架
        book.setIsAvailable(true);
        bookDao.save(book);
    }

    // 辅助方法：从DTO更新Book实体
    private void updateBookFromDto(Book book, BookDto bookDto) {
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(bookDto.getIsbn());
        book.setDescription(bookDto.getDescription());
        book.setPrice(bookDto.getPrice());
        book.setStockQuantity(bookDto.getStockQuantity());
        book.setCoverImageBase64(bookDto.getCoverImageBase64());

        // 处理上架状态
        if (bookDto.getIsAvailable() != null) {
            book.setIsAvailable(bookDto.getIsAvailable());
        } else {
            // 新创建的书籍默认上架
            book.setIsAvailable(true);
        }
        
        // 处理标签
        if (bookDto.getTags() != null && !bookDto.getTags().isEmpty()) {
            // 使用TagService将标签名称转换为标签实体
            Set<Tag> tags = tagService.findOrCreateTags(bookDto.getTags());
            book.setTags(tags);
        } else {
            // 如果没有标签，设置为空集合
            book.setTags(new HashSet<>());
        }
    }
}