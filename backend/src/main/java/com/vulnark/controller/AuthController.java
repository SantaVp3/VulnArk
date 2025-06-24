package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.dto.LoginRequest;
import com.vulnark.dto.LoginResponse;
import com.vulnark.dto.RegisterRequest;
import com.vulnark.entity.User;
import com.vulnark.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ApiResponse.success("注册成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return ApiResponse.success("获取用户信息成功", user);
            }
            return ApiResponse.unauthorized();
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        // JWT是无状态的，客户端删除token即可
        return ApiResponse.success("登出成功");
    }
}
