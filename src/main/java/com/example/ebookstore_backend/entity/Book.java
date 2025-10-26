package com.example.ebookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;  
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book implements Serializable { 
    
    private static final long serialVersionUID = 1L; 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String author;

    @Column(name = "isbn", length = 20, unique = true)
    private String isbn;
    
    @Lob
    @Column(name = "cover_image_base64")
    private String coverImageBase64;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    // 一对一关系：Book 和 BookSales
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private BookSales bookSales;

    // 一对一关系：Book 和 BookInventory
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private BookInventory bookInventory;

    // 多对多关系: 一本书可以有多个标签
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_tags",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // 获取销量
    public Integer getSales() {
        return bookSales != null ? bookSales.getTotalSales() : 0;
    }

    // 获取库存数量
    public Integer getStockQuantity() {
        return bookInventory != null ? bookInventory.getQuantity() : 0;
    }

    // 设置销量
    public void setSales(Integer sales) {
        if (bookSales == null) {
            bookSales = new BookSales(this);
        }
        bookSales.setTotalSales(sales != null ? sales : 0);
    }

    // 设置库存数量
    public void setStockQuantity(Integer quantity) {
        if (bookInventory == null) {
            bookInventory = new BookInventory(this);
        }
        bookInventory.setQuantity(quantity != null ? quantity : 0);
    }
}