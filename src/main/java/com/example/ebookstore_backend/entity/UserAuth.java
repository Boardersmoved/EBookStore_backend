package com.example.ebookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;  

@Entity
@Table(name = "user_auths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAuth implements Serializable {  

    private static final long serialVersionUID = 1L;  

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String credential;

    public UserAuth(Long userId, String credential) {
        this.userId = userId;
        this.credential = credential;
    }
}