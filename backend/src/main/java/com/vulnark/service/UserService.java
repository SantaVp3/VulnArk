package com.vulnark.service;

import com.vulnark.dto.LoginRequest;
import com.vulnark.dto.LoginResponse;
import com.vulnark.dto.RegisterRequest;
import com.vulnark.entity.User;

import java.util.Optional;

public interface UserService {
    
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);
    
    /**
     * 用户注册
     */
    User register(RegisterRequest request);
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据用户名或邮箱查找用户
     */
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    
    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
}
