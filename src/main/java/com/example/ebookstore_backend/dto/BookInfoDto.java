package com.example.ebookstore_backend.dto;

import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.Tag;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图书基础信息DTO - 不包含库存和销量，适合长期缓存
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookInfoDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String title;
    private String author;
    private String coverImageBase64;
    private String isbn;
    private BigDecimal price;
    private String description;
    private Set<String> tags;
    private Boolean isAvailable;

    public static BookInfoDto fromEntity(Book book) {
        if (book == null) {
            return null;
        }
        return new BookInfoDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverImageBase64(),
                book.getIsbn(),
                book.getPrice(),
                book.getDescription(),
                book.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()),
                book.getIsAvailable()
        );
    }
    
    /**
     * 组装成完整的 BookDto
     */
    public BookDto toBookDto(Integer stockQuantity, Integer sales) {
        return new BookDto(
                this.id,
                this.title,
                this.author,
                this.coverImageBase64,
                this.isbn,
                this.price,
                this.description,
                sales != null ? sales : 0,
                stockQuantity != null ? stockQuantity : 0,
                this.tags,
                this.isAvailable
        );
    }
}
