package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.BookInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface BookInventoryRepository extends JpaRepository<BookInventory, Long> {
    
    /**
     * 根据图书ID查询库存
     */
    @Query("SELECT bi.quantity FROM BookInventory bi WHERE bi.bookId = :bookId")
    Optional<Integer> findQuantityByBookId(@Param("bookId") Long bookId);
    
    /**
     * 批量查询多本图书的库存
     */
    @Query("SELECT bi.bookId as bookId, bi.quantity as quantity FROM BookInventory bi WHERE bi.bookId IN :bookIds")
    List<Map<String, Object>> findQuantitiesByBookIds(@Param("bookIds") List<Long> bookIds);
}
