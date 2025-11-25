package com.example.ebookstore_backend.dao.impl;

import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.mongodb.BookDetails;
import com.example.ebookstore_backend.repository.BookDetailsRepository;
import com.example.ebookstore_backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BookDaoImpl implements BookDao {

    private final BookRepository bookRepository;
    private final BookDetailsRepository bookDetailsRepository;

    @Autowired
    public BookDaoImpl(BookRepository bookRepository, BookDetailsRepository bookDetailsRepository) {
        this.bookRepository = bookRepository;
        this.bookDetailsRepository = bookDetailsRepository;
    }


    @Override
    public Optional<Book> findById(Long id) {
        // 1. 从 MySQL 查基础信息
        Optional<Book> bookOpt = bookRepository.findById(id);

        // 2. 如果存在，去 MongoDB 查详情并填充
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            Optional<BookDetails> detailsOpt = bookDetailsRepository.findById(id);
            if (detailsOpt.isPresent()) {
                BookDetails details = detailsOpt.get();
                book.setDescription(details.getDescription());
                book.setCoverImageBase64(details.getCoverImageBase64());
            }
        }
        return bookOpt;
    }


    @Override
    public Page<Book> findAll(Specification<Book> spec, Pageable pageable) {
        // 1. 从 MySQL 分页查询
        Page<Book> page = bookRepository.findAll(spec, pageable);

        // 2. 提取 ID 列表
        List<Long> ids = page.getContent().stream().map(Book::getId).collect(Collectors.toList());

        // 3. 批量从 MongoDB 查询详情 (避免 N+1 问题)
        List<BookDetails> detailsList = (List<BookDetails>) bookDetailsRepository.findAllById(ids);
        Map<Long, BookDetails> detailsMap = detailsList.stream()
                .collect(Collectors.toMap(BookDetails::getId, d -> d));

        // 4. 将详情填充回 Book 对象
        page.getContent().forEach(book -> {
            BookDetails details = detailsMap.get(book.getId());
            if (details != null) {
                book.setDescription(details.getDescription());
                book.setCoverImageBase64(details.getCoverImageBase64());
            }
        });

        return page;
    }

    @Override
    public boolean existsById(Long bookId) {
        return bookRepository.existsById(bookId);
    }

    @Override
    @Transactional 
    public Book save(Book book) {
        // 1. 先保存到 MySQL (获取/更新 ID)
        Book savedBook = bookRepository.save(book);

        // 2. 再保存到 MongoDB
        BookDetails details = new BookDetails();
        details.setId(savedBook.getId()); // 确保 ID 一致
        details.setDescription(book.getDescription()); // 从传入的 book 对象取值
        details.setCoverImageBase64(book.getCoverImageBase64());

        bookDetailsRepository.save(details);

        // 3. 确保返回的对象包含所有数据
        savedBook.setDescription(book.getDescription());
        savedBook.setCoverImageBase64(book.getCoverImageBase64());

        return savedBook;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // 双删
        bookRepository.deleteById(id);
        bookDetailsRepository.deleteById(id);
    }

    @Override
    public void populateBookDetails(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return;
        }

        // 1. 提取所有书籍 ID
        List<Long> ids = books.stream()
                .map(Book::getId)
                .distinct()
                .collect(Collectors.toList());

        // 2. 批量从 MongoDB 查询详情
        List<BookDetails> detailsList = (List<BookDetails>) bookDetailsRepository.findAllById(ids);
        Map<Long, BookDetails> detailsMap = detailsList.stream()
                .collect(Collectors.toMap(BookDetails::getId, d -> d));

        // 3. 填充回 Book 对象
        for (Book book : books) {
            BookDetails details = detailsMap.get(book.getId());
            if (details != null) {
                book.setDescription(details.getDescription());
                book.setCoverImageBase64(details.getCoverImageBase64());
            }
        }
    }
}