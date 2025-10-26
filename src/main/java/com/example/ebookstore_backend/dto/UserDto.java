package com.example.ebookstore_backend.dto;

import com.example.ebookstore_backend.entity.User;
import lombok.Data;

import java.io.Serializable;  
import java.math.BigDecimal;

@Data
public class UserDto implements Serializable {  
    
    private static final long serialVersionUID = 1L;  
    
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatarBase64; 
    private String role;
    private String bio;
    private String phone;
    private BigDecimal balance;
    private String addressText;
    private Boolean valid;

    public static UserDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setAvatarBase64(user.getAvatarBase64());
        dto.setRole(user.getRole());
        dto.setBio(user.getBio());
        dto.setPhone(user.getPhone());
        dto.setBalance(user.getBalance());
        dto.setAddressText(user.getAddressText());
        dto.setValid(user.getValid());
        return dto;
    }
}