package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.entity.Project;
import com.vulnark.entity.User;
import com.vulnark.security.CurrentUser;
import com.vulnark.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "项目管理", description = "项目管理相关接口")
@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "*")
public class ProjectController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    
    @Autowired
    private ProjectService projectService;
    
    @Operation(summary = "创建项目", description = "创建新的项目")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Project>> createProject(
            @Valid @RequestBody Project project,
            @CurrentUser User currentUser) {
        try {
            Project createdProject = projectService.createProject(project, currentUser.getId());
            logger.info("用户 {} 创建了项目: {}", currentUser.getUsername(), createdProject.getName());
            return ResponseEntity.ok(ApiResponse.success("项目创建成功", createdProject));
        } catch (Exception e) {
            logger.error("创建项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建项目失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "更新项目", description = "更新指定项目的信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Project>> updateProject(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @Valid @RequestBody Project project,
            @CurrentUser User currentUser) {
        try {
            Project updatedProject = projectService.updateProject(id, project, currentUser.getId());
            logger.info("用户 {} 更新了项目: {}", currentUser.getUsername(), updatedProject.getName());
            return ResponseEntity.ok(ApiResponse.success("项目更新成功", updatedProject));
        } catch (Exception e) {
            logger.error("更新项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新项目失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "删除项目", description = "删除指定的项目")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteProject(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @CurrentUser User currentUser) {
        try {
            projectService.deleteProject(id, currentUser.getId());
            logger.info("用户 {} 删除了项目: {}", currentUser.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.success("项目删除成功", null));
        } catch (Exception e) {
            logger.error("删除项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除项目失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "根据ID获取项目详情", description = "获取指定项目的详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProject(
            @Parameter(description = "项目ID") @PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);
            return ResponseEntity.ok(ApiResponse.success("获取项目详情成功", project));
        } catch (Exception e) {
            logger.error("获取项目详情失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取项目详情失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取所有项目", description = "获取所有项目列表")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Project>>> getAllProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            return ResponseEntity.ok(ApiResponse.success("获取所有项目成功", projects));
        } catch (Exception e) {
            logger.error("获取所有项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取所有项目失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "分页查询项目", description = "根据条件分页查询项目")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Project>>> getProjects(
            @Parameter(description = "项目名称关键词") @RequestParam(required = false) String name,
            @Parameter(description = "项目状态") @RequestParam(required = false) Project.Status status,
            @Parameter(description = "优先级") @RequestParam(required = false) Project.Priority priority,
            @Parameter(description = "负责人ID") @RequestParam(required = false) Long ownerId,
            @Parameter(description = "部门关键词") @RequestParam(required = false) String department,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdTime") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Project> projects = projectService.getProjects(
                    name, status, priority, ownerId, department, keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success("获取项目列表成功", projects));
        } catch (Exception e) {
            logger.error("获取项目列表失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取项目列表失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "根据状态获取项目", description = "获取指定状态的项目")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByStatus(
            @Parameter(description = "项目状态") @PathVariable Project.Status status) {
        try {
            List<Project> projects = projectService.getProjectsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("获取状态项目成功", projects));
        } catch (Exception e) {
            logger.error("获取状态项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取状态项目失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "根据负责人获取项目", description = "获取指定负责人的项目")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByOwner(
            @Parameter(description = "负责人ID") @PathVariable Long ownerId) {
        try {
            List<Project> projects = projectService.getProjectsByOwner(ownerId);
            return ResponseEntity.ok(ApiResponse.success("获取负责人项目成功", projects));
        } catch (Exception e) {
            logger.error("获取负责人项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取负责人项目失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "根据成员获取项目", description = "获取指定成员参与的项目")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByMember(
            @Parameter(description = "成员ID") @PathVariable Long memberId) {
        try {
            List<Project> projects = projectService.getProjectsByMember(memberId);
            return ResponseEntity.ok(ApiResponse.success("获取成员项目成功", projects));
        } catch (Exception e) {
            logger.error("获取成员项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取成员项目失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "添加项目成员", description = "为项目添加新成员")
    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Project>> addProjectMember(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @RequestBody Map<String, Long> request,
            @CurrentUser User currentUser) {
        try {
            Long memberId = request.get("memberId");
            Project project = projectService.addProjectMember(id, memberId, currentUser.getId());
            logger.info("用户 {} 为项目 {} 添加了成员: {}", currentUser.getUsername(), id, memberId);
            return ResponseEntity.ok(ApiResponse.success("添加项目成员成功", project));
        } catch (Exception e) {
            logger.error("添加项目成员失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("添加项目成员失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "移除项目成员", description = "从项目中移除成员")
    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Project>> removeProjectMember(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @Parameter(description = "成员ID") @PathVariable Long memberId,
            @CurrentUser User currentUser) {
        try {
            Project project = projectService.removeProjectMember(id, memberId, currentUser.getId());
            logger.info("用户 {} 从项目 {} 移除了成员: {}", currentUser.getUsername(), id, memberId);
            return ResponseEntity.ok(ApiResponse.success("移除项目成员成功", project));
        } catch (Exception e) {
            logger.error("移除项目成员失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("移除项目成员失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "更新项目状态", description = "更新项目的状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Project>> updateProjectStatus(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @CurrentUser User currentUser) {
        try {
            Project.Status status = Project.Status.valueOf(request.get("status"));
            Project project = projectService.updateProjectStatus(id, status, currentUser.getId());
            logger.info("用户 {} 更新了项目 {} 的状态为: {}", currentUser.getUsername(), id, status);
            return ResponseEntity.ok(ApiResponse.success("项目状态更新成功", project));
        } catch (Exception e) {
            logger.error("更新项目状态失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新项目状态失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取项目统计信息", description = "获取项目统计数据")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ProjectService.ProjectStats>> getProjectStats() {
        try {
            ProjectService.ProjectStats stats = projectService.getProjectStats();
            return ResponseEntity.ok(ApiResponse.success("获取项目统计成功", stats));
        } catch (Exception e) {
            logger.error("获取项目统计失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取项目统计失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取即将到期的项目", description = "获取即将在指定天数内到期的项目")
    @GetMapping("/ending-soon")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsEndingSoon(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        try {
            List<Project> projects = projectService.getProjectsEndingSoon(days);
            return ResponseEntity.ok(ApiResponse.success("获取即将到期项目成功", projects));
        } catch (Exception e) {
            logger.error("获取即将到期项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取即将到期项目失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取最近创建的项目", description = "获取最近创建的项目")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Project>>> getRecentProjects(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Project> projects = projectService.getRecentProjects(limit);
            return ResponseEntity.ok(ApiResponse.success("获取最近项目成功", projects));
        } catch (Exception e) {
            logger.error("获取最近项目失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取最近项目失败: " + e.getMessage()));
        }
    }
} 