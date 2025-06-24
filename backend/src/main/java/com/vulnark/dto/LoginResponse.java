package com.vulnark.dto;

import com.vulnark.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录响应")
public class LoginResponse {
    
    @Schema(description = "JWT令牌")
    private String token;
    
    @Schema(description = "用户信息")
    private UserInfo user;
    
    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = new UserInfo(user);
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }
    
    @Schema(description = "用户信息")
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private String avatarUrl;
        private User.Role role;
        private User.Status status;
        
        public UserInfo(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.phone = user.getPhone();
            this.avatarUrl = user.getAvatarUrl();
            this.role = user.getRole();
            this.status = user.getStatus();
        }
        
        // Getters and Setters
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
        
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
        
        public User.Role getRole() {
            return role;
        }

        public void setRole(User.Role role) {
            this.role = role;
        }

        public User.Status getStatus() {
            return status;
        }

        public void setStatus(User.Status status) {
            this.status = status;
        }
    }
}
