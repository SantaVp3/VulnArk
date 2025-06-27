package com.vulnark.repository;

import com.vulnark.entity.AssetDiscoveryTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 资产发现任务Repository
 */
@Repository
public interface AssetDiscoveryTaskRepository extends JpaRepository<AssetDiscoveryTask, Long> {

    /**
     * 根据ID查找未删除的任务
     */
    Optional<AssetDiscoveryTask> findByIdAndDeletedFalse(Long id);

    /**
     * 查找所有未删除的任务
     */
    List<AssetDiscoveryTask> findByDeletedFalseOrderByCreatedTimeDesc();

    /**
     * 分页查询未删除的任务
     */
    Page<AssetDiscoveryTask> findByDeletedFalse(Pageable pageable);

    /**
     * 根据状态查找任务
     */
    List<AssetDiscoveryTask> findByStatusAndDeletedFalse(AssetDiscoveryTask.TaskStatus status);

    /**
     * 根据创建者查找任务
     */
    List<AssetDiscoveryTask> findByCreatedByAndDeletedFalse(Long createdBy);

    /**
     * 查找需要执行的定时任务
     */
    @Query("SELECT t FROM AssetDiscoveryTask t WHERE t.deleted = false " +
           "AND t.scheduleType != 'ONCE' " +
           "AND t.status IN ('PENDING', 'COMPLETED') " +
           "AND t.nextRunTime <= :currentTime")
    List<AssetDiscoveryTask> findScheduledTasks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找正在运行的任务
     */
    List<AssetDiscoveryTask> findByStatusAndDeletedFalse(AssetDiscoveryTask.TaskStatus status, Pageable pageable);

    /**
     * 根据名称模糊查询
     */
    @Query("SELECT t FROM AssetDiscoveryTask t WHERE t.deleted = false " +
           "AND (:name IS NULL OR t.name LIKE %:name%) " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:scanType IS NULL OR t.scanType = :scanType) " +
           "AND (:createdBy IS NULL OR t.createdBy = :createdBy)")
    Page<AssetDiscoveryTask> findTasksWithFilters(
            @Param("name") String name,
            @Param("status") AssetDiscoveryTask.TaskStatus status,
            @Param("scanType") AssetDiscoveryTask.ScanType scanType,
            @Param("createdBy") Long createdBy,
            Pageable pageable);

    /**
     * 统计任务数量按状态
     */
    @Query("SELECT t.status, COUNT(t) FROM AssetDiscoveryTask t WHERE t.deleted = false GROUP BY t.status")
    List<Object[]> countTasksByStatus();

    /**
     * 统计最近创建的任务数量
     */
    @Query("SELECT COUNT(t) FROM AssetDiscoveryTask t WHERE t.deleted = false " +
           "AND t.createdTime >= :startTime")
    long countRecentTasks(@Param("startTime") LocalDateTime startTime);

    /**
     * 查找最近完成的任务
     */
    @Query("SELECT t FROM AssetDiscoveryTask t WHERE t.deleted = false " +
           "AND t.status = 'COMPLETED' " +
           "ORDER BY t.lastRunTime DESC")
    List<AssetDiscoveryTask> findRecentCompletedTasks(Pageable pageable);

    /**
     * 查找长时间运行的任务
     */
    @Query("SELECT t FROM AssetDiscoveryTask t WHERE t.deleted = false " +
           "AND t.status = 'RUNNING' " +
           "AND t.lastRunTime < :timeThreshold")
    List<AssetDiscoveryTask> findLongRunningTasks(@Param("timeThreshold") LocalDateTime timeThreshold);

    /**
     * 更新任务状态
     */
    @Query("UPDATE AssetDiscoveryTask t SET t.status = :status, t.updatedTime = :updateTime " +
           "WHERE t.id = :taskId")
    void updateTaskStatus(@Param("taskId") Long taskId, 
                         @Param("status") AssetDiscoveryTask.TaskStatus status,
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新任务进度
     */
    @Query("UPDATE AssetDiscoveryTask t SET t.progress = :progress, t.updatedTime = :updateTime " +
           "WHERE t.id = :taskId")
    void updateTaskProgress(@Param("taskId") Long taskId, 
                           @Param("progress") java.math.BigDecimal progress,
                           @Param("updateTime") LocalDateTime updateTime);

    /**
     * 检查任务名称是否存在
     */
    boolean existsByNameAndDeletedFalse(String name);

    /**
     * 根据扫描类型统计任务
     */
    @Query("SELECT t.scanType, COUNT(t) FROM AssetDiscoveryTask t WHERE t.deleted = false GROUP BY t.scanType")
    List<Object[]> countTasksByScanType();

    /**
     * 查找即将到期的任务
     */
    @Query("SELECT t FROM AssetDiscoveryTask t WHERE t.deleted = false " +
           "AND t.scheduleType != 'ONCE' " +
           "AND t.nextRunTime BETWEEN :startTime AND :endTime")
    List<AssetDiscoveryTask> findUpcomingTasks(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);
}
