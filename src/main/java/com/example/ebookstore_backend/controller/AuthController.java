package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.dto.LoginRequestDto;
import com.example.ebookstore_backend.dto.LoginResponseDto;
import com.example.ebookstore_backend.dto.RegisterRequestDto;
import com.example.ebookstore_backend.dto.UserDto;
import com.example.ebookstore_backend.entity.User;
import com.example.ebookstore_backend.service.AuthService;
import com.example.ebookstore_backend.service.SessionTimerService;
import jakarta.servlet.http.HttpServletRequest; // 用于登出
import jakarta.servlet.http.HttpServletResponse; // 用于登出
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Scope("singleton")
public class AuthController {

    private final AuthService authService;
    private final SessionTimerService sessionTimerService;

    @Autowired
    public AuthController(AuthService authService, SessionTimerService sessionTimerService) {
        this.authService = authService;
        this.sessionTimerService = sessionTimerService;
    }

    /**
     * 处理用户注册请求
     * @param registerRequestDto 包含用户名、邮箱、密码等注册信息
     * @return 注册成功或失败的响应
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        // @Valid 注解会触发DTO中定义的校验规则（如@NotBlank, @Email, @Size）
        // 如果校验失败，全局异常处理器会捕获 MethodArgumentNotValidException
        authService.registerUser(registerRequestDto);
        // 注册成功，返回201 Created状态码和成功消息
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("用户 '" + registerRequestDto.getUsername() + "' 注册成功！请登录。");
    }

    /**
     * 处理用户登录请求
     * @param loginRequestDto 包含用户名和密码
     * @return 登录成功则返回用户信息和成功消息，失败则由异常处理器返回错误
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpSession session) {
        User loggedInUser = authService.loginUser(loginRequestDto);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        // 登录成功后启动会话计时
        sessionTimerService.startTimer();

        UserDto userDto = UserDto.fromEntity(loggedInUser);
        LoginResponseDto response = new LoginResponseDto(
                "登录成功！欢迎回来, " + userDto.getUsername() + "。",
                userDto
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前已认证用户的信息 (对应前端的 getMe())
     * 此接口受Spring Security保护，只有已认证用户才能访问
     * @return 当前登录用户的 UserDto，如果未认证则返回401/403（由Spring Security处理）
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAuthenticatedUser() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        // authService.getCurrentAuthenticatedUser() 会从SecurityContextHolder获取用户信息
        // 如果用户未登录，Spring Security的配置应阻止匿名访问此接口，
        // 或者 authService.getCurrentAuthenticatedUser() 会返回 null

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("无法获取当前用户信息，请确认您已登录。");
        }

        UserDto userDto = UserDto.fromEntity(currentUser);
        return ResponseEntity.ok(userDto);
    }

    /**
     * 处理用户登出请求
     * Spring Security的logout配置通常会自动处理，但提供一个显式的API端点也是好的做法。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return 登出成功的响应
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        // 获取当前的认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 停止会话计时并获取会话时长
        long sessionDuration = sessionTimerService.stopTimer();
        if (authentication != null) {
            // 使用Spring Security提供的处理器来执行登出操作
            // 这会清除SecurityContext，使HTTP Session无效，并清除认证相关的Cookie
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        String formattedDuration = formatDuration(sessionDuration);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "您已成功登出。");
        responseData.put("sessionDuration", sessionDuration);
        responseData.put("formattedDuration", formattedDuration);
        return ResponseEntity.ok(responseData);
    }

    /**
     * 格式化时长显示
     * @param seconds 秒数
     * @return 格式化的时长字符串
     */
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d小时%d分钟%d秒", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, secs);
        } else {
            return String.format("%d秒", secs);
        }
    }
}