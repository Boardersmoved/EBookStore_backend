package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * GraphQL 分页信息响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfoResponse {
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    /**
     * 从 Spring Data Page 对象创建 PageInfoResponse
     */
    public static PageInfoResponse fromPage(Page<?> page) {
        return new PageInfoResponse(
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}

