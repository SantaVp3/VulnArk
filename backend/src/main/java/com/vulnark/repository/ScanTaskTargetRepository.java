package com.vulnark.repository;

import com.vulnark.entity.ScanTaskTarget;
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
public interface ScanTaskTargetRepository extends JpaRepository<ScanTaskTarget, Long> {
    
    // 根据扫描任务查询目标
    List<ScanTaskTarget> findByScanTaskId(Long scanTaskId);
    Page<ScanTaskTarget> findByScanTaskId(Long scanTaskId, Pageable pageable);
    
    // 根据资产查询扫描记录
    List<ScanTaskTarget> findByAssetId(Long assetId);
    Page<ScanTaskTarget> findByAssetId(Long assetId, Pageable pageable);
    
    // 根据状态查询
    List<ScanTaskTarget> findByStatus(ScanTaskTarget.ScanStatus status);
    
    // 根据扫描任务和资产查询
    Optional<ScanTaskTarget> findByScanTaskIdAndAssetId(Long scanTaskId, Long assetId);
    
    // 查询扫描任务的目标统计
    @Query("SELECT COUNT(stt) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId")
    long countByScanTaskId(@Param("scanTaskId") Long scanTaskId);
    
    @Query("SELECT COUNT(stt) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId AND stt.status = :status")
    long countByScanTaskIdAndStatus(@Param("scanTaskId") Long scanTaskId, 
                                   @Param("status") ScanTaskTarget.ScanStatus status);
    
    // 查询正在扫描的目标
    @Query("SELECT stt FROM ScanTaskTarget stt WHERE stt.status = 'RUNNING'")
    List<ScanTaskTarget> findRunningTargets();
    
    // 查询已完成的目标
    @Query("SELECT stt FROM ScanTaskTarget stt WHERE stt.status = 'COMPLETED' " +
           "ORDER BY stt.completedTime DESC")
    List<ScanTaskTarget> findCompletedTargets(Pageable pageable);
    
    // 查询失败的目标
    @Query("SELECT stt FROM ScanTaskTarget stt WHERE stt.status = 'FAILED' " +
           "ORDER BY stt.completedTime DESC")
    List<ScanTaskTarget> findFailedTargets(Pageable pageable);
    
    // 统计扫描任务的漏洞数量
    @Query("SELECT SUM(stt.vulnerabilityCount) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId")
    Long sumVulnerabilityCountByScanTaskId(@Param("scanTaskId") Long scanTaskId);
    
    @Query("SELECT SUM(stt.highRiskCount) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId")
    Long sumHighRiskCountByScanTaskId(@Param("scanTaskId") Long scanTaskId);
    
    @Query("SELECT SUM(stt.mediumRiskCount) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId")
    Long sumMediumRiskCountByScanTaskId(@Param("scanTaskId") Long scanTaskId);
    
    @Query("SELECT SUM(stt.lowRiskCount) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId")
    Long sumLowRiskCountByScanTaskId(@Param("scanTaskId") Long scanTaskId);
    
    @Query("SELECT SUM(stt.infoRiskCount) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId")
    Long sumInfoRiskCountByScanTaskId(@Param("scanTaskId") Long scanTaskId);
    
    // 查询资产的最新扫描记录
    @Query("SELECT stt FROM ScanTaskTarget stt WHERE stt.asset.id = :assetId " +
           "ORDER BY stt.createdTime DESC")
    List<ScanTaskTarget> findLatestByAssetId(@Param("assetId") Long assetId, Pageable pageable);
    
    // 查询扫描任务的进度统计
    @Query("SELECT AVG(stt.progress) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId")
    Double getAverageProgressByScanTaskId(@Param("scanTaskId") Long scanTaskId);
    
    // 查询状态分布统计
    @Query("SELECT stt.status, COUNT(stt) FROM ScanTaskTarget stt WHERE stt.scanTask.id = :scanTaskId GROUP BY stt.status")
    List<Object[]> getStatusStatisticsByScanTaskId(@Param("scanTaskId") Long scanTaskId);
    
    // 查询时间范围内的扫描记录
    @Query("SELECT stt FROM ScanTaskTarget stt WHERE stt.createdTime BETWEEN :startTime AND :endTime")
    List<ScanTaskTarget> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);
    
    // 删除扫描任务的所有目标
    void deleteByScanTaskId(Long scanTaskId);
    
    // 查询需要重试的目标
    @Query("SELECT stt FROM ScanTaskTarget stt WHERE stt.status = 'FAILED' " +
           "AND stt.errorMessage NOT LIKE '%timeout%' " +
           "AND stt.completedTime > :since")
    List<ScanTaskTarget> findRetryableTargets(@Param("since") LocalDateTime since);
}
