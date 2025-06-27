package com.vulnark.repository;

import com.vulnark.entity.AssetDiscoveryResult;
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
 * 资产发现结果Repository
 */
@Repository
public interface AssetDiscoveryResultRepository extends JpaRepository<AssetDiscoveryResult, Long> {

    /**
     * 根据任务ID查找结果
     */
    List<AssetDiscoveryResult> findByTaskIdOrderByDiscoveredTimeDesc(Long taskId);

    /**
     * 分页查询任务结果
     */
    Page<AssetDiscoveryResult> findByTaskId(Long taskId, Pageable pageable);

    /**
     * 根据IP地址查找结果
     */
    List<AssetDiscoveryResult> findByIpAddressOrderByDiscoveredTimeDesc(String ipAddress);

    /**
     * 根据关联状态查找结果
     */
    List<AssetDiscoveryResult> findByCorrelationStatus(AssetDiscoveryResult.CorrelationStatus status);

    /**
     * 查找在线的主机
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.taskId = :taskId AND r.isAlive = true")
    List<AssetDiscoveryResult> findAliveHostsByTask(@Param("taskId") Long taskId);

    /**
     * 查找新发现的资产
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.correlationStatus = 'NEW' " +
           "ORDER BY r.discoveredTime DESC")
    List<AssetDiscoveryResult> findNewDiscoveredAssets(Pageable pageable);

    /**
     * 根据时间范围查找结果
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.discoveredTime BETWEEN :startTime AND :endTime " +
           "ORDER BY r.discoveredTime DESC")
    List<AssetDiscoveryResult> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 统计任务发现的资产数量
     */
    @Query("SELECT COUNT(r) FROM AssetDiscoveryResult r WHERE r.taskId = :taskId")
    long countByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计在线主机数量
     */
    @Query("SELECT COUNT(r) FROM AssetDiscoveryResult r WHERE r.taskId = :taskId AND r.isAlive = true")
    long countAliveHostsByTask(@Param("taskId") Long taskId);

    /**
     * 根据关联状态统计
     */
    @Query("SELECT r.correlationStatus, COUNT(r) FROM AssetDiscoveryResult r " +
           "WHERE r.taskId = :taskId GROUP BY r.correlationStatus")
    List<Object[]> countByCorrelationStatusAndTask(@Param("taskId") Long taskId);

    /**
     * 查找重复的IP地址
     */
    @Query("SELECT r.ipAddress, COUNT(r) FROM AssetDiscoveryResult r " +
           "WHERE r.taskId = :taskId GROUP BY r.ipAddress HAVING COUNT(r) > 1")
    List<Object[]> findDuplicateIpAddresses(@Param("taskId") Long taskId);

    /**
     * 查找具有开放端口的主机
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.taskId = :taskId " +
           "AND r.openPorts IS NOT NULL AND r.openPorts != '[]'")
    List<AssetDiscoveryResult> findHostsWithOpenPorts(@Param("taskId") Long taskId);

    /**
     * 根据操作系统统计
     */
    @Query("SELECT r.operatingSystem, COUNT(r) FROM AssetDiscoveryResult r " +
           "WHERE r.taskId = :taskId AND r.operatingSystem IS NOT NULL " +
           "GROUP BY r.operatingSystem")
    List<Object[]> countByOperatingSystem(@Param("taskId") Long taskId);

    /**
     * 查找高置信度的结果
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.taskId = :taskId " +
           "AND r.confidenceScore >= :minConfidence ORDER BY r.confidenceScore DESC")
    List<AssetDiscoveryResult> findHighConfidenceResults(@Param("taskId") Long taskId,
                                                         @Param("minConfidence") java.math.BigDecimal minConfidence);

    /**
     * 根据设备类型统计
     */
    @Query("SELECT r.deviceType, COUNT(r) FROM AssetDiscoveryResult r " +
           "WHERE r.taskId = :taskId AND r.deviceType IS NOT NULL " +
           "GROUP BY r.deviceType")
    List<Object[]> countByDeviceType(@Param("taskId") Long taskId);

    /**
     * 查找最近发现的结果
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.discoveredTime >= :since " +
           "ORDER BY r.discoveredTime DESC")
    List<AssetDiscoveryResult> findRecentResults(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 根据厂商统计
     */
    @Query("SELECT r.vendor, COUNT(r) FROM AssetDiscoveryResult r " +
           "WHERE r.taskId = :taskId AND r.vendor IS NOT NULL " +
           "GROUP BY r.vendor")
    List<Object[]> countByVendor(@Param("taskId") Long taskId);

    /**
     * 查找未关联的结果
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.assetId IS NULL " +
           "AND r.correlationStatus = 'NEW' ORDER BY r.discoveredTime DESC")
    List<AssetDiscoveryResult> findUnmatchedResults(Pageable pageable);

    /**
     * 根据响应时间范围查找
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.taskId = :taskId " +
           "AND r.responseTime BETWEEN :minTime AND :maxTime " +
           "ORDER BY r.responseTime ASC")
    List<AssetDiscoveryResult> findByResponseTimeRange(@Param("taskId") Long taskId,
                                                       @Param("minTime") Integer minTime,
                                                       @Param("maxTime") Integer maxTime);

    /**
     * 删除旧的发现结果
     */
    @Query("DELETE FROM AssetDiscoveryResult r WHERE r.discoveredTime < :cutoffTime")
    void deleteOldResults(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 查找特定IP范围的结果
     */
    @Query("SELECT r FROM AssetDiscoveryResult r WHERE r.taskId = :taskId " +
           "AND r.ipAddress LIKE :ipPattern ORDER BY r.ipAddress")
    List<AssetDiscoveryResult> findByIpPattern(@Param("taskId") Long taskId,
                                              @Param("ipPattern") String ipPattern);
}
