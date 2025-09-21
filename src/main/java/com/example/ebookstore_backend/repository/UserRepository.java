package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);

    // 检查用户名是否存在
    Boolean existsByUsername(String username);

    // 检查邮箱是否存在
    Boolean existsByEmail(String email);
    
}