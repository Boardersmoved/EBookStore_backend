package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Set<Tag> findByNameIn(Set<String> names); // 根据一组名称查找标签
    // Spring Data JPA 会自动根据方法名生成查询
    // JpaRepository 已经提供了 findAll() 方法
}