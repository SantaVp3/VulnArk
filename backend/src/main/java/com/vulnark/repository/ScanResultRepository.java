package com.vulnark.repository;

import com.vulnark.entity.ScanResult;
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
 * 扫描结果数据访问层
 */
@Repository
public interface ScanResultRepository extends JpaRepository<ScanResult, Long> {

    /**
     * 根据ID查找未删除的扫描结果
     */
    Optional<ScanResult> findByIdAndDeletedFalse(Long id);

    /**
     * 查找所有未删除的扫描结果
     */
    Page<ScanResult> findByDeletedFalse(Pageable pageable);

    /**
     * 根据扫描任务ID查找结果
     */
    Page<ScanResult> findByScanTaskIdAndDeletedFalse(Long scanTaskId, Pageable pageable);

    /**
     * 根据扫描任务ID查找所有结果
     */
    List<ScanResult> findByScanTaskIdAndDeletedFalse(Long scanTaskId);

    /**
     * 根据严重程度查找扫描结果
     */
    Page<ScanResult> findBySeverityAndDeletedFalse(ScanResult.Severity severity, Pageable pageable);

    /**
     * 根据漏洞状态查找扫描结果
     */
    Page<ScanResult> findByStatusAndDeletedFalse(ScanResult.VulnerabilityStatus status, Pageable pageable);

    /**
     * 根据目标主机查找扫描结果
     */
    Page<ScanResult> findByTargetHostAndDeletedFalse(String targetHost, Pageable pageable);

    /**
     * 根据CVE编号查找扫描结果
     */
    List<ScanResult> findByCveIdAndDeletedFalse(String cveId);

    /**
     * 根据资产ID查找扫描结果
     */
    Page<ScanResult> findByAssetIdAndDeletedFalse(Long assetId, Pageable pageable);

    /**
     * 查找误报结果
     */
    Page<ScanResult> findByFalsePositiveTrueAndDeletedFalse(Pageable pageable);

    /**
     * 查找已确认的结果
     */
    Page<ScanResult> findByConfirmationStatusAndDeletedFalse(ScanResult.ConfirmationStatus status, Pageable pageable);

    /**
     * 查找指定时间范围内发现的漏洞
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.deleted = false AND sr.discoveredTime BETWEEN :startTime AND :endTime")
    List<ScanResult> findByDiscoveredTimeBetweenAndDeletedFalse(@Param("startTime") LocalDateTime startTime, 
                                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计各严重程度的漏洞数量
     */
    @Query("SELECT sr.severity, COUNT(sr) FROM ScanResult sr WHERE sr.deleted = false GROUP BY sr.severity")
    List<Object[]> countBySeverity();

    /**
     * 统计各状态的漏洞数量
     */
    @Query("SELECT sr.status, COUNT(sr) FROM ScanResult sr WHERE sr.deleted = false GROUP BY sr.status")
    List<Object[]> countByStatus();

    /**
     * 统计扫描任务的漏洞数量
     */
    @Query("SELECT COUNT(sr) FROM ScanResult sr WHERE sr.scanTask.id = :scanTaskId AND sr.deleted = false")
    long countByScanTaskId(@Param("scanTaskId") Long scanTaskId);

    /**
     * 统计扫描任务各严重程度的漏洞数量
     */
    @Query("SELECT sr.severity, COUNT(sr) FROM ScanResult sr WHERE sr.scanTask.id = :scanTaskId AND sr.deleted = false GROUP BY sr.severity")
    List<Object[]> countByScanTaskIdAndSeverity(@Param("scanTaskId") Long scanTaskId);

    /**
     * 查找高危漏洞
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.deleted = false AND sr.severity IN ('CRITICAL', 'HIGH')")
    List<ScanResult> findHighRiskVulnerabilities(Pageable pageable);

    /**
     * 查找指定CVSS评分范围的漏洞
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.deleted = false AND sr.cvssScore BETWEEN :minScore AND :maxScore")
    List<ScanResult> findByCvssScoreRange(@Param("minScore") Double minScore, @Param("maxScore") Double maxScore);

    /**
     * 查找最新发现的漏洞
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.deleted = false ORDER BY sr.discoveredTime DESC")
    List<ScanResult> findLatestVulnerabilities(Pageable pageable);

    /**
     * 根据漏洞名称模糊查询
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.deleted = false AND sr.vulnerabilityName LIKE %:name%")
    List<ScanResult> findByVulnerabilityNameContaining(@Param("name") String name);

    /**
     * 查找重复的漏洞（相同主机、端口、漏洞名称）
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.deleted = false AND " +
           "sr.targetHost = :targetHost AND sr.targetPort = :targetPort AND sr.vulnerabilityName = :vulnerabilityName")
    List<ScanResult> findDuplicateVulnerabilities(@Param("targetHost") String targetHost,
                                                  @Param("targetPort") Integer targetPort,
                                                  @Param("vulnerabilityName") String vulnerabilityName);

    /**
     * 统计目标主机的漏洞数量
     */
    @Query("SELECT sr.targetHost, COUNT(sr) FROM ScanResult sr WHERE sr.deleted = false GROUP BY sr.targetHost")
    List<Object[]> countByTargetHost();

    /**
     * 查找指定插件的扫描结果
     */
    List<ScanResult> findByPluginIdAndDeletedFalse(String pluginId);

    /**
     * 查找指定插件系列的扫描结果
     */
    List<ScanResult> findByPluginFamilyAndDeletedFalse(String pluginFamily);

    /**
     * 复合条件查询扫描结果
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.deleted = false " +
           "AND (:scanTaskId IS NULL OR sr.scanTask.id = :scanTaskId) " +
           "AND (:severity IS NULL OR sr.severity = :severity) " +
           "AND (:status IS NULL OR sr.status = :status) " +
           "AND (:targetHost IS NULL OR sr.targetHost LIKE %:targetHost%) " +
           "AND (:vulnerabilityName IS NULL OR sr.vulnerabilityName LIKE %:vulnerabilityName%) " +
           "AND (:cveId IS NULL OR sr.cveId = :cveId) " +
           "AND (:falsePositive IS NULL OR sr.falsePositive = :falsePositive)")
    Page<ScanResult> findByConditions(@Param("scanTaskId") Long scanTaskId,
                                     @Param("severity") ScanResult.Severity severity,
                                     @Param("status") ScanResult.VulnerabilityStatus status,
                                     @Param("targetHost") String targetHost,
                                     @Param("vulnerabilityName") String vulnerabilityName,
                                     @Param("cveId") String cveId,
                                     @Param("falsePositive") Boolean falsePositive,
                                     Pageable pageable);

    /**
     * 统计今日新增漏洞数量
     */
    @Query("SELECT COUNT(sr) FROM ScanResult sr WHERE sr.deleted = false AND DATE(sr.discoveredTime) = CURRENT_DATE")
    long countTodayVulnerabilities();

    /**
     * 统计本周新增漏洞数量
     */
    @Query("SELECT COUNT(sr) FROM ScanResult sr WHERE sr.deleted = false AND " +
           "sr.discoveredTime >= :weekStart AND sr.discoveredTime < :weekEnd")
    long countWeekVulnerabilities(@Param("weekStart") LocalDateTime weekStart, @Param("weekEnd") LocalDateTime weekEnd);

    /**
     * 统计本月新增漏洞数量
     */
    @Query("SELECT COUNT(sr) FROM ScanResult sr WHERE sr.deleted = false AND " +
           "YEAR(sr.discoveredTime) = YEAR(CURRENT_DATE) AND MONTH(sr.discoveredTime) = MONTH(CURRENT_DATE)")
    long countMonthVulnerabilities();

    /**
     * 查找需要清理的旧扫描结果
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.deleted = false AND sr.discoveredTime < :cleanupThreshold")
    List<ScanResult> findResultsForCleanup(@Param("cleanupThreshold") LocalDateTime cleanupThreshold);

    /**
     * 批量更新漏洞状态
     */
    @Query("UPDATE ScanResult sr SET sr.status = :newStatus, sr.updatedTime = :updateTime " +
           "WHERE sr.id IN :resultIds AND sr.deleted = false")
    int batchUpdateStatus(@Param("resultIds") List<Long> resultIds, 
                         @Param("newStatus") ScanResult.VulnerabilityStatus newStatus,
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量标记为误报
     */
    @Query("UPDATE ScanResult sr SET sr.falsePositive = true, sr.confirmationStatus = 'FALSE_POSITIVE', " +
           "sr.updatedTime = :updateTime WHERE sr.id IN :resultIds AND sr.deleted = false")
    int batchMarkAsFalsePositive(@Param("resultIds") List<Long> resultIds, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 软删除扫描结果
     */
    @Query("UPDATE ScanResult sr SET sr.deleted = true, sr.updatedTime = :updateTime " +
           "WHERE sr.id = :resultId AND sr.deleted = false")
    int softDelete(@Param("resultId") Long resultId, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 批量软删除扫描结果
     */
    @Query("UPDATE ScanResult sr SET sr.deleted = true, sr.updatedTime = :updateTime " +
           "WHERE sr.id IN :resultIds AND sr.deleted = false")
    int batchSoftDelete(@Param("resultIds") List<Long> resultIds, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据扫描任务删除所有相关结果
     */
    @Query("UPDATE ScanResult sr SET sr.deleted = true, sr.updatedTime = :updateTime " +
           "WHERE sr.scanTask.id = :scanTaskId AND sr.deleted = false")
    int deleteByScanTaskId(@Param("scanTaskId") Long scanTaskId, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 统计资产的漏洞分布
     */
    @Query("SELECT sr.severity, COUNT(sr) FROM ScanResult sr WHERE sr.asset.id = :assetId AND sr.deleted = false GROUP BY sr.severity")
    List<Object[]> countByAssetIdAndSeverity(@Param("assetId") Long assetId);

    /**
     * 查找资产的最新漏洞
     */
    @Query("SELECT sr FROM ScanResult sr WHERE sr.asset.id = :assetId AND sr.deleted = false ORDER BY sr.discoveredTime DESC")
    List<ScanResult> findLatestByAssetId(@Param("assetId") Long assetId, Pageable pageable);

    /**
     * 统计漏洞趋势数据
     */
    @Query("SELECT DATE(sr.discoveredTime), sr.severity, COUNT(sr) FROM ScanResult sr " +
           "WHERE sr.deleted = false AND sr.discoveredTime BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(sr.discoveredTime), sr.severity ORDER BY DATE(sr.discoveredTime)")
    List<Object[]> getVulnerabilityTrends(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
