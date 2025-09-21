package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.Order;
import com.example.ebookstore_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.book WHERE o.user = :user ORDER BY o.orderDate DESC")
    List<Order> findByUserWithItemsAndBooks(@Param("user") User user);

}