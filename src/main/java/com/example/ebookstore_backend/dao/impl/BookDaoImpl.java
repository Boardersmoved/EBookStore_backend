package com.example.ebookstore_backend.dao.impl;

import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class BookDaoImpl implements BookDao {

    private final BookRepository bookRepository;

    @Autowired
    public BookDaoImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }


    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }


    @Override
    public Page<Book> findAll(Specification<Book> spec, Pageable pageable) {
        return bookRepository.findAll(spec, pageable);
    }

    @Override
    public boolean existsById(Long bookId) {
        return bookRepository.existsById(bookId);
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

}