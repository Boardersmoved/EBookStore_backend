package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.entity.neo4j.TagNode;
import com.example.ebookstore_backend.repository.TagNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
public class TagGraphService {

    @Autowired
    private TagNodeRepository tagNodeRepository;

    // 获取扩展后的标签列表
    public List<String> getExpandedSearchTags(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return Collections.emptyList();
        }
        // 从 Neo4j 查找关联标签
        List<String> relatedTags = tagNodeRepository.findRelatedTags(tagName);
        
        if (relatedTags.isEmpty()) {
            return Collections.singletonList(tagName);
        }
        return relatedTags;
    }
}
