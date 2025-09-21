package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserIdAndBookId(Long userId, Long bookId);

    void deleteByUserIdAndBookId(Long userId, Long bookId);

    void deleteByUserId(Long userId); // 清空用户购物车

    /**
     * 根据用户ID和一组书籍ID列表删除购物车中的项目
     * @param userId 用户ID
     * @param bookIds 要删除的书籍ID列表
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId AND ci.book.id IN :bookIds")
    void deleteByUserIdAndBookIdIn(@Param("userId") Long userId, @Param("bookIds") List<Long> bookIds);
}
