package com.example.ebookstore_backend.dto;

import com.example.ebookstore_backend.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class UserProfileResponseDto {
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
    private boolean valid;

    public static UserProfileResponseDto fromEntity(User user) {
        if (user == null) {
            return null;
        }
        UserProfileResponseDto dto = new UserProfileResponseDto();
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