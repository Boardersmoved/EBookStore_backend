package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.entity.Tag;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface TagService {
    /**
     * 获取所有标签的名称列表
     * @return 标签名称字符串列表
     */
    List<String> getAllTagNames();

    @Transactional
        // 读写事务
    Set<Tag> findOrCreateTags(Set<String> tagNames);
}