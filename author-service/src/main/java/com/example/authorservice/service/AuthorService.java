package com.example.authorservice.service;


public interface AuthorService {
    
    /**
     * 根据书名获取作者
     * @param title 书名
     * @return 作者名称，未找到返回 null
     */
    String getAuthorByBookTitle(String title);
}
