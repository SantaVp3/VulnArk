package com.vulnark.service;

import com.vulnark.dto.ProjectQueryRequest;
import com.vulnark.dto.ProjectRequest;
import com.vulnark.entity.Project;
import com.vulnark.repository.ProjectRepository;
import com.vulnark.repository.VulnerabilityRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    /**
     * 创建项目
     */
    public Project createProject(ProjectRequest request) {
        // 检查项目名称是否已存在
        Optional<Project> existingProject = projectRepository.findByNameAndDeletedFalse(request.getName());
        if (existingProject.isPresent()) {
            throw new RuntimeException("项目名称已存在");
        }
        
        Project project = new Project();
        BeanUtils.copyProperties(request, project);
        
        // 设置默认值
        if (project.getStatus() == null) {
            project.setStatus(Project.Status.ACTIVE);
        }
        if (project.getPriority() == null) {
            project.setPriority(Project.Priority.MEDIUM);
        }
        if (project.getMemberCount() == null) {
            project.setMemberCount(0);
        }
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        
        return projectRepository.save(project);
    }
    
    /**
     * 更新项目
     */
    public Project updateProject(Long id, ProjectRequest request) {
        Project project = getProjectById(id);
        
        // 检查项目名称是否已被其他项目使用
        if (!project.getName().equals(request.getName())) {
            Optional<Project> existingProject = projectRepository.findByNameAndDeletedFalse(request.getName());
            if (existingProject.isPresent()) {
                throw new RuntimeException("项目名称已存在");
            }
        }
        
        // 更新字段
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getOwnerId() != null) {
            project.setOwnerId(request.getOwnerId());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getType() != null) {
            project.setType(request.getType());
        }
        if (request.getPriority() != null) {
            project.setPriority(request.getPriority());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        if (request.getBudget() != null) {
            project.setBudget(request.getBudget());
        }
        if (request.getTags() != null) {
            project.setTags(request.getTags());
        }
        if (request.getMemberCount() != null) {
            project.setMemberCount(request.getMemberCount());
        }
        if (request.getProgress() != null) {
            project.setProgress(request.getProgress());
        }
        
        return projectRepository.save(project);
    }
    
    /**
     * 删除项目（逻辑删除）
     */
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        project.setDeleted(true);
        projectRepository.save(project);
    }
    
    /**
     * 根据ID获取项目
     */
    public Project getProjectById(Long id) {
        return projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("项目不存在或已被删除"));
    }
    
    /**
     * 分页查询项目
     */
    public Page<Project> getProjects(ProjectQueryRequest request) {
        // 创建分页对象
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 如果有关键词搜索，使用全文搜索
        if (StringUtils.hasText(request.getKeyword())) {
            return projectRepository.searchByKeyword(request.getKeyword(), pageable);
        }
        
        // 否则使用条件查询
        return projectRepository.findByConditions(
                request.getName(),
                request.getStatus(),
                request.getPriority(),
                request.getType(),
                request.getOwnerId(),
                request.getStartDate(),
                request.getEndDate(),
                pageable
        );
    }
    
    /**
     * 获取所有项目
     */
    public List<Project> getAllProjects() {
        return projectRepository.findByDeletedFalse();
    }
    
    /**
     * 根据负责人ID获取项目
     */
    public List<Project> getProjectsByOwnerId(Long ownerId) {
        return projectRepository.findByOwnerIdAndDeletedFalse(ownerId);
    }
    
    /**
     * 获取最近的项目
     */
    public List<Project> getRecentProjects(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return projectRepository.findRecentProjects(pageable);
    }
    
    /**
     * 获取即将到期的项目
     */
    public List<Project> getOverdueProjects() {
        return projectRepository.findOverdueProjects(LocalDate.now());
    }
    
    /**
     * 获取活跃项目
     */
    public List<Project> getActiveProjects() {
        return projectRepository.findActiveProjects();
    }
    
    /**
     * 更新项目状态
     */
    public Project updateProjectStatus(Long id, Project.Status status) {
        Project project = getProjectById(id);
        project.setStatus(status);
        return projectRepository.save(project);
    }
    
    /**
     * 更新项目进度
     */
    public Project updateProjectProgress(Long id, Integer progress) {
        if (progress < 0 || progress > 100) {
            throw new RuntimeException("项目进度必须在0-100之间");
        }
        
        Project project = getProjectById(id);
        project.setProgress(progress);
        
        // 如果进度达到100%，自动设置状态为已完成
        if (progress == 100 && project.getStatus() == Project.Status.ACTIVE) {
            project.setStatus(Project.Status.COMPLETED);
        }
        
        return projectRepository.save(project);
    }
    
    /**
     * 更新项目统计信息
     */
    public Project updateProjectStatistics(Long id) {
        Project project = getProjectById(id);
        
        // 更新漏洞数量
        long vulnerabilityCount = vulnerabilityRepository.countByProjectIdAndDeletedFalse(id);
        project.setVulnerabilityCount((int) vulnerabilityCount);
        
        // TODO: 更新资产数量（需要实现资产管理后）
        // long assetCount = assetRepository.countByProjectIdAndDeletedFalse(id);
        // project.setAssetCount((int) assetCount);
        
        return projectRepository.save(project);
    }
    
    /**
     * 获取项目统计信息
     */
    public ProjectStats getProjectStats() {
        ProjectStats stats = new ProjectStats();
        stats.setTotal(projectRepository.countByDeletedFalse());
        stats.setActive(projectRepository.countByStatusAndDeletedFalse(Project.Status.ACTIVE));
        stats.setCompleted(projectRepository.countByStatusAndDeletedFalse(Project.Status.COMPLETED));
        stats.setArchived(projectRepository.countByStatusAndDeletedFalse(Project.Status.ARCHIVED));
        stats.setHigh(projectRepository.countByPriorityAndDeletedFalse(Project.Priority.HIGH));
        stats.setCritical(projectRepository.countByPriorityAndDeletedFalse(Project.Priority.CRITICAL));
        return stats;
    }

    /**
     * 批量导入项目
     */
    public List<Project> importProjects(List<ProjectRequest> requests) {
        List<Project> projects = new ArrayList<>();
        for (ProjectRequest request : requests) {
            try {
                Project project = createProject(request);
                projects.add(project);
            } catch (Exception e) {
                // 记录错误但继续处理其他项目
                // 可以考虑返回导入结果详情
            }
        }
        return projects;
    }

    /**
     * 批量导出项目
     */
    public List<Project> exportProjects(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return getAllProjects();
        }

        List<Project> projects = new ArrayList<>();
        for (Long id : projectIds) {
            try {
                Project project = getProjectById(id);
                projects.add(project);
            } catch (Exception e) {
                // 忽略不存在的项目
            }
        }
        return projects;
    }

    // 内部类：项目统计信息
    public static class ProjectStats {
        private long total;
        private long active;
        private long completed;
        private long archived;
        private long high;
        private long critical;
        
        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getActive() { return active; }
        public void setActive(long active) { this.active = active; }
        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }
        public long getArchived() { return archived; }
        public void setArchived(long archived) { this.archived = archived; }
        public long getHigh() { return high; }
        public void setHigh(long high) { this.high = high; }
        public long getCritical() { return critical; }
        public void setCritical(long critical) { this.critical = critical; }
    }
}
