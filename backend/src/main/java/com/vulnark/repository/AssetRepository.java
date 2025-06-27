package com.vulnark.repository;

import com.vulnark.entity.Asset;
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
public interface AssetRepository extends JpaRepository<Asset, Long> {
    
    // 查找未删除的资产
    List<Asset> findByDeletedFalse();
    
    // 分页查找未删除的资产
    Page<Asset> findByDeletedFalse(Pageable pageable);
    
    // 根据ID查找未删除的资产
    Optional<Asset> findByIdAndDeletedFalse(Long id);
    
    // 根据项目ID查找资产
    List<Asset> findByProjectIdAndDeletedFalse(Long projectId);
    
    // 根据负责人ID查找资产
    List<Asset> findByOwnerIdAndDeletedFalse(Long ownerId);
    
    // 根据资产类型查找资产
    List<Asset> findByTypeAndDeletedFalse(Asset.AssetType type);
    
    // 根据状态查找资产
    List<Asset> findByStatusAndDeletedFalse(Asset.Status status);
    
    // 根据重要性等级查找资产
    List<Asset> findByImportanceAndDeletedFalse(Asset.Importance importance);
    
    // 根据IP地址查找资产
    Optional<Asset> findByIpAddressAndDeletedFalse(String ipAddress);
    
    // 根据域名查找资产
    Optional<Asset> findByDomainAndDeletedFalse(String domain);
    
    // 复合查询：根据多个条件查找资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND (:name IS NULL OR a.name LIKE %:name%) " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:importance IS NULL OR a.importance = :importance) " +
           "AND (:projectId IS NULL OR a.projectId = :projectId) " +
           "AND (:ownerId IS NULL OR a.ownerId = :ownerId) " +
           "AND (:ipAddress IS NULL OR a.ipAddress LIKE %:ipAddress%) " +
           "AND (:domain IS NULL OR a.domain LIKE %:domain%)")
    Page<Asset> findByConditions(
            @Param("name") String name,
            @Param("type") Asset.AssetType type,
            @Param("status") Asset.Status status,
            @Param("importance") Asset.Importance importance,
            @Param("projectId") Long projectId,
            @Param("ownerId") Long ownerId,
            @Param("ipAddress") String ipAddress,
            @Param("domain") String domain,
            Pageable pageable);
    
    // 统计查询
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false")
    long countByDeletedFalse();
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.type = :type")
    long countByTypeAndDeletedFalse(@Param("type") Asset.AssetType type);
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.status = :status")
    long countByStatusAndDeletedFalse(@Param("status") Asset.Status status);
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.importance = :importance")
    long countByImportanceAndDeletedFalse(@Param("importance") Asset.Importance importance);
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.projectId = :projectId")
    long countByProjectIdAndDeletedFalse(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.ownerId = :ownerId")
    long countByOwnerIdAndDeletedFalse(@Param("ownerId") Long ownerId);
    
    // 获取最近的资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false ORDER BY a.createdTime DESC")
    List<Asset> findRecentAssets(Pageable pageable);
    
    // 获取高风险资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND a.riskScore >= :minRiskScore " +
           "ORDER BY a.riskScore DESC")
    List<Asset> findHighRiskAssets(@Param("minRiskScore") Double minRiskScore);
    
    // 获取需要扫描的资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND a.status = 'ACTIVE' " +
           "AND (a.lastScanTime IS NULL OR a.lastScanTime < :beforeTime)")
    List<Asset> findAssetsNeedingScan(@Param("beforeTime") LocalDateTime beforeTime);
    
    // 获取活跃资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND a.status = 'ACTIVE'")
    List<Asset> findActiveAssets();
    
    // 全文搜索
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND (a.name LIKE %:keyword% " +
           "OR a.description LIKE %:keyword% " +
           "OR a.ipAddress LIKE %:keyword% " +
           "OR a.domain LIKE %:keyword% " +
           "OR a.service LIKE %:keyword% " +
           "OR a.operatingSystem LIKE %:keyword% " +
           "OR a.vendor LIKE %:keyword% " +
           "OR a.tags LIKE %:keyword%)")
    Page<Asset> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 根据标签搜索资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND a.tags LIKE %:tag%")
    List<Asset> findByTagsContaining(@Param("tag") String tag);
    
    // 获取资产统计信息
    @Query("SELECT a.type, COUNT(a) FROM Asset a WHERE a.deleted = false GROUP BY a.type")
    List<Object[]> getAssetTypeStatistics();
    
    @Query("SELECT a.status, COUNT(a) FROM Asset a WHERE a.deleted = false GROUP BY a.status")
    List<Object[]> getAssetStatusStatistics();
    
    @Query("SELECT a.importance, COUNT(a) FROM Asset a WHERE a.deleted = false GROUP BY a.importance")
    List<Object[]> getAssetImportanceStatistics();
    
    // 根据IP地址范围查找资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND a.ipAddress IS NOT NULL " +
           "AND a.ipAddress LIKE :ipPattern")
    List<Asset> findByIpAddressPattern(@Param("ipPattern") String ipPattern);
    
    // 根据端口查找资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND a.port = :port")
    List<Asset> findByPort(@Param("port") Integer port);
    
    // 根据服务查找资产
    @Query("SELECT a FROM Asset a WHERE a.deleted = false " +
           "AND a.service LIKE %:service%")
    List<Asset> findByServiceContaining(@Param("service") String service);

    // 根据ID列表查找资产
    List<Asset> findByIdInAndDeletedFalse(List<Long> ids);

    // 仪表盘相关查询
    // 按重要性统计资产数量（字符串参数）
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.deleted = false AND a.importance = :importance")
    long countByImportanceStringAndDeletedFalse(@Param("importance") String importance);

    // 检查资产是否存在且未删除
    boolean existsByIdAndDeletedFalse(Long id);

    // 根据名称模糊查询未删除的资产
    List<Asset> findByNameContainingAndDeletedFalse(String name);
}
