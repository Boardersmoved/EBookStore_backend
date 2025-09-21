package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.entity.UserAuth;
import com.example.ebookstore_backend.exception.AccountDisabledException;
import com.example.ebookstore_backend.repository.UserAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserAuthRepository userAuthRepository;

    @Autowired
    public UserDetailsServiceImpl(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 通过用户名查找认证信息（使用关联查询）
        UserAuth userAuth = userAuthRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户名或密码错误"));

        User user = userAuth.getUser(); // 获取关联的User对象
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 检查账户是否被禁用
        if (!user.getValid()) {
            logger.warn("Login attempt for disabled account: {}", username);
            throw new AccountDisabledException("您的账号已经被禁用");
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        // 从User实体中获取角色信息并转换为GrantedAuthority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),       // 从User实体获取用户名
                userAuth.getCredential(), // 加密后的密码
                authorities               // 用户的权限集合
        );
    }
}