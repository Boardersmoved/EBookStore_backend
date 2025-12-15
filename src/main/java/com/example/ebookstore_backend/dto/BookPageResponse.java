package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * GraphQL 分页书籍响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookPageResponse {
    private List<BookDto> content;
    private PageInfoResponse pageInfo;

    /**
     * 从 Spring Data Page<BookDto> 创建 BookPageResponse
     */
    public static BookPageResponse fromPage(Page<BookDto> page) {
        return new BookPageResponse(
                page.getContent(),
                PageInfoResponse.fromPage(page)
        );
    }
}

