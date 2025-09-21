package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.UpdateUserProfileRequestDto;
import com.example.ebookstore_backend.dto.UserProfileResponseDto;
import com.example.ebookstore_backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile") // 个人资料相关的API基础路径
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前登录用户的个人资料
     * @return UserProfileResponseDto
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "anonymous";
        logger.info("Request received for /api/profile/me by user: {}", username);

        UserProfileResponseDto userProfile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(userProfile);
    }

    /**
     * 更新当前登录用户的个人资料
     * @param updateUserProfileRequestDto 包含要更新的字段
     * @return 更新后的 UserProfileResponseDto
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDto> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequestDto updateUserProfileRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "anonymous";
        logger.info("Request received to update profile for user: {} with data: {}", username, updateUserProfileRequestDto);

        UserProfileResponseDto updatedProfile = userService.updateCurrentUserProfile(updateUserProfileRequestDto);
        return ResponseEntity.ok(updatedProfile);
    }

    // TODO: 后续将添加头像上传的API接口
}