package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.UserProfileResponseDto;
import com.example.ebookstore_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取所有用户信息（管理员功能）
     *
     * @return 所有用户的信息列表
     */
    @GetMapping
    public ResponseEntity<List<UserProfileResponseDto>> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous";

        logger.info("Admin user '{}' is requesting all users information", currentUsername);

        List<UserProfileResponseDto> allUsers = userService.getAllUsers();

        logger.info("Retrieved {} users for admin user '{}'", allUsers.size(), currentUsername);
        return ResponseEntity.ok(allUsers);
    }

    /**
     * 管理员功能：启用或禁用用户账户
     * @param userId 用户ID
     * @param valid 账户是否有效（true=启用，false=禁用）
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<UserProfileResponseDto> setUserAccountStatus(
            @PathVariable Long userId,
            @RequestParam Boolean valid) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal()))
                ? authentication.getName() : "anonymous";
        
        String action = valid ? "启用" : "禁用";
        logger.info("Admin user '{}' is attempting to {} user with ID: {}", 
                    currentUsername, action, userId);
        
        UserProfileResponseDto updatedUser = userService.setUserAccountStatus(userId, valid);
        
        logger.info("User account status updated successfully by admin user '{}' - User ID: {}, New Status: {}", 
                    currentUsername, userId, valid ? "启用" : "禁用");
        
        return ResponseEntity.ok(updatedUser);
    }
}