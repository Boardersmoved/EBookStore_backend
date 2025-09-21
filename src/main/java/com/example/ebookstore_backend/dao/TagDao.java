package com.example.ebookstore_backend.dao;

import com.example.ebookstore_backend.entity.Tag;

import java.util.List;
import java.util.Set;

public interface TagDao {
    // 保存标签
    Tag save(Tag tag);

    // 批量保存标签
    List<Tag> saveAll(Set<Tag> tags);

    // 根据一组名称查找标签
    Set<Tag> findByNameIn(Set<String> names);

    // 查找所有标签
    List<Tag> findAll();
}