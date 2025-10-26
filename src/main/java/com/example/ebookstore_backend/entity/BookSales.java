package com.example.ebookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;  

@Entity
@Table(name = "book_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookSales implements Serializable {  

    private static final long serialVersionUID = 1L;  

    @Id
    private Long bookId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "total_sales", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer totalSales = 0;

    public BookSales(Book book) {
        this.book = book;
        this.totalSales = 0;
    }

}