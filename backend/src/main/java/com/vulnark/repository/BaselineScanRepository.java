package com.vulnark.repository;

import com.vulnark.entity.BaselineScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 基线扫描仓库接口
 */
@Repository
public interface BaselineScanRepository extends JpaRepository<BaselineScan, Long> {
    
    /**
     * 查找未删除的扫描记录
     */
    @Query("SELECT bs FROM BaselineScan bs WHERE bs.deleted = false")
    Page<BaselineScan> findAllNotDeleted(Pageable pageable);
    
    /**
     * 根据条件查询扫描记录
     */
    @Query("SELECT bs FROM BaselineScan bs WHERE bs.deleted = false " +
           "AND (:scanName IS NULL OR bs.scanName LIKE %:scanName%) " +
           "AND (:status IS NULL OR bs.status = :status) " +
           "AND (:scanType IS NULL OR bs.scanType = :scanType)")
    Page<BaselineScan> findByConditions(
        @Param("scanName") String scanName,
        @Param("status") BaselineScan.ScanStatus status,
        @Param("scanType") String scanType,
        Pageable pageable
    );
    
    /**
     * 根据ID查找未删除的扫描记录
     */
    @Query("SELECT bs FROM BaselineScan bs WHERE bs.id = :id AND bs.deleted = false")
    Optional<BaselineScan> findByIdNotDeleted(@Param("id") Long id);
    
    /**
     * 根据资产ID查找扫描记录
     */
    @Query("SELECT bs FROM BaselineScan bs WHERE bs.assetId = :assetId AND bs.deleted = false")
    List<BaselineScan> findByAssetIdNotDeleted(@Param("assetId") Long assetId);
    
    /**
     * 统计各状态的扫描数量
     */
    @Query("SELECT bs.status, COUNT(bs) FROM BaselineScan bs WHERE bs.deleted = false GROUP BY bs.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计各扫描类型的数量
     */
    @Query("SELECT bs.scanType, COUNT(bs) FROM BaselineScan bs WHERE bs.deleted = false GROUP BY bs.scanType")
    List<Object[]> countByScanType();
    
    /**
     * 计算平均合规分数
     */
    @Query("SELECT AVG(bs.complianceScore) FROM BaselineScan bs WHERE bs.deleted = false AND bs.status = 'COMPLETED'")
    Double getAverageComplianceScore();
    
    /**
     * 获取最近的扫描记录
     */
    @Query("SELECT bs FROM BaselineScan bs WHERE bs.deleted = false ORDER BY bs.createdTime DESC")
    List<BaselineScan> findRecentScans(Pageable pageable);
    
    /**
     * 统计总扫描数量
     */
    @Query("SELECT COUNT(bs) FROM BaselineScan bs WHERE bs.deleted = false")
    Long countNotDeleted();
}
