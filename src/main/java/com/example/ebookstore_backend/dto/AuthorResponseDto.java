package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponseDto {
    
    /**
     * 查询的书名
     */
    private String bookTitle;
    
    /**
     * 作者名称
     */
    private String author;
    
    /**
     * 是否查询成功
     */
    private boolean success;
    
    /**
     * 响应消息
     */
    private String message;
}

