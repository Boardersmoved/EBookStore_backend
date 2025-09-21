package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.dto.UpdateUserProfileRequestDto;
import com.example.ebookstore_backend.dto.UserProfileResponseDto;
import com.example.ebookstore_backend.exception.ResourceNotFoundException;
import com.example.ebookstore_backend.exception.UnauthorizedOperationException;
import com.example.ebookstore_backend.exception.UserAlreadyExistsException; // 用于邮箱唯一性检查
import com.example.ebookstore_backend.dao.UserDao;
import com.example.ebookstore_backend.service.AuthService; // 用于获取当前用户
import com.example.ebookstore_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // 用于检查字符串是否为空白

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final AuthService authService; // 用于获取当前登录用户

    public UserServiceImpl(UserDao userDao, AuthService authService) {
        this.userDao = userDao;
        this.authService = authService;
    }


    private User getAuthenticatedUser() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            throw new IllegalStateException("用户未登录，无法执行此操作。");
        }
        return currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getCurrentUserProfile() {
        User currentUser = getAuthenticatedUser();
        logger.info("Fetching profile for user: {}", currentUser.getUsername());
        // User实体可能在getAuthenticatedUser中已经从数据库加载，这里直接转换
        return UserProfileResponseDto.fromEntity(currentUser);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateCurrentUserProfile(UpdateUserProfileRequestDto requestDto) {
        User currentUser = getAuthenticatedUser(); // 获取当前登录用户实体（应为受管状态）
        logger.info("Updating profile for user: {}", currentUser.getUsername());

        boolean updated = false;

        // 更新邮箱 (如果提供了并且与当前不同)
        if (StringUtils.hasText(requestDto.getEmail()) && !Objects.equals(currentUser.getEmail(), requestDto.getEmail())) {
            // 检查新邮箱是否已被其他用户使用
            if (userDao.existsByEmail(requestDto.getEmail())) {
                // 注意：如果允许用户修改为自己已有的邮箱（虽然这里逻辑是不同才更新），则不需要此检查
                // 但如果一个邮箱只能被一个账户使用，则此检查是必要的
                User existingUserWithEmail = userDao.findByEmail(requestDto.getEmail()).orElse(null);
                if (existingUserWithEmail != null && !Objects.equals(existingUserWithEmail.getId(), currentUser.getId())) {
                    logger.warn("User {} attempted to update email to {}, which is already in use by another user.",
                            currentUser.getUsername(), requestDto.getEmail());
                    throw new UserAlreadyExistsException("邮箱 '" + requestDto.getEmail() + "' 已被其他用户注册。");
                }
            }
            currentUser.setEmail(requestDto.getEmail());
            updated = true;
        }

        // 更新昵称
        if (requestDto.getNickname() != null) { // 允许设置为空字符串，如果业务允许
            currentUser.setNickname(requestDto.getNickname());
            updated = true;
        }

        // 更新个人简介
        if (requestDto.getBio() != null) {
            currentUser.setBio(requestDto.getBio());
            updated = true;
        }

        // 更新头像（Base64字符串）
        if (requestDto.getAvatarBase64() != null) {
            currentUser.setAvatarBase64(requestDto.getAvatarBase64());
            updated = true;
        }

        // 更新联系电话
        if (requestDto.getPhone() != null) {
            currentUser.setPhone(requestDto.getPhone());
            updated = true;
        }

        // 更新常用住址
        if (requestDto.getAddressText() != null) {
            currentUser.setAddressText(requestDto.getAddressText());
            updated = true;
        }

        // 如果有实际更新，则保存
        if (updated) {
                User updatedUser = userDao.save(currentUser); // JPA会自动处理更新
                logger.info("Profile updated successfully for user: {}", updatedUser.getUsername());
                return UserProfileResponseDto.fromEntity(updatedUser);
            } else {
                logger.info("No changes detected for user profile: {}", currentUser.getUsername());
                return UserProfileResponseDto.fromEntity(currentUser); // 没有变化，返回当前信息
            }
        }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getAllUsers() {
        logger.info("Fetching all users information");

        List<User> allUsers = userDao.findAll();

        // 将User实体列表转换为UserProfileResponseDto列表
        List<UserProfileResponseDto> userDtos = allUsers.stream()
                .map(UserProfileResponseDto::fromEntity)
                .collect(Collectors.toList());

        logger.info("Retrieved {} users", userDtos.size());
        return userDtos;
    }

     @Override
    @Transactional
    public UserProfileResponseDto setUserAccountStatus(Long userId, Boolean valid) {
        // 获取当前管理员用户
        User currentUser = getAuthenticatedUser();
        
        // 验证当前用户是否为管理员
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            logger.warn("User '{}' with role '{}' attempted to modify user account status. Access denied.", 
                        currentUser.getUsername(), currentUser.getRole());
            throw new UnauthorizedOperationException("只有管理员才能修改用户账户状态。");
        }

        // 查找目标用户
        User targetUser = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));


        // 更新用户状态
        Boolean oldStatus = targetUser.getValid();
        targetUser.setValid(valid);
        User updatedUser = userDao.save(targetUser);

        String action = valid ? "启用" : "禁用";
        logger.info("Admin user '{}' has {} user '{}' (ID: {}). Status changed from {} to {}", 
                    currentUser.getUsername(), action, targetUser.getUsername(), 
                    userId, oldStatus, valid);

        return UserProfileResponseDto.fromEntity(updatedUser);
    }
}