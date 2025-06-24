package com.vulnark.repository;

import com.vulnark.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // 查找未删除的项目
    List<Project> findByDeletedFalse();
    
    // 分页查找未删除的项目
    Page<Project> findByDeletedFalse(Pageable pageable);
    
    // 根据ID查找未删除的项目
    Optional<Project> findByIdAndDeletedFalse(Long id);
    
    // 根据负责人ID查找项目
    List<Project> findByOwnerIdAndDeletedFalse(Long ownerId);
    
    // 根据状态查找项目
    List<Project> findByStatusAndDeletedFalse(Project.Status status);
    
    // 根据优先级查找项目
    List<Project> findByPriorityAndDeletedFalse(Project.Priority priority);
    
    // 根据项目类型查找项目
    List<Project> findByTypeAndDeletedFalse(String type);
    
    // 复合查询：根据多个条件查找项目
    @Query("SELECT p FROM Project p WHERE p.deleted = false " +
           "AND (:name IS NULL OR p.name LIKE %:name%) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:priority IS NULL OR p.priority = :priority) " +
           "AND (:type IS NULL OR p.type LIKE %:type%) " +
           "AND (:ownerId IS NULL OR p.ownerId = :ownerId) " +
           "AND (:startDate IS NULL OR p.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR p.endDate <= :endDate)")
    Page<Project> findByConditions(
            @Param("name") String name,
            @Param("status") Project.Status status,
            @Param("priority") Project.Priority priority,
            @Param("type") String type,
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    // 统计查询
    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false")
    long countByDeletedFalse();
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false AND p.status = :status")
    long countByStatusAndDeletedFalse(@Param("status") Project.Status status);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false AND p.priority = :priority")
    long countByPriorityAndDeletedFalse(@Param("priority") Project.Priority priority);
    
    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false AND p.ownerId = :ownerId")
    long countByOwnerIdAndDeletedFalse(@Param("ownerId") Long ownerId);
    
    // 获取最近的项目
    @Query("SELECT p FROM Project p WHERE p.deleted = false ORDER BY p.createdTime DESC")
    List<Project> findRecentProjects(Pageable pageable);
    
    // 获取即将到期的项目
    @Query("SELECT p FROM Project p WHERE p.deleted = false " +
           "AND p.endDate IS NOT NULL " +
           "AND p.endDate <= :date " +
           "AND p.status NOT IN ('COMPLETED', 'ARCHIVED')")
    List<Project> findOverdueProjects(@Param("date") LocalDate date);
    
    // 获取活跃项目
    @Query("SELECT p FROM Project p WHERE p.deleted = false " +
           "AND p.status = 'ACTIVE'")
    List<Project> findActiveProjects();
    
    // 根据项目名称查找项目（精确匹配）
    Optional<Project> findByNameAndDeletedFalse(String name);
    
    // 全文搜索
    @Query("SELECT p FROM Project p WHERE p.deleted = false " +
           "AND (p.name LIKE %:keyword% " +
           "OR p.description LIKE %:keyword% " +
           "OR p.type LIKE %:keyword% " +
           "OR p.tags LIKE %:keyword%)")
    Page<Project> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 根据标签搜索项目
    @Query("SELECT p FROM Project p WHERE p.deleted = false " +
           "AND p.tags LIKE %:tag%")
    List<Project> findByTagsContaining(@Param("tag") String tag);
    
    // 获取项目统计信息
    @Query("SELECT p.status, COUNT(p) FROM Project p WHERE p.deleted = false GROUP BY p.status")
    List<Object[]> getProjectStatusStatistics();
    
    @Query("SELECT p.priority, COUNT(p) FROM Project p WHERE p.deleted = false GROUP BY p.priority")
    List<Object[]> getProjectPriorityStatistics();
    
    @Query("SELECT p.type, COUNT(p) FROM Project p WHERE p.deleted = false AND p.type IS NOT NULL GROUP BY p.type")
    List<Object[]> getProjectTypeStatistics();

    // 仪表盘相关查询
    // 按状态统计项目数量（字符串参数）
    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false AND p.status = :status")
    long countByStatusStringAndDeletedFalse(@Param("status") String status);

    // 统计过期项目数量
    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false " +
           "AND p.endDate IS NOT NULL AND p.endDate < CURRENT_DATE " +
           "AND p.status NOT IN ('COMPLETED', 'ARCHIVED')")
    long countOverdueProjects();
}
