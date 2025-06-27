package com.vulnark.repository;

import com.vulnark.entity.BaselineCheckItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 基线检查项Repository
 */
@Repository
public interface BaselineCheckItemRepository extends JpaRepository<BaselineCheckItem, Long> {

    /**
     * 根据基线检查ID查找检查项
     */
    List<BaselineCheckItem> findByBaselineCheckId(Long baselineCheckId);

    /**
     * 分页查找基线检查项
     */
    Page<BaselineCheckItem> findByBaselineCheckId(Long baselineCheckId, Pageable pageable);

    /**
     * 根据基线检查ID和状态查找检查项
     */
    List<BaselineCheckItem> findByBaselineCheckIdAndStatus(Long baselineCheckId, BaselineCheckItem.ItemStatus status);

    /**
     * 根据基线检查ID和结果查找检查项
     */
    List<BaselineCheckItem> findByBaselineCheckIdAndResult(Long baselineCheckId, BaselineCheckItem.ItemResult result);

    /**
     * 根据基线检查ID和严重级别查找检查项
     */
    List<BaselineCheckItem> findByBaselineCheckIdAndSeverity(Long baselineCheckId, BaselineCheckItem.SeverityLevel severity);

    /**
     * 统计基线检查的各状态项数
     */
    @Query("SELECT bci.status, COUNT(bci) FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId GROUP BY bci.status")
    List<Object[]> countByStatusForCheck(@Param("baselineCheckId") Long baselineCheckId);

    /**
     * 统计基线检查的各结果项数
     */
    @Query("SELECT bci.result, COUNT(bci) FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId AND bci.result IS NOT NULL GROUP BY bci.result")
    List<Object[]> countByResultForCheck(@Param("baselineCheckId") Long baselineCheckId);

    /**
     * 统计基线检查的各严重级别项数
     */
    @Query("SELECT bci.severity, COUNT(bci) FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId GROUP BY bci.severity")
    List<Object[]> countBySeverityForCheck(@Param("baselineCheckId") Long baselineCheckId);

    /**
     * 条件查询检查项
     */
    @Query("SELECT bci FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId " +
           "AND (:itemName IS NULL OR bci.itemName LIKE %:itemName%) " +
           "AND (:category IS NULL OR bci.category = :category) " +
           "AND (:severity IS NULL OR bci.severity = :severity) " +
           "AND (:status IS NULL OR bci.status = :status) " +
           "AND (:result IS NULL OR bci.result = :result)")
    Page<BaselineCheckItem> findByConditions(
            @Param("baselineCheckId") Long baselineCheckId,
            @Param("itemName") String itemName,
            @Param("category") String category,
            @Param("severity") BaselineCheckItem.SeverityLevel severity,
            @Param("status") BaselineCheckItem.ItemStatus status,
            @Param("result") BaselineCheckItem.ItemResult result,
            Pageable pageable);

    /**
     * 查找失败的检查项
     */
    @Query("SELECT bci FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId " +
           "AND bci.result = 'FAIL' ORDER BY bci.severity DESC, bci.itemCode ASC")
    List<BaselineCheckItem> findFailedItems(@Param("baselineCheckId") Long baselineCheckId);

    /**
     * 查找高风险检查项
     */
    @Query("SELECT bci FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId " +
           "AND bci.severity IN ('CRITICAL', 'HIGH') AND bci.result = 'FAIL' " +
           "ORDER BY bci.severity DESC, bci.itemCode ASC")
    List<BaselineCheckItem> findHighRiskFailedItems(@Param("baselineCheckId") Long baselineCheckId);

    /**
     * 统计检查项分类
     */
    @Query("SELECT bci.category, COUNT(bci) FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId GROUP BY bci.category")
    List<Object[]> countByCategoryForCheck(@Param("baselineCheckId") Long baselineCheckId);

    /**
     * 计算检查完成进度
     */
    @Query("SELECT COUNT(bci) FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId AND bci.status = 'COMPLETED'")
    Long countCompletedItems(@Param("baselineCheckId") Long baselineCheckId);

    /**
     * 计算总检查项数
     */
    @Query("SELECT COUNT(bci) FROM BaselineCheckItem bci WHERE bci.baselineCheck.id = :baselineCheckId")
    Long countTotalItems(@Param("baselineCheckId") Long baselineCheckId);

    /**
     * 删除基线检查的所有检查项
     */
    void deleteByBaselineCheckId(Long baselineCheckId);

    /**
     * 查找待检查的项目
     */
    List<BaselineCheckItem> findByBaselineCheckIdAndStatusOrderByItemCodeAsc(Long baselineCheckId, BaselineCheckItem.ItemStatus status);

    /**
     * 查找正在检查的项目
     */
    List<BaselineCheckItem> findByBaselineCheckIdAndStatusIn(Long baselineCheckId, List<BaselineCheckItem.ItemStatus> statuses);
}
