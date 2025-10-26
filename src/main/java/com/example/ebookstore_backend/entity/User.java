package com.example.ebookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;  
import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {  

    private static final long serialVersionUID = 1L; 

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Lob
    @Column(name = "avatar_base64", columnDefinition = "LONGTEXT")
    private String avatarBase64;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Lob
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "address_text", length = 500)
    private String addressText;

    @Column(nullable = false)
    private Boolean valid = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserAuth userAuth;

    // 构造函数
    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.role = "USER";
        this.valid = true; 
    }
}