package com.example.ebookstore_backend.client;

import com.example.ebookstore_backend.dto.AuthorResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "author-service", fallback = AuthorServiceFallback.class)
public interface AuthorServiceClient {
    
    @GetMapping("/api/authors/by-book")
    AuthorResponseDto getAuthorByBookTitle(@RequestParam("title") String title);
}

