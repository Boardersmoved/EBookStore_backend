package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags") // 所有此控制器下的API都以 /api/tags 开头
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<String>> getAllTagNames() {
        List<String> tagNames = tagService.getAllTagNames();
        return ResponseEntity.ok(tagNames);
    }
}