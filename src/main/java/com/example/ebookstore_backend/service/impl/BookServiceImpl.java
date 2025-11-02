package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.Tag;
import com.example.ebookstore_backend.dto.BookDto;
import com.example.ebookstore_backend.dto.PageResult;
import com.example.ebookstore_backend.dao.BookDao;
import com.example.ebookstore_backend.service.BookService;
import com.example.ebookstore_backend.service.TagService;
import com.example.ebookstore_backend.repository.BookInventoryRepository;
import com.example.ebookstore_backend.dto.BookInfoDto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy; 
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookDao bookDao;
    private final TagService tagService;
    private final BookServiceImpl self;
    private final BookInventoryRepository bookInventoryRepository;

    @Autowired
    public BookServiceImpl(BookDao bookDao, 
                          TagService tagService, 
                          @Lazy BookServiceImpl self,
                          BookInventoryRepository bookInventoryRepository) {
        this.bookDao = bookDao;
        this.tagService = tagService;
        this.self = self;
        this.bookInventoryRepository = bookInventoryRepository;
    }

    private BookDto convertToDto(Book book) {
        return BookDto.fromEntity(book); 
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> getAllBooks(Pageable pageable, String tagName, String keyword) {
        logger.info("【查询图书列表】Page: {}, Size: {}, Sort: {}, Tag: {}, Keyword: {}", 
                pageable.getPageNumber(), 
                pageable.getPageSize(),
                pageable.getSort(),
                tagName, 
                keyword);
        
        // 无过滤条件的查询使用缓存
        if (tagName == null && keyword == null) {
            PageResult<BookDto> pageResult = self.queryBooksWithCache(pageable);
            return new PageImpl<>(
                pageResult.getContent(), 
                pageable, 
                pageResult.getTotalElements()
            );
        }
        
        // 有过滤条件的查询直接查数据库
        return queryBooksFromDatabase(pageable, tagName, keyword);
    }

    /**
     * 带缓存的查询方法
     */
    @Cacheable(
        value = "bookList",
        key = "'page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize + '-sort-' + #pageable.sort.toString()",
        unless = "#result == null"
    )
    public PageResult<BookDto> queryBooksWithCache(Pageable pageable) {
        logger.info("缓存未命中：从数据库查询 - 参数: page={}, size={}, sort={}", 
                pageable.getPageNumber(), 
                pageable.getPageSize(),
                pageable.getSort());
        
        Page<BookDto> page = queryBooksFromDatabase(pageable, null, null);
        PageResult<BookDto> result = PageResult.from(page);
        
        logger.info("写入缓存 key: page-{}-size-{}-sort-{}", 
                pageable.getPageNumber(), 
                pageable.getPageSize(),
                pageable.getSort());
        
        return result;
    }

    private Page<BookDto> queryBooksFromDatabase(Pageable pageable, String tagName, String keyword) {
        Specification<Book> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
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
        Page<BookDto> result = bookPage.map(this::convertToDto);
        
        logger.info("数据库查询成功", 
                result.getTotalElements(), 
                result.getNumberOfElements());
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> getBooks(Pageable pageable, String tagName, String keyword) {
        logger.info("管理员查询图书列表 Page: {}, Size: {}, Sort: {}, Tag: {}, Keyword: {}", 
                pageable.getPageNumber(), 
                pageable.getPageSize(),
                pageable.getSort(),
                tagName, 
                keyword);
        
        // 管理员直接查数据库
        return queryBooksFromDatabase(pageable, tagName, keyword);
    }

    /**
     * 缓存图书基础信息（不含库存）
     */
    @Cacheable(value = "book_info", key = "#id", unless = "#result == null")
    public BookInfoDto getBookInfoFromCache(Long id) {
        logger.info("【基础信息缓存未命中】从数据库查询 Book ID: {}", id);
        
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        return BookInfoDto.fromEntity(book);
    }
    
    /**
     * 实时查询库存（不缓存）
     */
    public Integer getRealtimeStock(Long bookId) {
        return bookInventoryRepository.findQuantityByBookId(bookId).orElse(0);
    }
    
    /**
     * 获取图书详情 - 组合缓存的基础信息 + 实时库存
     */
    @Override
    @Transactional(readOnly = true)
    public BookDto getBookById(Long id) {
        
        // 1. 从缓存获取基础信息
        BookInfoDto info = self.getBookInfoFromCache(id);
        
        // 2. 实时查询库存
        Integer stock = getRealtimeStock(id);
        
        // 3. 查询销量
        Book book = bookDao.findById(id).orElseThrow();
        Integer sales = book.getSales();
        
        // 4. 组装成 BookDto
        BookDto result = info.toBookDto(stock, sales);
        return result;
    }

    @Override
    @Transactional
    @Caching(
        put = @CachePut(value = "book", key = "#result.id"),
        evict = @CacheEvict(value = "bookList", allEntries = true)
    )
    public BookDto createBook(BookDto bookDto) {
        logger.info("【创建图书】标题: {}", bookDto.getTitle());
        
        Book book = new Book();
        updateBookFromDto(book, bookDto);
        Book savedBook = bookDao.save(book);
        
        logger.info("【创建成功并清除列表缓存】图书ID: {}", savedBook.getId());
        return convertToDto(savedBook);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "book", key = "#bookDto.id"),
        @CacheEvict(value = "bookList", allEntries = true)
    })
    public BookDto updateBook(BookDto bookDto) {
        logger.info("【更新图书】ID: {}", bookDto.getId());
        
        Book book = bookDao.findById(bookDto.getId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookDto.getId()));
        updateBookFromDto(book, bookDto);
        Book updatedBook = bookDao.save(book);
        
        logger.info("【更新成功并清除所有缓存】图书ID: {}, 新标题: {}", bookDto.getId(), bookDto.getTitle());
        return convertToDto(updatedBook);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "book", key = "#id"),
        @CacheEvict(value = "bookList", allEntries = true)
    })
    public void deleteBook(Long id) {
        logger.info("【软删除图书】ID: {}", id);
        
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        book.setIsAvailable(false);
        bookDao.save(book);
        
        logger.info("【下架成功并清除所有缓存】图书ID: {}", id);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "book", key = "#id"),
        @CacheEvict(value = "bookList", allEntries = true)
    })
    public void restoreBook(Long id) {
        logger.info("【恢复图书上架】ID: {}", id);
        
        Book book = bookDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        
        book.setIsAvailable(true);
        bookDao.save(book);
        
        logger.info("【恢复成功并清除所有缓存】图书ID: {}", id);
    }

    private void updateBookFromDto(Book book, BookDto bookDto) {
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setIsbn(bookDto.getIsbn());
        book.setDescription(bookDto.getDescription());
        book.setPrice(bookDto.getPrice());
        book.setStockQuantity(bookDto.getStockQuantity());
        book.setCoverImageBase64(bookDto.getCoverImageBase64());

        if (bookDto.getIsAvailable() != null) {
            book.setIsAvailable(bookDto.getIsAvailable());
        } else {
            book.setIsAvailable(true);
        }
        
        if (bookDto.getTags() != null && !bookDto.getTags().isEmpty()) {
            Set<Tag> tags = tagService.findOrCreateTags(bookDto.getTags());
            book.setTags(tags);
        } else {
            book.setTags(new HashSet<>());
        }
    }
}