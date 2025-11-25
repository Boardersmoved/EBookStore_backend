package com.example.ebookstore_backend.entity.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "book_details")
public class BookDetails {
    @Id
    private Long id; // 与 MySQL 中的 Book ID 保持一致
    private String description;
    private String coverImageBase64;
}
