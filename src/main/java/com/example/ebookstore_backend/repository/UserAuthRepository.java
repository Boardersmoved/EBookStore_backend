package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    
    // 根据用户名查找认证信息（通过关联查询User表）
    @Query("SELECT ua FROM UserAuth ua JOIN ua.user u WHERE u.username = :username")
    Optional<UserAuth> findByUsername(@Param("username") String username);
    
    // 检查用户是否存在认证信息
    boolean existsByUserId(Long userId);
}