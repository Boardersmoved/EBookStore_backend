package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.dto.UpdateUserProfileRequestDto;
import com.example.ebookstore_backend.dto.UserProfileResponseDto;

import java.util.List;

public interface UserService {
    /**
     * 获取当前认证用户的个人资料
     * @return UserProfileResponseDto 包含用户资料
     */
    UserProfileResponseDto getCurrentUserProfile();

    /**
     * 更新当前认证用户的个人资料
     * @param requestDto 包含要更新的字段
     * @return 更新后的 UserProfileResponseDto
     */
    UserProfileResponseDto updateCurrentUserProfile(UpdateUserProfileRequestDto requestDto);

    /**
     * 管理员功能：启用或禁用用户账户
     * @param userId 用户ID
     * @param valid 账户是否有效（true=启用，false=禁用）
     * @return 更新后的用户信息
     */
    UserProfileResponseDto setUserAccountStatus(Long userId, Boolean valid);

    /**
     * 获取所有用户信息（管理员功能）
     * @return 所有用户的信息列表
     */
    List<UserProfileResponseDto> getAllUsers();

}
