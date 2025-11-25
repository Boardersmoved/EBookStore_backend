package com.example.ebookstore_backend.dao;

import com.example.ebookstore_backend.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface BookDao {
    Optional<Book> findById(Long id);

    // 从JpaSpecificationExecutor继承的方法
    Page<Book> findAll(Specification<Book> spec, Pageable pageable);

    boolean existsById(Long bookId);

    Book save(Book book);

    void deleteById(Long id);

    void populateBookDetails(List<Book> books);
}