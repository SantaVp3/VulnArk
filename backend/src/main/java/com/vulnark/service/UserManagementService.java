package com.vulnark.service;

import com.vulnark.dto.UserQueryRequest;
import com.vulnark.dto.UserRequest;
import com.vulnark.entity.User;
import com.vulnark.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 创建用户
     */
    public User createUser(UserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        
        User user = new User();
        BeanUtils.copyProperties(request, user, "password");
        
        // 加密密码
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(User.Status.ACTIVE);
        }
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * 更新用户
     */
    public User updateUser(Long id, UserRequest request) {
        User user = getUserById(id);
        
        // 检查用户名是否已被其他用户使用
        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }
        }
        
        // 检查邮箱是否已被其他用户使用
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("邮箱已存在");
            }
        }
        
        // 更新字段（排除密码）
        BeanUtils.copyProperties(request, user, "password");
        
        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        return userRepository.save(user);
    }
    
    /**
     * 删除用户（硬删除）
     */
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("用户不存在或已被删除"));
    }
    
    /**
     * 分页查询用户
     */
    public Page<User> getUsers(UserQueryRequest request) {
        // 创建分页对象
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 如果有关键词搜索，使用全文搜索
        if (StringUtils.hasText(request.getKeyword())) {
            return userRepository.searchByKeyword(request.getKeyword(), pageable);
        }
        
        // 否则使用条件查询
        return userRepository.findByConditions(
                request.getUsername(),
                request.getEmail(),
                request.getFullName(),
                request.getRole(),
                request.getStatus(),
                pageable
        );
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findByDeletedFalse();
    }
    
    /**
     * 根据角色获取用户
     */
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRoleAndDeletedFalse(role);
    }
    
    /**
     * 根据状态获取用户
     */
    public List<User> getUsersByStatus(User.Status status) {
        return userRepository.findByStatusAndDeletedFalse(status);
    }
    
    /**
     * 更新用户状态
     */
    public User updateUserStatus(Long id, User.Status status) {
        User user = getUserById(id);
        user.setStatus(status);
        return userRepository.save(user);
    }
    
    /**
     * 重置用户密码
     */
    public User resetUserPassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    
    /**
     * 批量导入用户
     */
    public List<User> importUsers(List<UserRequest> requests) {
        List<User> users = new ArrayList<>();
        for (UserRequest request : requests) {
            try {
                User user = createUser(request);
                users.add(user);
            } catch (Exception e) {
                // 记录错误但继续处理其他用户
                // 可以考虑返回导入结果详情
            }
        }
        return users;
    }
    
    /**
     * 批量导出用户
     */
    public List<User> exportUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return getAllUsers();
        }
        
        List<User> users = new ArrayList<>();
        for (Long id : userIds) {
            try {
                User user = getUserById(id);
                users.add(user);
            } catch (Exception e) {
                // 忽略不存在的用户
            }
        }
        return users;
    }
    
    /**
     * 获取用户统计信息
     */
    public UserStats getUserStats() {
        UserStats stats = new UserStats();
        stats.setTotal(userRepository.countByDeletedFalse());
        stats.setActive(userRepository.countByStatusAndDeletedFalse(User.Status.ACTIVE));
        stats.setInactive(userRepository.countByStatusAndDeletedFalse(User.Status.INACTIVE));
        stats.setAdmin(userRepository.countByRoleAndDeletedFalse(User.Role.ADMIN));
        stats.setManager(userRepository.countByRoleAndDeletedFalse(User.Role.MANAGER));
        stats.setUser(userRepository.countByRoleAndDeletedFalse(User.Role.USER));
        return stats;
    }
    
    // 内部类：用户统计信息
    public static class UserStats {
        private long total;
        private long active;
        private long inactive;
        private long admin;
        private long manager;
        private long user;
        
        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getActive() { return active; }
        public void setActive(long active) { this.active = active; }
        public long getInactive() { return inactive; }
        public void setInactive(long inactive) { this.inactive = inactive; }
        public long getAdmin() { return admin; }
        public void setAdmin(long admin) { this.admin = admin; }
        public long getManager() { return manager; }
        public void setManager(long manager) { this.manager = manager; }
        public long getUser() { return user; }
        public void setUser(long user) { this.user = user; }
    }
}
