package com.vulnark.service;

import com.vulnark.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectService {
    
    /**
     * 创建项目
     */
    Project createProject(Project project, Long currentUserId);
    
    /**
     * 更新项目
     */
    Project updateProject(Long projectId, Project project, Long currentUserId);
    
    /**
     * 删除项目
     */
    void deleteProject(Long projectId, Long currentUserId);
    
    /**
     * 根据ID获取项目
     */
    Project getProjectById(Long projectId);
    
    /**
     * 获取所有项目
     */
    List<Project> getAllProjects();
    
    /**
     * 分页查询项目
     */
    Page<Project> getProjects(String name, Project.Status status, Project.Priority priority,
                             Long ownerId, String department, String keyword, 
                             Pageable pageable);
    
    /**
     * 根据状态查询项目
     */
    List<Project> getProjectsByStatus(Project.Status status);
    
    /**
     * 根据负责人查询项目
     */
    List<Project> getProjectsByOwner(Long ownerId);
    
    /**
     * 根据成员查询项目
     */
    List<Project> getProjectsByMember(Long memberId);
    
    /**
     * 添加项目成员
     */
    Project addProjectMember(Long projectId, Long memberId, Long currentUserId);
    
    /**
     * 移除项目成员
     */
    Project removeProjectMember(Long projectId, Long memberId, Long currentUserId);
    
    /**
     * 更新项目状态
     */
    Project updateProjectStatus(Long projectId, Project.Status status, Long currentUserId);
    
    /**
     * 获取项目统计信息
     */
    ProjectStats getProjectStats();
    
    /**
     * 获取即将到期的项目
     */
    List<Project> getProjectsEndingSoon(int days);
    
    /**
     * 获取最近创建的项目
     */
    List<Project> getRecentProjects(int limit);
    
    /**
     * 项目统计信息类
     */
    class ProjectStats {
        private long totalProjects;
        private long activeProjects;
        private long completedProjects;
        private long onHoldProjects;
        private long cancelledProjects;
        private long planningProjects;
        
        // Getters and Setters
        public long getTotalProjects() { return totalProjects; }
        public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }
        
        public long getActiveProjects() { return activeProjects; }
        public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }
        
        public long getCompletedProjects() { return completedProjects; }
        public void setCompletedProjects(long completedProjects) { this.completedProjects = completedProjects; }
        
        public long getOnHoldProjects() { return onHoldProjects; }
        public void setOnHoldProjects(long onHoldProjects) { this.onHoldProjects = onHoldProjects; }
        
        public long getCancelledProjects() { return cancelledProjects; }
        public void setCancelledProjects(long cancelledProjects) { this.cancelledProjects = cancelledProjects; }
        
        public long getPlanningProjects() { return planningProjects; }
        public void setPlanningProjects(long planningProjects) { this.planningProjects = planningProjects; }
    }
} 