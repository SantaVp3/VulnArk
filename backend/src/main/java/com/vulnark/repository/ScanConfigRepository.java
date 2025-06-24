package com.vulnark.repository;

import com.vulnark.entity.ScanConfig;
import com.vulnark.entity.ScanTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScanConfigRepository extends JpaRepository<ScanConfig, Long> {
    
    // 根据删除状态查询
    List<ScanConfig> findByDeletedFalse();
    Page<ScanConfig> findByDeletedFalse(Pageable pageable);
    
    // 根据ID和删除状态查询
    Optional<ScanConfig> findByIdAndDeletedFalse(Long id);
    
    // 根据启用状态查询
    List<ScanConfig> findByEnabledTrueAndDeletedFalse();
    
    // 根据默认配置查询
    List<ScanConfig> findByIsDefaultTrueAndDeletedFalse();
    Optional<ScanConfig> findFirstByIsDefaultTrueAndDeletedFalse();
    
    // 根据扫描引擎查询
    List<ScanConfig> findByScanEngineAndDeletedFalse(ScanTask.ScanEngine scanEngine);
    List<ScanConfig> findByScanEngineAndEnabledTrueAndDeletedFalse(ScanTask.ScanEngine scanEngine);
    
    // 根据扫描类型查询
    List<ScanConfig> findByScanTypeAndDeletedFalse(ScanTask.ScanType scanType);
    List<ScanConfig> findByScanTypeAndEnabledTrueAndDeletedFalse(ScanTask.ScanType scanType);
    
    // 根据扫描模板查询
    List<ScanConfig> findByScanTemplateAndDeletedFalse(ScanTask.ScanTemplate scanTemplate);
    
    // 根据创建者查询
    List<ScanConfig> findByCreatedByIdAndDeletedFalse(Long createdById);
    Page<ScanConfig> findByCreatedByIdAndDeletedFalse(Long createdById, Pageable pageable);
    
    // 根据名称查询
    Optional<ScanConfig> findByNameAndDeletedFalse(String name);
    List<ScanConfig> findByNameContainingIgnoreCaseAndDeletedFalse(String name);
    
    // 检查名称是否存在
    @Query("SELECT COUNT(sc) > 0 FROM ScanConfig sc WHERE sc.name = :name AND sc.deleted = false " +
           "AND (:excludeId IS NULL OR sc.id != :excludeId)")
    boolean existsByNameAndDeletedFalse(@Param("name") String name, @Param("excludeId") Long excludeId);
    
    // 复合查询
    @Query("SELECT sc FROM ScanConfig sc WHERE " +
           "(:name IS NULL OR sc.name LIKE %:name%) AND " +
           "(:scanEngine IS NULL OR sc.scanEngine = :scanEngine) AND " +
           "(:scanType IS NULL OR sc.scanType = :scanType) AND " +
           "(:enabled IS NULL OR sc.enabled = :enabled) AND " +
           "(:createdById IS NULL OR sc.createdBy.id = :createdById) AND " +
           "sc.deleted = false")
    Page<ScanConfig> findByConditions(@Param("name") String name,
                                     @Param("scanEngine") ScanTask.ScanEngine scanEngine,
                                     @Param("scanType") ScanTask.ScanType scanType,
                                     @Param("enabled") Boolean enabled,
                                     @Param("createdById") Long createdById,
                                     Pageable pageable);
    
    // 统计查询
    @Query("SELECT COUNT(sc) FROM ScanConfig sc WHERE sc.deleted = false")
    long countAllConfigs();
    
    @Query("SELECT COUNT(sc) FROM ScanConfig sc WHERE sc.enabled = true AND sc.deleted = false")
    long countEnabledConfigs();
    
    @Query("SELECT COUNT(sc) FROM ScanConfig sc WHERE sc.scanEngine = :scanEngine AND sc.deleted = false")
    long countByScanEngine(@Param("scanEngine") ScanTask.ScanEngine scanEngine);
    
    @Query("SELECT COUNT(sc) FROM ScanConfig sc WHERE sc.scanType = :scanType AND sc.deleted = false")
    long countByScanType(@Param("scanType") ScanTask.ScanType scanType);
    
    // 统计各扫描引擎的配置数量
    @Query("SELECT sc.scanEngine, COUNT(sc) FROM ScanConfig sc WHERE sc.deleted = false GROUP BY sc.scanEngine")
    List<Object[]> getScanEngineStatistics();
    
    // 统计各扫描类型的配置数量
    @Query("SELECT sc.scanType, COUNT(sc) FROM ScanConfig sc WHERE sc.deleted = false GROUP BY sc.scanType")
    List<Object[]> getScanTypeStatistics();
    
    // 查询最常用的配置
    @Query("SELECT sc.id, sc.name, COUNT(st) as usage_count FROM ScanConfig sc " +
           "LEFT JOIN ScanTask st ON st.scanConfigId = sc.id " +
           "WHERE sc.deleted = false " +
           "GROUP BY sc.id, sc.name " +
           "ORDER BY usage_count DESC")
    List<Object[]> getMostUsedConfigs(Pageable pageable);
    
    // 查询推荐配置（根据扫描类型和引擎）
    @Query("SELECT sc FROM ScanConfig sc WHERE sc.scanType = :scanType " +
           "AND (:scanEngine IS NULL OR sc.scanEngine = :scanEngine) " +
           "AND sc.enabled = true AND sc.deleted = false " +
           "ORDER BY sc.isDefault DESC, sc.createdTime DESC")
    List<ScanConfig> findRecommendedConfigs(@Param("scanType") ScanTask.ScanType scanType,
                                           @Param("scanEngine") ScanTask.ScanEngine scanEngine,
                                           Pageable pageable);
    
    // 查询用户的默认配置
    @Query("SELECT sc FROM ScanConfig sc WHERE sc.createdBy.id = :userId " +
           "AND sc.isDefault = true AND sc.enabled = true AND sc.deleted = false")
    List<ScanConfig> findUserDefaultConfigs(@Param("userId") Long userId);
    
    // 查询系统默认配置
    @Query("SELECT sc FROM ScanConfig sc WHERE sc.isDefault = true " +
           "AND sc.enabled = true AND sc.deleted = false " +
           "ORDER BY sc.scanType, sc.scanEngine")
    List<ScanConfig> findSystemDefaultConfigs();
    
    // 全文搜索
    @Query("SELECT sc FROM ScanConfig sc WHERE sc.deleted = false " +
           "AND (sc.name LIKE %:keyword% " +
           "OR sc.description LIKE %:keyword% " +
           "OR sc.parameters LIKE %:keyword%)")
    Page<ScanConfig> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 查询可用于特定扫描深度的配置
    @Query("SELECT sc FROM ScanConfig sc WHERE sc.scanDepth = :scanDepth " +
           "AND sc.enabled = true AND sc.deleted = false")
    List<ScanConfig> findByScanDepth(@Param("scanDepth") ScanConfig.ScanDepth scanDepth);
    
    // 查询支持特定功能的配置
    @Query("SELECT sc FROM ScanConfig sc WHERE " +
           "(:vulnerabilityScan IS NULL OR sc.vulnerabilityScan = :vulnerabilityScan) AND " +
           "(:webAppScan IS NULL OR sc.webAppScan = :webAppScan) AND " +
           "(:serviceDetection IS NULL OR sc.serviceDetection = :serviceDetection) AND " +
           "(:osDetection IS NULL OR sc.osDetection = :osDetection) AND " +
           "sc.enabled = true AND sc.deleted = false")
    List<ScanConfig> findByFeatures(@Param("vulnerabilityScan") Boolean vulnerabilityScan,
                                   @Param("webAppScan") Boolean webAppScan,
                                   @Param("serviceDetection") Boolean serviceDetection,
                                   @Param("osDetection") Boolean osDetection);
}
