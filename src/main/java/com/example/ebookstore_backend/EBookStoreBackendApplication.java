package com.example.ebookstore_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class EBookStoreBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EBookStoreBackendApplication.class, args);
    }
}
