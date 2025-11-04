package com.example.authorservice.dao;

import java.util.Optional;

public interface BookDao {
    
    /**
     * 根据书名查询作者
     * @param title 书名
     * @return 作者名称
     */
    Optional<String> findAuthorByTitle(String title);
}

