package com.example.ebookstore_backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsService {
    /**
     * 根据用户名加载用户信息
     * @param username 用户名
     * @return 用户的详细信息
     * @throws UsernameNotFoundException 如果用户未找到
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}