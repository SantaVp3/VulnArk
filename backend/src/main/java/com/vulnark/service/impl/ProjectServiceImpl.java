package com.vulnark.service.impl;

import com.vulnark.entity.Project;
import com.vulnark.entity.User;
import com.vulnark.exception.BusinessException;
import com.vulnark.repository.ProjectRepository;
import com.vulnark.repository.UserRepository;
import com.vulnark.repository.AssetRepository;
import com.vulnark.repository.VulnerabilityRepository;
import com.vulnark.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    @Override
    public Project createProject(Project project, Long currentUserId) {
        // 设置创建者为项目负责人
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        project.setOwner(owner);
        
        // 验证项目名称不重复
        List<Project> existingProjects = projectRepository.findByNameContainingIgnoreCase(project.getName());
        for (Project existing : existingProjects) {
            if (existing.getName().equalsIgnoreCase(project.getName())) {
                throw new BusinessException("项目名称已存在");
            }
        }
        
        // 初始化成员列表（包含负责人）
        if (project.getMembers() == null) {
            project.setMembers(new ArrayList<>());
        }
        if (!project.getMembers().contains(owner)) {
            project.getMembers().add(owner);
        }
        
        return projectRepository.save(project);
    }
    
    @Override
    public Project updateProject(Long projectId, Project project, Long currentUserId) {
        Project existingProject = getProjectById(projectId);
        
        // 检查权限（只有项目负责人和管理员可以修改）
        if (!existingProject.getOwner().getId().equals(currentUserId)) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            if (!currentUser.getRole().equals(User.Role.ADMIN)) {
                throw new BusinessException("没有权限修改此项目");
            }
        }
        
        // 更新项目信息
        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setStatus(project.getStatus());
        existingProject.setPriority(project.getPriority());
        existingProject.setDepartment(project.getDepartment());
        existingProject.setStartDate(project.getStartDate());
        existingProject.setEndDate(project.getEndDate());
        existingProject.setBudget(project.getBudget());
        existingProject.setTags(project.getTags());
        
        return projectRepository.save(existingProject);
    }
    
    @Override
    public void deleteProject(Long projectId, Long currentUserId) {
        Project project = getProjectById(projectId);
        
        // 检查权限（只有项目负责人和管理员可以删除）
        if (!project.getOwner().getId().equals(currentUserId)) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            if (!currentUser.getRole().equals(User.Role.ADMIN)) {
                throw new BusinessException("没有权限删除此项目");
            }
        }
        
        // 检查是否有关联的资产或漏洞
        long assetCount = assetRepository.countByProjectId(projectId);
        long vulnerabilityCount = vulnerabilityRepository.countByProjectIdAndDeletedFalse(projectId);
        
        if (assetCount > 0 || vulnerabilityCount > 0) {
            throw new BusinessException("项目下还有关联的资产或漏洞，无法删除");
        }
        
        projectRepository.delete(project);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("项目不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Project> getProjects(String name, Project.Status status, Project.Priority priority,
                                   Long ownerId, String department, String keyword, 
                                   Pageable pageable) {
        return projectRepository.findProjectsWithFilters(
                name, status, priority, ownerId, department, keyword, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsByStatus(Project.Status status) {
        return projectRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsByOwner(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsByMember(Long memberId) {
        return projectRepository.findByMemberId(memberId);
    }
    
    @Override
    public Project addProjectMember(Long projectId, Long memberId, Long currentUserId) {
        Project project = getProjectById(projectId);
        
        // 检查权限（只有项目负责人和管理员可以添加成员）
        if (!project.getOwner().getId().equals(currentUserId)) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            if (!currentUser.getRole().equals(User.Role.ADMIN)) {
                throw new BusinessException("没有权限管理此项目成员");
            }
        }
        
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (project.getMembers() == null) {
            project.setMembers(new ArrayList<>());
        }
        
        if (!project.getMembers().contains(member)) {
            project.getMembers().add(member);
        }
        
        return projectRepository.save(project);
    }
    
    @Override
    public Project removeProjectMember(Long projectId, Long memberId, Long currentUserId) {
        Project project = getProjectById(projectId);
        
        // 检查权限（只有项目负责人和管理员可以移除成员）
        if (!project.getOwner().getId().equals(currentUserId)) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            if (!currentUser.getRole().equals(User.Role.ADMIN)) {
                throw new BusinessException("没有权限管理此项目成员");
            }
        }
        
        // 不能移除项目负责人
        if (project.getOwner().getId().equals(memberId)) {
            throw new BusinessException("不能移除项目负责人");
        }
        
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (project.getMembers() != null) {
            project.getMembers().remove(member);
        }
        
        return projectRepository.save(project);
    }
    
    @Override
    public Project updateProjectStatus(Long projectId, Project.Status status, Long currentUserId) {
        Project project = getProjectById(projectId);
        
        // 检查权限（只有项目负责人和管理员可以更新状态）
        if (!project.getOwner().getId().equals(currentUserId)) {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            if (!currentUser.getRole().equals(User.Role.ADMIN)) {
                throw new BusinessException("没有权限修改此项目状态");
            }
        }
        
        project.setStatus(status);
        return projectRepository.save(project);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProjectStats getProjectStats() {
        ProjectStats stats = new ProjectStats();
        
        List<Object[]> statusCounts = projectRepository.countByStatus();
        
        stats.setTotalProjects(projectRepository.count());
        
        for (Object[] row : statusCounts) {
            Project.Status status = (Project.Status) row[0];
            Long count = (Long) row[1];
            
            switch (status) {
                case ACTIVE:
                    stats.setActiveProjects(count);
                    break;
                case COMPLETED:
                    stats.setCompletedProjects(count);
                    break;
                case ON_HOLD:
                    stats.setOnHoldProjects(count);
                    break;
                case CANCELLED:
                    stats.setCancelledProjects(count);
                    break;
                case PLANNING:
                    stats.setPlanningProjects(count);
                    break;
            }
        }
        
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getProjectsEndingSoon(int days) {
        LocalDateTime endDate = LocalDateTime.now().plusDays(days);
        return projectRepository.findProjectsEndingBefore(endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Project> getRecentProjects(int limit) {
        return projectRepository.findTop10ByOrderByCreatedTimeDesc();
    }
} 