package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.entity.UserAuth;
import com.example.ebookstore_backend.dto.LoginRequestDto;
import com.example.ebookstore_backend.dto.RegisterRequestDto;
import com.example.ebookstore_backend.exception.AccountDisabledException;
import com.example.ebookstore_backend.exception.UserAlreadyExistsException;
import com.example.ebookstore_backend.dao.UserDao;
import com.example.ebookstore_backend.service.AuthService;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequestDto registerRequestDto) {
        if (userDao.existsByUsername(registerRequestDto.getUsername())) {
            throw new UserAlreadyExistsException("注册失败：用户名 '" + registerRequestDto.getUsername() + "' 已被使用。");
        }
        if (userDao.existsByEmail(registerRequestDto.getEmail())) {
            throw new UserAlreadyExistsException("注册失败：邮箱 '" + registerRequestDto.getEmail() + "' 已被注册。");
        }

        User user = new User();
        user.setUsername(registerRequestDto.getUsername());
        user.setEmail(registerRequestDto.getEmail());
        user.setNickname(registerRequestDto.getNickname() != null ? registerRequestDto.getNickname() : registerRequestDto.getUsername());
        user.setRole("USER");
        user.setBalance(new BigDecimal("1000.00"));
        user.setValid(true);

        User savedUser = userDao.save(user);

        // 创建认证信息，使用user_id作为主键
        UserAuth userAuth = new UserAuth(
            savedUser.getId(), 
            passwordEncoder.encode(registerRequestDto.getPassword())
        );
        userDao.save(userAuth);

        logger.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    @Autowired
    private UserDetailsService userDetailsServiceImpl;

    public User loginUser(LoginRequestDto loginRequestDto) {
        try {
            // 1. 加载用户（这里会检查账户是否被禁用）
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(loginRequestDto.getUsername());
            
            // 2. 验证密码
            if (!passwordEncoder.matches(loginRequestDto.getPassword(), userDetails.getPassword())) {
                logger.warn("Password mismatch for user: {}", loginRequestDto.getUsername());
                throw new BadCredentialsException("用户名或密码错误");
            }
            
            // 3. 创建认证对象
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, 
                    null, // 已认证的Authentication不应包含凭据
                    userDetails.getAuthorities()
                );
                
            // 4. 设置认证上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 5. 返回用户信息
            return userDao.findByUsername(loginRequestDto.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("用户信息未找到"));
                    
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found: {}", loginRequestDto.getUsername());
            throw new BadCredentialsException("用户名或密码错误");
        } catch (AccountDisabledException e) {
            // 账户被禁用异常直接抛出，不转换
            logger.warn("Account disabled for user: {}", loginRequestDto.getUsername());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("getCurrentAuthenticatedUser - Authentication object from SecurityContext: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("getCurrentAuthenticatedUser - User is not authenticated or is anonymous. Principal: {}", (authentication != null ? authentication.getPrincipal() : "null"));
            return null;
        }
        String username = authentication.getName();
        logger.info("getCurrentAuthenticatedUser - Authenticated user: {}", username);
        return userDao.findByUsername(username).orElseGet(() -> {
            logger.warn("getCurrentAuthenticatedUser - User {} found in SecurityContext but not in repository.", username);
            return null; 
        });
    }
}