package com.vulnark.repository;

import com.vulnark.entity.Project;
import com.vulnark.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * 根据项目名称查找项目
     */
    List<Project> findByNameContainingIgnoreCase(String name);
    
    /**
     * 根据状态查找项目
     */
    List<Project> findByStatus(Project.Status status);
    
    /**
     * 根据优先级查找项目
     */
    List<Project> findByPriority(Project.Priority priority);
    
    /**
     * 根据负责人查找项目
     */
    List<Project> findByOwner(User owner);
    
    /**
     * 根据负责人ID查找项目
     */
    List<Project> findByOwnerId(Long ownerId);
    
    /**
     * 根据部门查找项目
     */
    List<Project> findByDepartmentContainingIgnoreCase(String department);
    
    /**
     * 查找用户参与的项目
     */
    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findByMemberId(@Param("userId") Long userId);
    
    /**
     * 复杂查询方法
     */
    @Query("SELECT p FROM Project p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:priority IS NULL OR p.priority = :priority) AND " +
           "(:ownerId IS NULL OR p.owner.id = :ownerId) AND " +
           "(:department IS NULL OR LOWER(p.department) LIKE LOWER(CONCAT('%', :department, '%'))) AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Project> findProjectsWithFilters(
            @Param("name") String name,
            @Param("status") Project.Status status,
            @Param("priority") Project.Priority priority,
            @Param("ownerId") Long ownerId,
            @Param("department") String department,
            @Param("keyword") String keyword,
            Pageable pageable);
    
    /**
     * 统计不同状态的项目数量
     */
    @Query("SELECT p.status, COUNT(p) FROM Project p GROUP BY p.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计不同优先级的项目数量
     */
    @Query("SELECT p.priority, COUNT(p) FROM Project p GROUP BY p.priority")
    List<Object[]> countByPriority();
    
    /**
     * 根据时间范围查找项目
     */
    List<Project> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找即将到期的项目
     */
    @Query("SELECT p FROM Project p WHERE p.endDate IS NOT NULL AND p.endDate <= :date AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Project> findProjectsEndingBefore(@Param("date") LocalDateTime date);
    
    /**
     * 查找最近创建的项目
     */
    List<Project> findTop10ByOrderByCreatedTimeDesc();
    
    /**
     * 查找活跃的项目
     */
    List<Project> findByStatusIn(List<Project.Status> statuses);
    
    /**
     * 统计项目总数
     */
    @Query("SELECT COUNT(p) FROM Project p")
    long countAllProjects();
    
    /**
     * 统计活跃项目数
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 'ACTIVE'")
    long countActiveProjects();
    
    /**
     * 统计已完成项目数
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 'COMPLETED'")
    long countCompletedProjects();
} 