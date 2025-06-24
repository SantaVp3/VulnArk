package com.vulnark.service.impl;

import com.vulnark.dto.LoginRequest;
import com.vulnark.dto.LoginResponse;
import com.vulnark.dto.RegisterRequest;
import com.vulnark.entity.User;
import com.vulnark.repository.UserRepository;
import com.vulnark.security.JwtTokenProvider;
import com.vulnark.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查找用户
        Optional<User> userOpt = findByUsernameOrEmail(request.getUsername());
        if (!userOpt.isPresent()) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        User user = userOpt.get();
        
        // 检查用户状态
        if (user.getStatus() != User.Status.ACTIVE) {
            throw new RuntimeException("账户已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 更新最后登录时间
        updateLastLoginTime(user.getId());
        
        // 生成JWT令牌
        String token = jwtTokenProvider.generateToken(user.getUsername());
        
        return new LoginResponse(token, user);
    }
    
    @Override
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(User.Role.VIEWER); // 默认角色
        user.setStatus(User.Status.ACTIVE); // 默认状态
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail);
    }
    
    @Override
    public void updateLastLoginTime(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
