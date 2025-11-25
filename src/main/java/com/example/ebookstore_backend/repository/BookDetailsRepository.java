package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.mongodb.BookDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailsRepository extends MongoRepository<BookDetails, Long> {
}
