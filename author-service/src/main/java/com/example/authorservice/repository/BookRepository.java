package com.example.authorservice.repository;

import com.example.authorservice.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Book Repository
 * 提供图书数据访问功能
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     * 根据书名查询作者（精确匹配）
     * @param title 书名
     * @return 作者名称
     */
    @Query("SELECT b.author FROM Book b WHERE b.title = :title AND b.isAvailable = true")
    Optional<String> findAuthorByTitle(@Param("title") String title);
}

