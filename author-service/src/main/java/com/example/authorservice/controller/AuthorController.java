package com.example.authorservice.controller;

import com.example.authorservice.dto.AuthorResponse;
import com.example.authorservice.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthorController {
    
    private final AuthorService authorService;
    
    @GetMapping("/by-book")
    public ResponseEntity<AuthorResponse> getAuthorByBookTitle(
            @RequestParam("title") String title) {
        
        String author = authorService.getAuthorByBookTitle(title);
        
        if (author != null) {
            return ResponseEntity.ok(AuthorResponse.success(title, author));
        } else {
            return ResponseEntity.ok(AuthorResponse.notFound(title));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Author Service is running!");
    }
}

