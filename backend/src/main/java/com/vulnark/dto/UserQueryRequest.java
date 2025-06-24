package com.vulnark.dto;

import com.vulnark.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户查询请求DTO")
public class UserQueryRequest {
    
    @Schema(description = "用户名关键词")
    private String username;
    
    @Schema(description = "邮箱关键词")
    private String email;
    
    @Schema(description = "全名关键词")
    private String fullName;
    
    @Schema(description = "角色")
    private User.Role role;
    
    @Schema(description = "状态")
    private User.Status status;
    
    @Schema(description = "部门关键词")
    private String department;
    
    @Schema(description = "职位关键词")
    private String position;
    
    @Schema(description = "搜索关键词")
    private String keyword;
    
    @Schema(description = "页码", example = "0")
    private Integer page = 0;
    
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
    
    @Schema(description = "排序字段", example = "createdTime")
    private String sortBy = "createdTime";
    
    @Schema(description = "排序方向", example = "desc")
    private String sortDir = "desc";
    
    // Constructors
    public UserQueryRequest() {}
    
    // Getters and Setters
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
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDir() {
        return sortDir;
    }
    
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
