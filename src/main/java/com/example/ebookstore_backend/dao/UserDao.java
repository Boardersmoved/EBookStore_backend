package com.example.ebookstore_backend.dao;

import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.entity.UserAuth;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    // 基本CRUD操作
    User save(User user);
    UserAuth save(UserAuth userAuth);
    Optional<User> findById(Long id);

    // 从UserRepository移植的方法
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    List<User> findAll();
}