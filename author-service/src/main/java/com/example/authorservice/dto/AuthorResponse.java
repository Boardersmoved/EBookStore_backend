package com.example.authorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作者查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {
    
    /**
     * 查询的书名或关键词
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
    
    /**
     * 便捷构造方法
     */
    public AuthorResponse(String bookTitle, String author, boolean success) {
        this.bookTitle = bookTitle;
        this.author = author;
        this.success = success;
        this.message = success ? "查询成功" : "查询失败";
    }
    
    /**
     * 创建成功响应
     */
    public static AuthorResponse success(String bookTitle, String author) {
        return new AuthorResponse(bookTitle, author, true, "查询成功");
    }
    
    /**
     * 创建失败响应
     */
    public static AuthorResponse notFound(String bookTitle) {
        return new AuthorResponse(bookTitle, null, false, "未找到该书籍或书籍已下架");
    }
}

