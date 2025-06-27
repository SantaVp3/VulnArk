package com.vulnark.service;

import com.vulnark.baseline.BaselineCheckEngine;
import com.vulnark.baseline.BaselineCheckResult;
import com.vulnark.entity.Asset;
import com.vulnark.entity.BaselineCheck;
import com.vulnark.entity.BaselineCheckItem;
import com.vulnark.entity.User;
import com.vulnark.repository.AssetRepository;
import com.vulnark.repository.BaselineCheckItemRepository;
import com.vulnark.repository.BaselineCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 基线检查服务
 */
@Service
@Transactional
public class BaselineCheckService {

    private static final Logger logger = LoggerFactory.getLogger(BaselineCheckService.class);

    @Autowired
    private BaselineCheckRepository baselineCheckRepository;

    @Autowired
    private BaselineCheckItemRepository baselineCheckItemRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private BaselineCheckEngine baselineCheckEngine;

    /**
     * 创建基线检查
     */
    public BaselineCheck createBaselineCheck(BaselineCheck baselineCheck, User currentUser) {
        logger.info("创建基线检查: {}", baselineCheck.getName());
        
        baselineCheck.setCreatedBy(currentUser);
        baselineCheck.setStatus(BaselineCheck.CheckStatus.PENDING);
        baselineCheck.setProgress(0);
        
        return baselineCheckRepository.save(baselineCheck);
    }

    /**
     * 获取基线检查详情
     */
    public Optional<BaselineCheck> getBaselineCheckById(Long id) {
        return baselineCheckRepository.findByIdAndDeletedFalse(id);
    }

    /**
     * 分页查询基线检查
     */
    public Page<BaselineCheck> getBaselineChecks(Pageable pageable) {
        return baselineCheckRepository.findByDeletedFalse(pageable);
    }

    /**
     * 条件查询基线检查
     */
    public Page<BaselineCheck> searchBaselineChecks(String name, BaselineCheck.CheckType checkType, 
                                                   BaselineCheck.CheckStatus status, Long assetId, 
                                                   Long createdById, Pageable pageable) {
        return baselineCheckRepository.findByConditions(name, checkType, status, assetId, createdById, pageable);
    }

    /**
     * 启动基线检查
     */
    @Async
    public CompletableFuture<Void> startBaselineCheck(Long checkId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Optional<BaselineCheck> optionalCheck = baselineCheckRepository.findById(checkId);
                if (!optionalCheck.isPresent()) {
                    logger.error("基线检查不存在: {}", checkId);
                    return;
                }

                BaselineCheck baselineCheck = optionalCheck.get();
                if (!baselineCheck.canStart()) {
                    logger.error("基线检查无法启动，当前状态: {}", baselineCheck.getStatus());
                    return;
                }

                // 获取目标资产
                Asset asset = baselineCheck.getAsset();
                if (asset == null) {
                    baselineCheck.markAsFailed("未指定目标资产");
                    baselineCheckRepository.save(baselineCheck);
                    return;
                }

                // 标记为开始
                baselineCheck.markAsStarted();
                baselineCheckRepository.save(baselineCheck);

                logger.info("开始执行基线检查: {} for asset: {}", baselineCheck.getName(), asset.getName());

                // 执行基线检查
                BaselineCheckResult result = baselineCheckEngine.executeCheck(baselineCheck, asset).get();

                // 保存检查结果
                saveCheckResult(baselineCheck, result);

                logger.info("基线检查完成: {}", baselineCheck.getName());

            } catch (Exception e) {
                logger.error("基线检查执行失败: {}", checkId, e);
                
                Optional<BaselineCheck> optionalCheck = baselineCheckRepository.findById(checkId);
                if (optionalCheck.isPresent()) {
                    BaselineCheck baselineCheck = optionalCheck.get();
                    baselineCheck.markAsFailed("执行异常: " + e.getMessage());
                    baselineCheckRepository.save(baselineCheck);
                }
            }
        });
    }

    /**
     * 停止基线检查
     */
    public void stopBaselineCheck(Long checkId) {
        Optional<BaselineCheck> optionalCheck = baselineCheckRepository.findById(checkId);
        if (!optionalCheck.isPresent()) {
            throw new RuntimeException("基线检查不存在");
        }

        BaselineCheck baselineCheck = optionalCheck.get();
        if (!baselineCheck.canCancel()) {
            throw new RuntimeException("基线检查无法取消，当前状态: " + baselineCheck.getStatus());
        }

        baselineCheck.markAsCancelled();
        baselineCheckRepository.save(baselineCheck);

        logger.info("基线检查已取消: {}", baselineCheck.getName());
    }

    /**
     * 删除基线检查
     */
    public void deleteBaselineCheck(Long checkId) {
        Optional<BaselineCheck> optionalCheck = baselineCheckRepository.findById(checkId);
        if (!optionalCheck.isPresent()) {
            throw new RuntimeException("基线检查不存在");
        }

        BaselineCheck baselineCheck = optionalCheck.get();
        baselineCheck.setDeleted(true);
        baselineCheckRepository.save(baselineCheck);

        logger.info("基线检查已删除: {}", baselineCheck.getName());
    }

    /**
     * 获取基线检查项
     */
    public Page<BaselineCheckItem> getCheckItems(Long checkId, Pageable pageable) {
        return baselineCheckItemRepository.findByBaselineCheckId(checkId, pageable);
    }

    /**
     * 条件查询检查项
     */
    public Page<BaselineCheckItem> searchCheckItems(Long checkId, String itemName, String category,
                                                   BaselineCheckItem.SeverityLevel severity,
                                                   BaselineCheckItem.ItemStatus status,
                                                   BaselineCheckItem.ItemResult result,
                                                   Pageable pageable) {
        return baselineCheckItemRepository.findByConditions(checkId, itemName, category, severity, status, result, pageable);
    }

    /**
     * 获取失败的检查项
     */
    public List<BaselineCheckItem> getFailedItems(Long checkId) {
        return baselineCheckItemRepository.findFailedItems(checkId);
    }

    /**
     * 获取高风险失败项
     */
    public List<BaselineCheckItem> getHighRiskFailedItems(Long checkId) {
        return baselineCheckItemRepository.findHighRiskFailedItems(checkId);
    }

    /**
     * 获取检查统计信息
     */
    public BaselineCheckStatistics getCheckStatistics() {
        BaselineCheckStatistics statistics = new BaselineCheckStatistics();
        
        // 状态统计
        List<Object[]> statusCounts = baselineCheckRepository.countByStatus();
        for (Object[] row : statusCounts) {
            BaselineCheck.CheckStatus status = (BaselineCheck.CheckStatus) row[0];
            Long count = (Long) row[1];
            statistics.addStatusCount(status, count);
        }
        
        // 类型统计
        List<Object[]> typeCounts = baselineCheckRepository.countByCheckType();
        for (Object[] row : typeCounts) {
            BaselineCheck.CheckType type = (BaselineCheck.CheckType) row[0];
            Long count = (Long) row[1];
            statistics.addTypeCount(type, count);
        }
        
        // 结果统计
        List<Object[]> resultCounts = baselineCheckRepository.countByResult();
        for (Object[] row : resultCounts) {
            BaselineCheck.CheckResult result = (BaselineCheck.CheckResult) row[0];
            Long count = (Long) row[1];
            statistics.addResultCount(result, count);
        }
        
        // 平均合规分数
        Double avgScore = baselineCheckRepository.calculateAverageComplianceScore();
        statistics.setAverageComplianceScore(avgScore != null ? avgScore : 0.0);
        
        return statistics;
    }

    /**
     * 获取资产检查统计
     */
    public List<AssetCheckStatistics> getAssetCheckStatistics() {
        List<Object[]> results = baselineCheckRepository.getAssetCheckStatistics();
        return results.stream().map(row -> {
            AssetCheckStatistics stats = new AssetCheckStatistics();
            stats.setAssetId((Long) row[0]);
            stats.setAssetName((String) row[1]);
            stats.setTotalChecks(((Number) row[2]).longValue());
            stats.setPassedChecks(((Number) row[3]).longValue());
            stats.setFailedChecks(((Number) row[4]).longValue());
            stats.setAverageScore((Double) row[5]);
            return stats;
        }).toList();
    }

    /**
     * 保存检查结果
     */
    private void saveCheckResult(BaselineCheck baselineCheck, BaselineCheckResult result) {
        try {
            // 更新基线检查状态
            baselineCheck.setTotalItems(result.getTotalItems());
            baselineCheck.setPassedItems(result.getPassedItems());
            baselineCheck.setFailedItems(result.getFailedItems());
            baselineCheck.setWarningItems(result.getWarningItems());
            baselineCheck.setSkippedItems(result.getSkippedItems());
            baselineCheck.setComplianceScore(result.getComplianceScore());
            
            if (result.getErrorMessage() != null) {
                baselineCheck.markAsFailed(result.getErrorMessage());
            } else {
                baselineCheck.markAsCompleted();
            }
            
            baselineCheckRepository.save(baselineCheck);
            
            // 保存检查项
            if (result.getCheckItems() != null) {
                for (BaselineCheckItem item : result.getCheckItems()) {
                    item.setBaselineCheck(baselineCheck);
                    baselineCheckItemRepository.save(item);
                }
            }
            
        } catch (Exception e) {
            logger.error("保存检查结果失败: {}", baselineCheck.getId(), e);
            baselineCheck.markAsFailed("保存结果失败: " + e.getMessage());
            baselineCheckRepository.save(baselineCheck);
        }
    }

    /**
     * 基线检查统计信息
     */
    public static class BaselineCheckStatistics {
        private java.util.Map<BaselineCheck.CheckStatus, Long> statusCounts = new java.util.HashMap<>();
        private java.util.Map<BaselineCheck.CheckType, Long> typeCounts = new java.util.HashMap<>();
        private java.util.Map<BaselineCheck.CheckResult, Long> resultCounts = new java.util.HashMap<>();
        private Double averageComplianceScore = 0.0;

        public void addStatusCount(BaselineCheck.CheckStatus status, Long count) {
            statusCounts.put(status, count);
        }

        public void addTypeCount(BaselineCheck.CheckType type, Long count) {
            typeCounts.put(type, count);
        }

        public void addResultCount(BaselineCheck.CheckResult result, Long count) {
            resultCounts.put(result, count);
        }

        // Getters and Setters
        public java.util.Map<BaselineCheck.CheckStatus, Long> getStatusCounts() { return statusCounts; }
        public java.util.Map<BaselineCheck.CheckType, Long> getTypeCounts() { return typeCounts; }
        public java.util.Map<BaselineCheck.CheckResult, Long> getResultCounts() { return resultCounts; }
        public Double getAverageComplianceScore() { return averageComplianceScore; }
        public void setAverageComplianceScore(Double averageComplianceScore) { this.averageComplianceScore = averageComplianceScore; }
    }

    /**
     * 资产检查统计信息
     */
    public static class AssetCheckStatistics {
        private Long assetId;
        private String assetName;
        private Long totalChecks;
        private Long passedChecks;
        private Long failedChecks;
        private Double averageScore;

        // Getters and Setters
        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }
        public String getAssetName() { return assetName; }
        public void setAssetName(String assetName) { this.assetName = assetName; }
        public Long getTotalChecks() { return totalChecks; }
        public void setTotalChecks(Long totalChecks) { this.totalChecks = totalChecks; }
        public Long getPassedChecks() { return passedChecks; }
        public void setPassedChecks(Long passedChecks) { this.passedChecks = passedChecks; }
        public Long getFailedChecks() { return failedChecks; }
        public void setFailedChecks(Long failedChecks) { this.failedChecks = failedChecks; }
        public Double getAverageScore() { return averageScore; }
        public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    }
}
