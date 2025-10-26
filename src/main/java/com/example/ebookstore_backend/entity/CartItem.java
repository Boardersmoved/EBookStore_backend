package com.example.ebookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;  

@Entity
@Table(name = "cart_items",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "book_id"}) }) // 对应数据库的uk_user_book
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {  

    private static final long serialVersionUID = 1L;  

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 一个用户可以有多个购物车项
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 关联到用户

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // 一本书可以被添加到多个用户的购物车中（通过不同的CartItem记录）
    @JoinColumn(name = "book_id", nullable = false)
    private Book book; // 关联到书籍

    @Column(nullable = false)
    private Integer quantity; // 商品数量


    public CartItem(User user, Book book, Integer quantity) {
        this.user = user;
        this.book = book;
        this.quantity = quantity;
    }
}