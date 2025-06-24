package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.dto.ProjectQueryRequest;
import com.vulnark.dto.ProjectRequest;
import com.vulnark.entity.Project;
import com.vulnark.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "项目管理", description = "项目管理相关接口")
@RestController
@RequestMapping("/projects")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Operation(summary = "创建项目")
    @PostMapping
    public ApiResponse<Project> createProject(@Valid @RequestBody ProjectRequest request) {
        try {
            Project project = projectService.createProject(request);
            return ApiResponse.success("项目创建成功", project);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public ApiResponse<Project> updateProject(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        try {
            Project project = projectService.updateProject(id, request);
            return ApiResponse.success("项目更新成功", project);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteProject(
            @Parameter(description = "项目ID") @PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ApiResponse.success("项目删除成功", "删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据ID获取项目详情")
    @GetMapping("/{id}")
    public ApiResponse<Project> getProjectById(
            @Parameter(description = "项目ID") @PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id);
            return ApiResponse.success("获取项目详情成功", project);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "分页查询项目")
    @GetMapping
    public ApiResponse<Page<Project>> getProjects(
            @Parameter(description = "项目名称关键词") @RequestParam(required = false) String name,
            @Parameter(description = "项目状态") @RequestParam(required = false) Project.Status status,
            @Parameter(description = "项目优先级") @RequestParam(required = false) Project.Priority priority,
            @Parameter(description = "项目类型关键词") @RequestParam(required = false) String type,
            @Parameter(description = "项目负责人ID") @RequestParam(required = false) Long ownerId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdTime") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            ProjectQueryRequest request = new ProjectQueryRequest();
            request.setName(name);
            request.setStatus(status);
            request.setPriority(priority);
            request.setType(type);
            request.setOwnerId(ownerId);
            request.setKeyword(keyword);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDir(sortDir);
            
            Page<Project> projects = projectService.getProjects(request);
            return ApiResponse.success("获取项目列表成功", projects);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取所有项目")
    @GetMapping("/all")
    public ApiResponse<List<Project>> getAllProjects() {
        try {
            List<Project> projects = projectService.getAllProjects();
            return ApiResponse.success("获取所有项目成功", projects);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据负责人ID获取项目")
    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Project>> getProjectsByOwnerId(
            @Parameter(description = "负责人ID") @PathVariable Long ownerId) {
        try {
            List<Project> projects = projectService.getProjectsByOwnerId(ownerId);
            return ApiResponse.success("获取负责人项目成功", projects);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取最近的项目")
    @GetMapping("/recent")
    public ApiResponse<List<Project>> getRecentProjects(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Project> projects = projectService.getRecentProjects(limit);
            return ApiResponse.success("获取最近项目成功", projects);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取即将到期的项目")
    @GetMapping("/overdue")
    public ApiResponse<List<Project>> getOverdueProjects() {
        try {
            List<Project> projects = projectService.getOverdueProjects();
            return ApiResponse.success("获取即将到期项目成功", projects);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取活跃项目")
    @GetMapping("/active")
    public ApiResponse<List<Project>> getActiveProjects() {
        try {
            List<Project> projects = projectService.getActiveProjects();
            return ApiResponse.success("获取活跃项目成功", projects);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新项目状态")
    @PutMapping("/{id}/status")
    public ApiResponse<Project> updateProjectStatus(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Project.Status status = Project.Status.valueOf(request.get("status"));
            Project project = projectService.updateProjectStatus(id, status);
            return ApiResponse.success("项目状态更新成功", project);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新项目进度")
    @PutMapping("/{id}/progress")
    public ApiResponse<Project> updateProjectProgress(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer progress = request.get("progress");
            Project project = projectService.updateProjectProgress(id, progress);
            return ApiResponse.success("项目进度更新成功", project);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新项目统计信息")
    @PutMapping("/{id}/statistics")
    public ApiResponse<Project> updateProjectStatistics(
            @Parameter(description = "项目ID") @PathVariable Long id) {
        try {
            Project project = projectService.updateProjectStatistics(id);
            return ApiResponse.success("项目统计信息更新成功", project);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取项目统计信息")
    @GetMapping("/stats")
    public ApiResponse<ProjectService.ProjectStats> getProjectStats() {
        try {
            ProjectService.ProjectStats stats = projectService.getProjectStats();
            return ApiResponse.success("获取项目统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "批量导入项目")
    @PostMapping("/import")
    public ApiResponse<List<Project>> importProjects(@RequestBody List<ProjectRequest> requests) {
        try {
            List<Project> projects = projectService.importProjects(requests);
            return ApiResponse.success("项目导入成功", projects);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "批量导出项目")
    @PostMapping("/export")
    public ApiResponse<List<Project>> exportProjects(@RequestBody(required = false) List<Long> projectIds) {
        try {
            List<Project> projects = projectService.exportProjects(projectIds);
            return ApiResponse.success("项目导出成功", projects);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
