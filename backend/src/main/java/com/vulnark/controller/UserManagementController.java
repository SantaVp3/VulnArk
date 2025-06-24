package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.dto.UserQueryRequest;
import com.vulnark.dto.UserRequest;
import com.vulnark.entity.User;
import com.vulnark.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {
    
    @Autowired
    private UserManagementService userManagementService;
    
    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<User> createUser(@Valid @RequestBody UserRequest request) {
        try {
            User user = userManagementService.createUser(request);
            return ApiResponse.success("用户创建成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        try {
            User user = userManagementService.updateUser(id, request);
            return ApiResponse.success("用户更新成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        try {
            userManagementService.deleteUser(id);
            return ApiResponse.success("用户删除成功", "删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据ID获取用户详情")
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        try {
            User user = userManagementService.getUserById(id);
            return ApiResponse.success("获取用户详情成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "分页查询用户")
    @GetMapping
    public ApiResponse<Page<User>> getUsers(
            @Parameter(description = "用户名关键词") @RequestParam(required = false) String username,
            @Parameter(description = "邮箱关键词") @RequestParam(required = false) String email,
            @Parameter(description = "全名关键词") @RequestParam(required = false) String fullName,
            @Parameter(description = "角色") @RequestParam(required = false) User.Role role,
            @Parameter(description = "状态") @RequestParam(required = false) User.Status status,
            @Parameter(description = "部门关键词") @RequestParam(required = false) String department,
            @Parameter(description = "职位关键词") @RequestParam(required = false) String position,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdTime") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            UserQueryRequest request = new UserQueryRequest();
            request.setUsername(username);
            request.setEmail(email);
            request.setFullName(fullName);
            request.setRole(role);
            request.setStatus(status);
            request.setDepartment(department);
            request.setPosition(position);
            request.setKeyword(keyword);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDir(sortDir);
            
            Page<User> users = userManagementService.getUsers(request);
            return ApiResponse.success("获取用户列表成功", users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取所有用户")
    @GetMapping("/all")
    public ApiResponse<List<User>> getAllUsers() {
        try {
            List<User> users = userManagementService.getAllUsers();
            return ApiResponse.success("获取所有用户成功", users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据角色获取用户")
    @GetMapping("/role/{role}")
    public ApiResponse<List<User>> getUsersByRole(
            @Parameter(description = "角色") @PathVariable User.Role role) {
        try {
            List<User> users = userManagementService.getUsersByRole(role);
            return ApiResponse.success("获取角色用户成功", users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据状态获取用户")
    @GetMapping("/status/{status}")
    public ApiResponse<List<User>> getUsersByStatus(
            @Parameter(description = "状态") @PathVariable User.Status status) {
        try {
            List<User> users = userManagementService.getUsersByStatus(status);
            return ApiResponse.success("获取状态用户成功", users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据部门获取用户")
    @GetMapping("/department/{department}")
    public ApiResponse<List<User>> getUsersByDepartment(
            @Parameter(description = "部门") @PathVariable String department) {
        try {
            List<User> users = userManagementService.getUsersByDepartment(department);
            return ApiResponse.success("获取部门用户成功", users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    public ApiResponse<User> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            User.Status status = User.Status.valueOf(request.get("status"));
            User user = userManagementService.updateUserStatus(id, status);
            return ApiResponse.success("用户状态更新成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password")
    public ApiResponse<User> resetUserPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("password");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ApiResponse.error("新密码不能为空");
            }
            User user = userManagementService.resetUserPassword(id, newPassword);
            return ApiResponse.success("密码重置成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取用户统计信息")
    @GetMapping("/stats")
    public ApiResponse<UserManagementService.UserStats> getUserStats() {
        try {
            UserManagementService.UserStats stats = userManagementService.getUserStats();
            return ApiResponse.success("获取用户统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "批量导入用户")
    @PostMapping("/import")
    public ApiResponse<List<User>> importUsers(@RequestBody List<UserRequest> requests) {
        try {
            List<User> users = userManagementService.importUsers(requests);
            return ApiResponse.success("用户导入成功", users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "批量导出用户")
    @PostMapping("/export")
    public ApiResponse<List<User>> exportUsers(@RequestBody(required = false) List<Long> userIds) {
        try {
            List<User> users = userManagementService.exportUsers(userIds);
            return ApiResponse.success("用户导出成功", users);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
