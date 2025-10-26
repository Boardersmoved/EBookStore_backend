package com.example.ebookstore_backend.dto;

import com.example.ebookstore_backend.entity.Tag;
import lombok.Data; // @Data combines @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;  
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.ebookstore_backend.entity.Book;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto implements Serializable {  
    
    private static final long serialVersionUID = 1L;  
    
    private Long id;
    private String title;
    private String author;
    private String coverImageBase64;
    private String isbn;
    private BigDecimal price;
    private String description;
    private Integer sales;
    private Integer stockQuantity;
    private Set<String> tags; // 将Set<Tag>转换为Set<String>
    private Boolean isAvailable;


    public static BookDto fromEntity(Book book) {
        if (book == null) {
            return null;
        }
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getCoverImageBase64(),
                book.getIsbn(),
                book.getPrice(),
                book.getDescription(),
                book.getSales(),
                book.getStockQuantity(),
                book.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()),
                book.getIsAvailable()
        );
    }
}