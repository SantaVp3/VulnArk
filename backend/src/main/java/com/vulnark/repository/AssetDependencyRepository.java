package com.vulnark.repository;

import com.vulnark.entity.AssetDependency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 资产依赖关系数据访问层
 */
@Repository
public interface AssetDependencyRepository extends JpaRepository<AssetDependency, Long> {
    
    /**
     * 查找未删除的依赖关系
     */
    List<AssetDependency> findByDeletedFalse();
    
    /**
     * 分页查找未删除的依赖关系
     */
    Page<AssetDependency> findByDeletedFalse(Pageable pageable);
    
    /**
     * 根据ID查找未删除的依赖关系
     */
    Optional<AssetDependency> findByIdAndDeletedFalse(Long id);
    
    /**
     * 查找指定资产作为源资产的所有依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.sourceAssetId = :assetId AND ad.deleted = false")
    List<AssetDependency> findBySourceAssetId(@Param("assetId") Long assetId);
    
    /**
     * 查找指定资产作为目标资产的所有依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.targetAssetId = :assetId AND ad.deleted = false")
    List<AssetDependency> findByTargetAssetId(@Param("assetId") Long assetId);
    
    /**
     * 查找指定资产的所有依赖关系（作为源资产或目标资产）
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE (ad.sourceAssetId = :assetId OR ad.targetAssetId = :assetId) AND ad.deleted = false")
    List<AssetDependency> findByAssetId(@Param("assetId") Long assetId);
    
    /**
     * 查找指定项目的所有资产依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad " +
           "JOIN Asset sa ON ad.sourceAssetId = sa.id " +
           "JOIN Asset ta ON ad.targetAssetId = ta.id " +
           "WHERE sa.projectId = :projectId AND ta.projectId = :projectId AND ad.deleted = false")
    List<AssetDependency> findByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 检查两个资产之间是否存在依赖关系
     */
    @Query("SELECT COUNT(ad) > 0 FROM AssetDependency ad " +
           "WHERE ad.sourceAssetId = :sourceAssetId AND ad.targetAssetId = :targetAssetId AND ad.deleted = false")
    boolean existsDependency(@Param("sourceAssetId") Long sourceAssetId, @Param("targetAssetId") Long targetAssetId);
    
    /**
     * 查找循环依赖
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.sourceAssetId = ad.targetAssetId AND ad.deleted = false")
    List<AssetDependency> findCircularDependencies();
    
    /**
     * 查找关键依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.isCritical = true AND ad.deleted = false")
    List<AssetDependency> findCriticalDependencies();
    
    /**
     * 根据依赖类型查找依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.dependencyType = :dependencyType AND ad.deleted = false")
    List<AssetDependency> findByDependencyType(@Param("dependencyType") AssetDependency.DependencyType dependencyType);
    
    /**
     * 根据依赖强度查找依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.dependencyStrength = :dependencyStrength AND ad.deleted = false")
    List<AssetDependency> findByDependencyStrength(@Param("dependencyStrength") AssetDependency.DependencyStrength dependencyStrength);
    
    /**
     * 查找指定资产的直接依赖（该资产依赖的其他资产）
     */
    @Query("SELECT ad FROM AssetDependency ad " +
           "JOIN FETCH ad.targetAsset " +
           "WHERE ad.sourceAssetId = :assetId AND ad.deleted = false")
    List<AssetDependency> findDirectDependencies(@Param("assetId") Long assetId);
    
    /**
     * 查找指定资产的反向依赖（依赖该资产的其他资产）
     */
    @Query("SELECT ad FROM AssetDependency ad " +
           "JOIN FETCH ad.sourceAsset " +
           "WHERE ad.targetAssetId = :assetId AND ad.deleted = false")
    List<AssetDependency> findReverseDependencies(@Param("assetId") Long assetId);
    
    /**
     * 统计指定资产的依赖数量
     */
    @Query("SELECT COUNT(ad) FROM AssetDependency ad WHERE ad.sourceAssetId = :assetId AND ad.deleted = false")
    long countDependenciesByAsset(@Param("assetId") Long assetId);
    
    /**
     * 统计指定资产的反向依赖数量
     */
    @Query("SELECT COUNT(ad) FROM AssetDependency ad WHERE ad.targetAssetId = :assetId AND ad.deleted = false")
    long countReverseDependenciesByAsset(@Param("assetId") Long assetId);
    
    /**
     * 查找断开的依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.status = 'BROKEN' AND ad.deleted = false")
    List<AssetDependency> findBrokenDependencies();
    
    /**
     * 根据端口查找依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.port = :port AND ad.deleted = false")
    List<AssetDependency> findByPort(@Param("port") Integer port);
    
    /**
     * 根据协议查找依赖关系
     */
    @Query("SELECT ad FROM AssetDependency ad WHERE ad.protocol = :protocol AND ad.deleted = false")
    List<AssetDependency> findByProtocol(@Param("protocol") String protocol);
    
    /**
     * 查找指定资产的依赖路径（递归查询）
     */
    @Query(value = "WITH RECURSIVE dependency_path AS (" +
                   "  SELECT source_asset_id, target_asset_id, 1 as level, " +
                   "         CAST(source_asset_id AS CHAR(1000)) as path " +
                   "  FROM asset_dependencies " +
                   "  WHERE source_asset_id = :assetId AND deleted = false " +
                   "  UNION ALL " +
                   "  SELECT ad.source_asset_id, ad.target_asset_id, dp.level + 1, " +
                   "         CONCAT(dp.path, '->', ad.source_asset_id) " +
                   "  FROM asset_dependencies ad " +
                   "  JOIN dependency_path dp ON ad.source_asset_id = dp.target_asset_id " +
                   "  WHERE ad.deleted = false AND dp.level < 10 " +
                   "    AND FIND_IN_SET(ad.target_asset_id, dp.path) = 0 " +
                   ") " +
                   "SELECT * FROM dependency_path", 
           nativeQuery = true)
    List<Object[]> findDependencyPath(@Param("assetId") Long assetId);
}
