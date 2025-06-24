package com.vulnark.repository;

import com.vulnark.entity.AssetFingerprint;
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
public interface AssetFingerprintRepository extends JpaRepository<AssetFingerprint, Long> {
    
    // 根据资产ID查询指纹信息
    List<AssetFingerprint> findByAssetIdAndActiveTrue(Long assetId);
    
    // 根据资产ID和指纹类型查询
    List<AssetFingerprint> findByAssetIdAndTypeAndActiveTrue(Long assetId, AssetFingerprint.FingerprintType type);
    
    // 根据指纹类型查询
    List<AssetFingerprint> findByTypeAndActiveTrue(AssetFingerprint.FingerprintType type);
    
    // 根据技术名称查询
    List<AssetFingerprint> findByNameContainingIgnoreCaseAndActiveTrue(String name);
    
    // 根据厂商查询
    List<AssetFingerprint> findByVendorContainingIgnoreCaseAndActiveTrue(String vendor);
    
    // 根据置信度范围查询
    @Query("SELECT af FROM AssetFingerprint af WHERE af.confidence >= :minConfidence " +
           "AND af.confidence <= :maxConfidence AND af.active = true")
    List<AssetFingerprint> findByConfidenceRange(@Param("minConfidence") Integer minConfidence,
                                                 @Param("maxConfidence") Integer maxConfidence);
    
    // 查询高置信度指纹
    @Query("SELECT af FROM AssetFingerprint af WHERE af.confidence >= 80 AND af.active = true")
    List<AssetFingerprint> findHighConfidenceFingerprints();
    
    // 根据端口查询指纹
    List<AssetFingerprint> findByPortAndActiveTrue(Integer port);
    
    // 根据协议查询指纹
    List<AssetFingerprint> findByProtocolAndActiveTrue(String protocol);
    
    // 分页查询资产指纹
    Page<AssetFingerprint> findByAssetIdAndActiveTrue(Long assetId, Pageable pageable);
    
    // 查询重复的指纹（相同资产、类型、名称）
    @Query("SELECT af FROM AssetFingerprint af WHERE af.assetId = :assetId " +
           "AND af.type = :type AND af.name = :name AND af.active = true")
    List<AssetFingerprint> findDuplicateFingerprints(@Param("assetId") Long assetId,
                                                     @Param("type") AssetFingerprint.FingerprintType type,
                                                     @Param("name") String name);
    
    // 检查指纹是否已存在
    @Query("SELECT COUNT(af) > 0 FROM AssetFingerprint af WHERE af.assetId = :assetId " +
           "AND af.type = :type AND af.name = :name AND af.active = true")
    boolean existsByAssetIdAndTypeAndName(@Param("assetId") Long assetId,
                                         @Param("type") AssetFingerprint.FingerprintType type,
                                         @Param("name") String name);
    
    // 统计指纹类型分布
    @Query("SELECT af.type, COUNT(af) FROM AssetFingerprint af WHERE af.active = true GROUP BY af.type")
    List<Object[]> countByType();
    
    // 统计技术使用情况
    @Query("SELECT af.name, COUNT(DISTINCT af.assetId) FROM AssetFingerprint af " +
           "WHERE af.active = true GROUP BY af.name ORDER BY COUNT(DISTINCT af.assetId) DESC")
    List<Object[]> getTechnologyUsageStatistics();
    
    // 统计厂商分布
    @Query("SELECT af.vendor, COUNT(DISTINCT af.assetId) FROM AssetFingerprint af " +
           "WHERE af.vendor IS NOT NULL AND af.active = true " +
           "GROUP BY af.vendor ORDER BY COUNT(DISTINCT af.assetId) DESC")
    List<Object[]> getVendorStatistics();
    
    // 查询资产的技术栈
    @Query("SELECT af FROM AssetFingerprint af WHERE af.assetId = :assetId AND af.active = true " +
           "ORDER BY af.type, af.confidence DESC")
    List<AssetFingerprint> getAssetTechnologyStack(@Param("assetId") Long assetId);
    
    // 根据识别方法查询
    List<AssetFingerprint> findByMethodAndActiveTrue(AssetFingerprint.IdentificationMethod method);
    
    // 查询最近识别的指纹
    @Query("SELECT af FROM AssetFingerprint af WHERE af.createdTime >= :since AND af.active = true " +
           "ORDER BY af.createdTime DESC")
    List<AssetFingerprint> findRecentFingerprints(@Param("since") LocalDateTime since);
    
    // 查询指定时间范围内的指纹
    @Query("SELECT af FROM AssetFingerprint af WHERE af.createdTime BETWEEN :startTime AND :endTime " +
           "AND af.active = true")
    List<AssetFingerprint> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);
    
    // 软删除指纹（设置为非活跃状态）
    @Query("UPDATE AssetFingerprint af SET af.active = false WHERE af.id = :id")
    void softDeleteById(@Param("id") Long id);
    
    // 软删除资产的所有指纹
    @Query("UPDATE AssetFingerprint af SET af.active = false WHERE af.assetId = :assetId")
    void softDeleteByAssetId(@Param("assetId") Long assetId);
    
    // 查询版本信息不为空的指纹
    @Query("SELECT af FROM AssetFingerprint af WHERE af.version IS NOT NULL " +
           "AND af.version != '' AND af.active = true")
    List<AssetFingerprint> findFingerprintsWithVersion();
    
    // 根据特征信息查询
    @Query("SELECT af FROM AssetFingerprint af WHERE af.signature LIKE %:signature% AND af.active = true")
    List<AssetFingerprint> findBySignatureContaining(@Param("signature") String signature);
    
    // 查询Web相关技术指纹
    @Query("SELECT af FROM AssetFingerprint af WHERE af.type IN ('WEB_SERVER', 'WEB_FRAMEWORK', 'CMS') " +
           "AND af.active = true")
    List<AssetFingerprint> findWebTechnologies();
    
    // 查询数据库相关指纹
    @Query("SELECT af FROM AssetFingerprint af WHERE af.type = 'DATABASE' AND af.active = true")
    List<AssetFingerprint> findDatabaseTechnologies();
    
    // 查询操作系统指纹
    @Query("SELECT af FROM AssetFingerprint af WHERE af.type = 'OPERATING_SYSTEM' AND af.active = true")
    List<AssetFingerprint> findOperatingSystemFingerprints();
    
    // 统计资产指纹数量
    @Query("SELECT COUNT(af) FROM AssetFingerprint af WHERE af.assetId = :assetId AND af.active = true")
    long countByAssetId(@Param("assetId") Long assetId);
    
    // 查询具有相同技术栈的资产
    @Query("SELECT DISTINCT af1.assetId FROM AssetFingerprint af1 " +
           "WHERE af1.name = :technology AND af1.active = true " +
           "AND af1.assetId != :excludeAssetId")
    List<Long> findAssetsWithSameTechnology(@Param("technology") String technology,
                                           @Param("excludeAssetId") Long excludeAssetId);
}
