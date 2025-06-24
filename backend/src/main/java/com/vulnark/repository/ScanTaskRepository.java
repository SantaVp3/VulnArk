package com.vulnark.repository;

import com.vulnark.entity.ScanTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScanTaskRepository extends JpaRepository<ScanTask, Long> {
    
    // 查找未删除的扫描任务
    List<ScanTask> findByDeletedFalse();
    
    // 分页查找未删除的扫描任务
    Page<ScanTask> findByDeletedFalse(Pageable pageable);
    
    // 根据ID查找未删除的扫描任务
    Optional<ScanTask> findByIdAndDeletedFalse(Long id);
    
    // 根据状态查找扫描任务
    List<ScanTask> findByStatusAndDeletedFalse(ScanTask.TaskStatus status);

    // 根据类型查找扫描任务
    List<ScanTask> findByScanTypeAndDeletedFalse(ScanTask.ScanType scanType);

    // 根据扫描引擎查找扫描任务
    List<ScanTask> findByScanEngineTypeAndDeletedFalse(ScanTask.ScanEngine scanEngineType);

    // 根据创建者查找扫描任务
    List<ScanTask> findByCreatedByIdAndDeletedFalse(Long createdById);
    Page<ScanTask> findByCreatedByIdAndDeletedFalse(Long createdById, Pageable pageable);

    // 根据项目查找扫描任务
    List<ScanTask> findByProjectIdAndDeletedFalse(Long projectId);
    Page<ScanTask> findByProjectIdAndDeletedFalse(Long projectId, Pageable pageable);

    // 查询正在运行的任务
    @Query("SELECT st FROM ScanTask st WHERE st.status IN ('RUNNING', 'QUEUED') AND st.deleted = false")
    List<ScanTask> findRunningTasks();

    // 查询可以启动的任务
    @Query("SELECT st FROM ScanTask st WHERE st.status IN ('CREATED', 'QUEUED') AND st.deleted = false " +
           "AND (st.scheduledStartTime IS NULL OR st.scheduledStartTime <= :now)")
    List<ScanTask> findTasksReadyToStart(@Param("now") LocalDateTime now);

    // 查询超时的任务
    @Query("SELECT st FROM ScanTask st WHERE st.status = 'RUNNING' AND st.deleted = false " +
           "AND st.actualStartTime < :timeoutThreshold")
    List<ScanTask> findTimeoutTasks(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    // 根据外部任务ID查询
    Optional<ScanTask> findByExternalTaskIdAndDeletedFalse(String externalTaskId);

    // 查询最近的扫描任务
    @Query("SELECT st FROM ScanTask st WHERE st.deleted = false " +
           "ORDER BY st.createdTime DESC")
    List<ScanTask> findRecentTasks(Pageable pageable);

    // 根据时间范围查询
    @Query("SELECT st FROM ScanTask st WHERE st.createdTime BETWEEN :startTime AND :endTime " +
           "AND st.deleted = false")
    List<ScanTask> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    // 统计查询
    @Query("SELECT COUNT(st) FROM ScanTask st WHERE st.deleted = false")
    long countAllTasks();

    @Query("SELECT COUNT(st) FROM ScanTask st WHERE st.status = :status AND st.deleted = false")
    long countByStatus(@Param("status") ScanTask.TaskStatus status);

    @Query("SELECT COUNT(st) FROM ScanTask st WHERE st.scanType = :scanType AND st.deleted = false")
    long countByScanType(@Param("scanType") ScanTask.ScanType scanType);

    @Query("SELECT COUNT(st) FROM ScanTask st WHERE st.project.id = :projectId AND st.deleted = false")
    long countByProjectId(@Param("projectId") Long projectId);

    // 统计各状态的任务数量
    @Query("SELECT st.status, COUNT(st) FROM ScanTask st WHERE st.deleted = false GROUP BY st.status")
    List<Object[]> getStatusStatistics();

    // 统计各扫描类型的任务数量
    @Query("SELECT st.scanType, COUNT(st) FROM ScanTask st WHERE st.deleted = false GROUP BY st.scanType")
    List<Object[]> getScanTypeStatistics();

    // 统计各扫描引擎的任务数量
    @Query("SELECT st.scanEngineType, COUNT(st) FROM ScanTask st WHERE st.deleted = false GROUP BY st.scanEngineType")
    List<Object[]> getScanEngineStatistics();

    // 查询项目的扫描任务统计
    @Query("SELECT st.status, COUNT(st) FROM ScanTask st WHERE st.project.id = :projectId " +
           "AND st.deleted = false GROUP BY st.status")
    List<Object[]> getProjectTaskStatistics(@Param("projectId") Long projectId);

    // 查询用户的扫描任务统计
    @Query("SELECT st.status, COUNT(st) FROM ScanTask st WHERE st.createdBy.id = :userId " +
           "AND st.deleted = false GROUP BY st.status")
    List<Object[]> getUserTaskStatistics(@Param("userId") Long userId);

    // 查询平均扫描时间
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, st.actualStartTime, st.completedTime)) " +
           "FROM ScanTask st WHERE st.status = 'COMPLETED' AND st.actualStartTime IS NOT NULL " +
           "AND st.completedTime IS NOT NULL AND st.deleted = false")
    Double getAverageScanDuration();

    // 查询成功率
    @Query("SELECT " +
           "(SELECT COUNT(st1) FROM ScanTask st1 WHERE st1.status = 'COMPLETED' AND st1.deleted = false) * 100.0 / " +
           "(SELECT COUNT(st2) FROM ScanTask st2 WHERE st2.status IN ('COMPLETED', 'FAILED', 'TIMEOUT') AND st2.deleted = false)")
    Double getSuccessRate();

    // 根据名称模糊查询
    @Query("SELECT st FROM ScanTask st WHERE st.name LIKE %:name% AND st.deleted = false")
    List<ScanTask> findByNameContaining(@Param("name") String name);

    // 复合查询
    @Query("SELECT st FROM ScanTask st WHERE " +
           "(:name IS NULL OR st.name LIKE %:name%) AND " +
           "(:status IS NULL OR st.status = :status) AND " +
           "(:scanType IS NULL OR st.scanType = :scanType) AND " +
           "(:scanEngine IS NULL OR st.scanEngineType = :scanEngine) AND " +
           "(:projectId IS NULL OR st.project.id = :projectId) AND " +
           "(:createdById IS NULL OR st.createdBy.id = :createdById) AND " +
           "st.deleted = false")
    Page<ScanTask> findByConditions(@Param("name") String name,
                                   @Param("status") ScanTask.TaskStatus status,
                                   @Param("scanType") ScanTask.ScanType scanType,
                                   @Param("scanEngine") ScanTask.ScanEngine scanEngine,
                                   @Param("projectId") Long projectId,
                                   @Param("createdById") Long createdById,
                                   Pageable pageable);

    // 删除过期任务
    @Query("UPDATE ScanTask st SET st.deleted = true WHERE st.createdTime < :cutoffTime " +
           "AND st.status IN ('COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT')")
    int deleteOldTasks(@Param("cutoffTime") LocalDateTime cutoffTime);

    // 查询需要清理的任务
    @Query("SELECT st FROM ScanTask st WHERE st.createdTime < :cutoffTime " +
           "AND st.status IN ('COMPLETED', 'FAILED', 'CANCELLED', 'TIMEOUT') AND st.deleted = false")
    List<ScanTask> findTasksToCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // 获取待执行的任务
    @Query("SELECT s FROM ScanTask s WHERE s.deleted = false " +
           "AND s.status = 'CREATED' " +
           "AND (s.scheduledStartTime IS NULL OR s.scheduledStartTime <= :now) " +
           "ORDER BY s.createdTime ASC")
    List<ScanTask> findPendingTasks(@Param("now") LocalDateTime now);
    
    // 获取失败的任务
    @Query("SELECT s FROM ScanTask s WHERE s.deleted = false " +
           "AND s.status = 'FAILED' " +
           "ORDER BY s.updatedTime DESC")
    List<ScanTask> findFailedTasks();

    // 获取今日扫描任务
    @Query("SELECT s FROM ScanTask s WHERE s.deleted = false " +
           "AND DATE(s.createdTime) = CURRENT_DATE " +
           "ORDER BY s.createdTime DESC")
    List<ScanTask> findTodayTasks();

    // 获取本周扫描任务
    @Query("SELECT s FROM ScanTask s WHERE s.deleted = false " +
           "AND YEARWEEK(s.createdTime) = YEARWEEK(NOW()) " +
           "ORDER BY s.createdTime DESC")
    List<ScanTask> findThisWeekTasks();
}
