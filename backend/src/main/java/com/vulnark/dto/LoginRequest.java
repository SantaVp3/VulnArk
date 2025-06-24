package com.vulnark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "登录请求")
public class LoginRequest {
    
    @Schema(description = "用户名或邮箱", required = true)
    @NotBlank(message = "用户名或邮箱不能为空")
    private String username;
    
    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
