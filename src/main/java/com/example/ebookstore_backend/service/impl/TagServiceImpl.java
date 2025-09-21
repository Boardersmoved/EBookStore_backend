package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.Tag;
import com.example.ebookstore_backend.dao.TagDao;
import com.example.ebookstore_backend.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
public class TagServiceImpl implements TagService {

    private final TagDao tagDao;

    public TagServiceImpl(TagDao tagDao) {
        this.tagDao = tagDao;
    }


    @Override
    @Transactional(readOnly = true) // 只读事务，提高性能
    public List<String> getAllTagNames() {
        return tagDao.findAll().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public Set<Tag> findOrCreateTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> existingTags = tagDao.findByNameIn(tagNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> newTagsToCreate = new HashSet<>();
        for (String tagName : tagNames) {
            if (!existingTagNames.contains(tagName)) {
                newTagsToCreate.add(new Tag(tagName.trim())); // 创建新标签时去除首尾空格
            }
        }

        if (!newTagsToCreate.isEmpty()) {
            existingTags.addAll(tagDao.saveAll(newTagsToCreate));
        }
        return existingTags;
    }
}