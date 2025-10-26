package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.BookDto;
import com.example.ebookstore_backend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // 用于排序
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<Page<BookDto>> getAllBooks(
            @RequestParam(name = "page", defaultValue = "0") int page, // 页码，从0开始
            @RequestParam(name = "size", defaultValue = "10") int size, // 每页数量
            @RequestParam(name = "tag", required = false) String tagName,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy, // 排序字段，默认按id
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir // 排序方向，asc或desc
    ) {
        String actualSortBy = sortBy;
        if ("sales".equals(sortBy)) {
            actualSortBy = "bookSales.totalSales";
        }
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, actualSortBy));
        Page<BookDto> bookPage = bookService.getAllBooks(pageable, tagName, keyword);
        return ResponseEntity.ok(bookPage);
    }

    @GetMapping("/manager")
    public ResponseEntity<Page<BookDto>> getBooks(
            @RequestParam(name = "page", defaultValue = "0") int page, // 页码，从0开始
            @RequestParam(name = "size", defaultValue = "10") int size, // 每页数量
            @RequestParam(name = "tag", required = false) String tagName,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy, // 排序字段，默认按id
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir // 排序方向，asc或desc
    ) {
        String actualSortBy = sortBy;
        if ("sales".equals(sortBy)) {
            actualSortBy = "bookSales.totalSales";
        }
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, actualSortBy));
        Page<BookDto> bookPage = bookService.getBooks(pageable, tagName, keyword);
        return ResponseEntity.ok(bookPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        BookDto bookDto = bookService.getBookById(id);
        return ResponseEntity.ok(bookDto);
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody BookDto bookDto) {
        BookDto createdBook = bookService.createBook(bookDto);
        return ResponseEntity.ok(createdBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@PathVariable Long id, @RequestBody BookDto bookDto) {
        bookDto.setId(id); // 确保ID匹配
        BookDto updatedBook = bookService.updateBook(bookDto);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}")
    public ResponseEntity<BookDto> restoreBook(@PathVariable Long id) {
        bookService.restoreBook(id);
        return ResponseEntity.ok().build();
    }
}