package com.vulnark.repository;

import com.vulnark.entity.AssetDetection;
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
public interface AssetDetectionRepository extends JpaRepository<AssetDetection, Long> {
    
    // 根据资产ID查询检测记录
    List<AssetDetection> findByAssetIdOrderByCreatedTimeDesc(Long assetId);
    
    // 根据资产ID和检测类型查询最新记录
    Optional<AssetDetection> findFirstByAssetIdAndTypeOrderByCreatedTimeDesc(
            Long assetId, AssetDetection.DetectionType type);
    
    // 根据资产ID查询最新检测记录
    Optional<AssetDetection> findFirstByAssetIdOrderByCreatedTimeDesc(Long assetId);
    
    // 分页查询检测记录
    Page<AssetDetection> findByAssetId(Long assetId, Pageable pageable);
    
    // 根据检测状态查询
    List<AssetDetection> findByStatus(AssetDetection.DetectionStatus status);
    
    // 根据检测结果查询
    List<AssetDetection> findByResult(AssetDetection.DetectionResult result);
    
    // 根据检测类型查询
    List<AssetDetection> findByType(AssetDetection.DetectionType type);
    
    // 查询指定时间范围内的检测记录
    @Query("SELECT ad FROM AssetDetection ad WHERE ad.createdTime BETWEEN :startTime AND :endTime")
    List<AssetDetection> findByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime);
    
    // 查询正在进行的检测
    @Query("SELECT ad FROM AssetDetection ad WHERE ad.status IN ('PENDING', 'RUNNING')")
    List<AssetDetection> findRunningDetections();
    
    // 查询超时的检测（超过指定时间仍未完成）
    @Query("SELECT ad FROM AssetDetection ad WHERE ad.status IN ('PENDING', 'RUNNING') " +
           "AND ad.startTime < :timeoutThreshold")
    List<AssetDetection> findTimeoutDetections(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);
    
    // 统计资产的检测次数
    @Query("SELECT COUNT(ad) FROM AssetDetection ad WHERE ad.assetId = :assetId")
    long countByAssetId(@Param("assetId") Long assetId);
    
    // 统计在线资产数量
    @Query("SELECT COUNT(DISTINCT ad.assetId) FROM AssetDetection ad " +
           "WHERE ad.result = 'ONLINE' AND ad.id IN (" +
           "SELECT MAX(ad2.id) FROM AssetDetection ad2 GROUP BY ad2.assetId)")
    long countOnlineAssets();
    
    // 统计离线资产数量
    @Query("SELECT COUNT(DISTINCT ad.assetId) FROM AssetDetection ad " +
           "WHERE ad.result = 'OFFLINE' AND ad.id IN (" +
           "SELECT MAX(ad2.id) FROM AssetDetection ad2 GROUP BY ad2.assetId)")
    long countOfflineAssets();
    
    // 查询资产的最新检测状态
    @Query("SELECT ad FROM AssetDetection ad WHERE ad.id IN (" +
           "SELECT MAX(ad2.id) FROM AssetDetection ad2 WHERE ad2.assetId = :assetId GROUP BY ad2.type)")
    List<AssetDetection> findLatestDetectionsByAssetId(@Param("assetId") Long assetId);
    
    // 查询所有资产的最新检测状态
    @Query("SELECT ad FROM AssetDetection ad WHERE ad.id IN (" +
           "SELECT MAX(ad2.id) FROM AssetDetection ad2 GROUP BY ad2.assetId, ad2.type)")
    List<AssetDetection> findAllLatestDetections();
    
    // 根据目标地址查询检测记录
    List<AssetDetection> findByTargetContainingIgnoreCase(String target);
    
    // 根据端口查询检测记录
    List<AssetDetection> findByPort(Integer port);
    
    // 查询成功的检测记录
    @Query("SELECT ad FROM AssetDetection ad WHERE ad.status = 'COMPLETED' AND ad.result = 'ONLINE'")
    List<AssetDetection> findSuccessfulDetections();
    
    // 查询失败的检测记录
    @Query("SELECT ad FROM AssetDetection ad WHERE ad.status IN ('FAILED', 'TIMEOUT') " +
           "OR (ad.status = 'COMPLETED' AND ad.result IN ('OFFLINE', 'UNREACHABLE'))")
    List<AssetDetection> findFailedDetections();
    
    // 删除指定时间之前的检测记录（用于清理历史数据）
    @Query("DELETE FROM AssetDetection ad WHERE ad.createdTime < :cutoffTime")
    void deleteOldDetections(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // 查询平均响应时间
    @Query("SELECT AVG(ad.responseTime) FROM AssetDetection ad WHERE ad.responseTime IS NOT NULL " +
           "AND ad.result = 'ONLINE' AND ad.createdTime >= :since")
    Double getAverageResponseTime(@Param("since") LocalDateTime since);
    
    // 查询检测统计信息
    @Query("SELECT ad.result, COUNT(ad) FROM AssetDetection ad " +
           "WHERE ad.createdTime >= :since GROUP BY ad.result")
    List<Object[]> getDetectionStatistics(@Param("since") LocalDateTime since);
    
    // 查询资产检测历史趋势
    @Query("SELECT DATE(ad.createdTime), ad.result, COUNT(ad) FROM AssetDetection ad " +
           "WHERE ad.assetId = :assetId AND ad.createdTime >= :since " +
           "GROUP BY DATE(ad.createdTime), ad.result ORDER BY DATE(ad.createdTime)")
    List<Object[]> getAssetDetectionTrend(@Param("assetId") Long assetId, 
                                         @Param("since") LocalDateTime since);
}
