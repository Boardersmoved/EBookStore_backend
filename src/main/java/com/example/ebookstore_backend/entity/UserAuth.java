package com.example.ebookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_auths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAuth {

    @Id
    @Column(name = "user_id")
    private Long userId; // 直接使用user_id作为主键

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user; // 关联到User实体，但不控制外键

    @Column(nullable = false, length = 255)
    private String credential; // 加密后的密码

    
    // 构造函数
    public UserAuth(Long userId, String credential) {
        this.userId = userId;
        this.credential = credential;
    }
}