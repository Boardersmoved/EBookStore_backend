package com.example.ebookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "book_inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookInventory {

    @Id
    private Long bookId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer quantity = 0;

    public BookInventory(Book book) {
        this.book = book;
        this.quantity = 0;
    }
}