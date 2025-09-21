package com.example.ebookstore_backend.service;

import com.example.ebookstore_backend.dto.LoginRequestDto;
import com.example.ebookstore_backend.dto.RegisterRequestDto;
import com.example.ebookstore_backend.entity.User; // 引入User

public interface AuthService {
    User registerUser(RegisterRequestDto registerRequestDto);
    User loginUser(LoginRequestDto loginRequestDto); // 登录成功返回User对象
    User getCurrentAuthenticatedUser(); // 新增方法，用于 /api/me
}