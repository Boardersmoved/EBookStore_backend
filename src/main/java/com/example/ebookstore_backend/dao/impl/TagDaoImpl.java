package com.example.ebookstore_backend.dao.impl;

import com.example.ebookstore_backend.dao.TagDao;
import com.example.ebookstore_backend.entity.Tag;
import com.example.ebookstore_backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class TagDaoImpl implements TagDao {

    private final TagRepository tagRepository;

    @Autowired
    public TagDaoImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public Tag save(Tag tag) {
        return tagRepository.save(tag);
    }

    @Override
    public List<Tag> saveAll(Set<Tag> tags) {
        return tagRepository.saveAll(tags);
    }

    @Override
    public Set<Tag> findByNameIn(Set<String> names) {
        return tagRepository.findByNameIn(names);
    }

    @Override
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }
}