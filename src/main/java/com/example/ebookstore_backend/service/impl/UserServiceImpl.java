package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.dto.UpdateUserProfileRequestDto;
import com.example.ebookstore_backend.dto.UserProfileResponseDto;
import com.example.ebookstore_backend.exception.ResourceNotFoundException;
import com.example.ebookstore_backend.exception.UnauthorizedOperationException;
import com.example.ebookstore_backend.exception.UserAlreadyExistsException;
import com.example.ebookstore_backend.dao.UserDao;
import com.example.ebookstore_backend.service.AuthService;
import com.example.ebookstore_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final AuthService authService;
    private final UserServiceImpl self;

    @Autowired
    public UserServiceImpl(UserDao userDao, AuthService authService, @Lazy UserServiceImpl self) {
        this.userDao = userDao;
        this.authService = authService;
        this.self = self;
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
        logger.info("【查询用户资料】Username: {}, ID: {}", currentUser.getUsername(), currentUser.getId());
        
        // 通过代理对象调用缓存方法
        return self.getUserProfileByIdWithCache(currentUser.getId());
    }

    /**
     * 带缓存的用户资料查询
     * 必须通过 self 代理对象调用
     */
    @Cacheable(value = "userProfile", key = "#userId", unless = "#result == null")
    public UserProfileResponseDto getUserProfileByIdWithCache(Long userId) {
        logger.info("缓存未命中：从数据库查询用户资料 - ID: {}", userId);
        
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        UserProfileResponseDto dto = UserProfileResponseDto.fromEntity(user);
        logger.info("写入用户缓存 - ID: {}, Username: {}", userId, user.getUsername());
        
        return dto;
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfile", key = "#result.id")
    public UserProfileResponseDto updateCurrentUserProfile(UpdateUserProfileRequestDto requestDto) {
        User currentUser = getAuthenticatedUser();
        logger.info("【更新用户资料】Username: {}, ID: {}", currentUser.getUsername(), currentUser.getId());

        boolean updated = false;

        // 更新邮箱
        if (StringUtils.hasText(requestDto.getEmail()) && !Objects.equals(currentUser.getEmail(), requestDto.getEmail())) {
            if (userDao.existsByEmail(requestDto.getEmail())) {
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
        if (requestDto.getNickname() != null) {
            currentUser.setNickname(requestDto.getNickname());
            updated = true;
        }

        // 更新个人简介
        if (requestDto.getBio() != null) {
            currentUser.setBio(requestDto.getBio());
            updated = true;
        }

        // 更新头像
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

        if (updated) {
            User updatedUser = userDao.save(currentUser);
            logger.info("用户资料更新成功并清除缓存 - ID: {}", updatedUser.getId());
            return UserProfileResponseDto.fromEntity(updatedUser);
        } else {
            logger.info("用户资料无变化 - ID: {}", currentUser.getId());
            return UserProfileResponseDto.fromEntity(currentUser);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> getAllUsers() {
        logger.info("管理员查询所有用户信息");

        List<User> allUsers = userDao.findAll();
        List<UserProfileResponseDto> userDtos = allUsers.stream()
                .map(UserProfileResponseDto::fromEntity)
                .collect(Collectors.toList());

        logger.info("查询到 {} 个用户", userDtos.size());
        return userDtos;
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfile", key = "#userId")
    public UserProfileResponseDto setUserAccountStatus(Long userId, Boolean valid) {
        User currentUser = getAuthenticatedUser();
        
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            logger.warn("User '{}' with role '{}' attempted to modify user account status. Access denied.", 
                        currentUser.getUsername(), currentUser.getRole());
            throw new UnauthorizedOperationException("只有管理员才能修改用户账户状态。");
        }

        User targetUser = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));

        Boolean oldStatus = targetUser.getValid();
        targetUser.setValid(valid);
        User updatedUser = userDao.save(targetUser);

        String action = valid ? "启用" : "禁用";
        logger.info("管理员 '{}' {} 用户 '{}' (ID: {}). 状态从 {} 变为 {}", 
                    currentUser.getUsername(), action, targetUser.getUsername(), 
                    userId, oldStatus, valid);
        logger.info("已清除用户缓存 - ID: {}", userId);

        return UserProfileResponseDto.fromEntity(updatedUser);
    }
}