package com.vulnark.repository;

import com.vulnark.entity.BaselineCheck;
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
 * 基线检查Repository
 */
@Repository
public interface BaselineCheckRepository extends JpaRepository<BaselineCheck, Long> {

    /**
     * 根据ID查找未删除的基线检查
     */
    Optional<BaselineCheck> findByIdAndDeletedFalse(Long id);

    /**
     * 查找所有未删除的基线检查
     */
    List<BaselineCheck> findByDeletedFalse();

    /**
     * 分页查找未删除的基线检查
     */
    Page<BaselineCheck> findByDeletedFalse(Pageable pageable);

    /**
     * 根据资产ID查找基线检查
     */
    List<BaselineCheck> findByAssetIdAndDeletedFalse(Long assetId);

    /**
     * 根据检查类型查找基线检查
     */
    List<BaselineCheck> findByCheckTypeAndDeletedFalse(BaselineCheck.CheckType checkType);

    /**
     * 根据状态查找基线检查
     */
    List<BaselineCheck> findByStatusAndDeletedFalse(BaselineCheck.CheckStatus status);

    /**
     * 根据创建者查找基线检查
     */
    List<BaselineCheck> findByCreatedByIdAndDeletedFalse(Long createdById);

    /**
     * 条件查询基线检查
     */
    @Query("SELECT bc FROM BaselineCheck bc WHERE bc.deleted = false " +
           "AND (:name IS NULL OR bc.name LIKE %:name%) " +
           "AND (:checkType IS NULL OR bc.checkType = :checkType) " +
           "AND (:status IS NULL OR bc.status = :status) " +
           "AND (:assetId IS NULL OR bc.asset.id = :assetId) " +
           "AND (:createdById IS NULL OR bc.createdBy.id = :createdById)")
    Page<BaselineCheck> findByConditions(
            @Param("name") String name,
            @Param("checkType") BaselineCheck.CheckType checkType,
            @Param("status") BaselineCheck.CheckStatus status,
            @Param("assetId") Long assetId,
            @Param("createdById") Long createdById,
            Pageable pageable);

    /**
     * 统计各状态的检查数量
     */
    @Query("SELECT bc.status, COUNT(bc) FROM BaselineCheck bc WHERE bc.deleted = false GROUP BY bc.status")
    List<Object[]> countByStatus();

    /**
     * 统计各检查类型的数量
     */
    @Query("SELECT bc.checkType, COUNT(bc) FROM BaselineCheck bc WHERE bc.deleted = false GROUP BY bc.checkType")
    List<Object[]> countByCheckType();

    /**
     * 统计各结果的数量
     */
    @Query("SELECT bc.result, COUNT(bc) FROM BaselineCheck bc WHERE bc.deleted = false AND bc.result IS NOT NULL GROUP BY bc.result")
    List<Object[]> countByResult();

    /**
     * 查找最近的检查记录
     */
    @Query("SELECT bc FROM BaselineCheck bc WHERE bc.deleted = false " +
           "AND bc.createdTime >= :since ORDER BY bc.createdTime DESC")
    List<BaselineCheck> findRecentChecks(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 查找正在运行的检查
     */
    List<BaselineCheck> findByStatusInAndDeletedFalse(List<BaselineCheck.CheckStatus> statuses);

    /**
     * 查找需要清理的过期检查
     */
    @Query("SELECT bc FROM BaselineCheck bc WHERE bc.deleted = false " +
           "AND bc.status IN ('COMPLETED', 'FAILED', 'CANCELLED') " +
           "AND bc.endTime < :cutoffTime")
    List<BaselineCheck> findChecksToCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 计算平均合规分数
     */
    @Query("SELECT AVG(bc.complianceScore) FROM BaselineCheck bc WHERE bc.deleted = false " +
           "AND bc.status = 'COMPLETED' AND bc.complianceScore IS NOT NULL")
    Double calculateAverageComplianceScore();

    /**
     * 根据资产和检查类型查找最新的检查
     */
    @Query("SELECT bc FROM BaselineCheck bc WHERE bc.deleted = false " +
           "AND bc.asset.id = :assetId AND bc.checkType = :checkType " +
           "ORDER BY bc.createdTime DESC")
    List<BaselineCheck> findLatestByAssetAndType(@Param("assetId") Long assetId, 
                                                @Param("checkType") BaselineCheck.CheckType checkType, 
                                                Pageable pageable);

    /**
     * 统计资产的检查情况
     */
    @Query("SELECT bc.asset.id, bc.asset.name, COUNT(bc), " +
           "SUM(CASE WHEN bc.result = 'PASS' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN bc.result = 'FAIL' THEN 1 ELSE 0 END), " +
           "AVG(bc.complianceScore) " +
           "FROM BaselineCheck bc WHERE bc.deleted = false " +
           "AND bc.status = 'COMPLETED' " +
           "GROUP BY bc.asset.id, bc.asset.name")
    List<Object[]> getAssetCheckStatistics();

    /**
     * 查找低合规分数的检查
     */
    @Query("SELECT bc FROM BaselineCheck bc WHERE bc.deleted = false " +
           "AND bc.status = 'COMPLETED' AND bc.complianceScore < :threshold " +
           "ORDER BY bc.complianceScore ASC")
    List<BaselineCheck> findLowComplianceChecks(@Param("threshold") Double threshold, Pageable pageable);
}
