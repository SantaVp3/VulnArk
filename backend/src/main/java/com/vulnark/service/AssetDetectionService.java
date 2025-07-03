package com.vulnark.service;

import com.vulnark.entity.Asset;
import com.vulnark.entity.AssetDetection;
import com.vulnark.entity.AssetFingerprint;
import com.vulnark.repository.AssetDetectionRepository;
import com.vulnark.repository.AssetFingerprintRepository;
import com.vulnark.repository.AssetRepository;
import com.vulnark.service.detection.AssetDetectionEngine;
import com.vulnark.service.detection.FingerprintEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class AssetDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssetDetectionService.class);
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private AssetDetectionRepository detectionRepository;
    
    @Autowired
    private AssetFingerprintRepository fingerprintRepository;
    
    @Autowired
    private AssetDetectionEngine detectionEngine;
    
    @Autowired
    private FingerprintEngine fingerprintEngine;
    
    /**
     * 检测单个资产
     */
    @Async
    public CompletableFuture<DetectionResult> detectAsset(Long assetId, boolean includeFingerprint) {
        logger.info("开始检测资产: {}", assetId);
        
        try {
            Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                    .orElseThrow(() -> new RuntimeException("资产不存在: " + assetId));
            
            // 执行状态检测
            CompletableFuture<List<AssetDetection>> detectionFuture = 
                    detectionEngine.detectAssetAsync(asset);
            
            // 执行指纹识别（如果需要）
            CompletableFuture<List<AssetFingerprint>> fingerprintFuture = null;
            if (includeFingerprint) {
                fingerprintFuture = fingerprintEngine.identifyFingerprintsAsync(asset);
            }
            
            // 等待检测完成
            List<AssetDetection> detections = detectionFuture.get();
            List<AssetFingerprint> fingerprints = new ArrayList<>();
            
            if (fingerprintFuture != null) {
                fingerprints = fingerprintFuture.get();
            }
            
            // 更新资产状态
            updateAssetStatus(asset, detections);
            
            DetectionResult result = new DetectionResult();
            result.setAssetId(assetId);
            result.setDetections(detections);
            result.setFingerprints(fingerprints);
            result.setSuccess(true);
            
            logger.info("资产检测完成: {}, 检测记录: {}, 指纹: {}", 
                       assetId, detections.size(), fingerprints.size());
            
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            logger.error("资产检测失败: {}", assetId, e);
            
            DetectionResult result = new DetectionResult();
            result.setAssetId(assetId);
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            
            return CompletableFuture.completedFuture(result);
        }
    }
    
    /**
     * 批量检测资产
     */
    @Async
    public CompletableFuture<List<DetectionResult>> detectAssets(List<Long> assetIds, boolean includeFingerprint) {
        logger.info("开始批量检测资产: {} 个", assetIds.size());
        
        List<CompletableFuture<DetectionResult>> futures = new ArrayList<>();
        
        for (Long assetId : assetIds) {
            CompletableFuture<DetectionResult> future = detectAsset(assetId, includeFingerprint);
            futures.add(future);
        }
        
        // 等待所有检测完成
        List<DetectionResult> results = new ArrayList<>();
        for (CompletableFuture<DetectionResult> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                logger.error("批量检测中的资产检测失败", e);
                DetectionResult errorResult = new DetectionResult();
                errorResult.setSuccess(false);
                errorResult.setErrorMessage(e.getMessage());
                results.add(errorResult);
            }
        }
        
        logger.info("批量检测完成: {} 个资产", results.size());
        return CompletableFuture.completedFuture(results);
    }
    
    /**
     * 检测项目下的所有资产 - 项目功能已删除
     */
    @Async
    public CompletableFuture<List<DetectionResult>> detectProjectAssets(Long projectId, boolean includeFingerprint) {
        logger.info("检测项目资产功能已删除，返回空结果: {}", projectId);
        
        // 项目功能已删除，返回空结果
        return CompletableFuture.completedFuture(new ArrayList<>());
    }
    
    /**
     * 获取资产检测历史
     */
    public Page<AssetDetection> getAssetDetectionHistory(Long assetId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        return detectionRepository.findByAssetId(assetId, pageable);
    }
    
    /**
     * 获取资产最新检测状态
     */
    public List<AssetDetection> getAssetLatestDetections(Long assetId) {
        return detectionRepository.findLatestDetectionsByAssetId(assetId);
    }
    
    /**
     * 获取资产指纹信息
     */
    public List<AssetFingerprint> getAssetFingerprints(Long assetId) {
        return fingerprintRepository.findByAssetIdAndActiveTrue(assetId);
    }
    
    /**
     * 获取检测统计信息
     */
    public DetectionStatistics getDetectionStatistics() {
        DetectionStatistics stats = new DetectionStatistics();
        
        // 总检测次数
        stats.setTotalDetections(detectionRepository.count());
        
        // 在线资产数量
        stats.setOnlineAssets(detectionRepository.countOnlineAssets());
        
        // 离线资产数量
        stats.setOfflineAssets(detectionRepository.countOfflineAssets());
        
        // 最近24小时检测次数
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        List<AssetDetection> recentDetections = detectionRepository.findByTimeRange(since, LocalDateTime.now());
        stats.setRecentDetections(recentDetections.size());
        
        // 平均响应时间
        Double avgResponseTime = detectionRepository.getAverageResponseTime(since);
        stats.setAverageResponseTime(avgResponseTime != null ? avgResponseTime : 0.0);
        
        // 检测结果分布
        List<Object[]> resultStats = detectionRepository.getDetectionStatistics(since);
        Map<String, Long> resultDistribution = new HashMap<>();
        for (Object[] row : resultStats) {
            if (row[0] != null && row[1] != null) {
                resultDistribution.put(row[0].toString(), (Long) row[1]);
            }
        }
        stats.setResultDistribution(resultDistribution);
        
        return stats;
    }
    
    /**
     * 获取指纹统计信息
     */
    public FingerprintStatistics getFingerprintStatistics() {
        FingerprintStatistics stats = new FingerprintStatistics();
        
        // 总指纹数量
        stats.setTotalFingerprints(fingerprintRepository.count());
        
        // 指纹类型分布
        List<Object[]> typeStats = fingerprintRepository.countByType();
        Map<String, Long> typeDistribution = new HashMap<>();
        for (Object[] row : typeStats) {
            if (row[0] != null && row[1] != null) {
                typeDistribution.put(row[0].toString(), (Long) row[1]);
            }
        }
        stats.setTypeDistribution(typeDistribution);

        // 技术使用统计
        List<Object[]> techStats = fingerprintRepository.getTechnologyUsageStatistics();
        Map<String, Long> technologyUsage = new HashMap<>();
        int maxEntries = Math.min(10, techStats.size()); // 只取前10个
        for (int i = 0; i < maxEntries; i++) {
            Object[] row = techStats.get(i);
            if (row[0] != null && row[1] != null) {
                technologyUsage.put(row[0].toString(), (Long) row[1]);
            }
        }
        stats.setTechnologyUsage(technologyUsage);

        // 厂商分布
        List<Object[]> vendorStats = fingerprintRepository.getVendorStatistics();
        Map<String, Long> vendorDistribution = new HashMap<>();
        int maxVendors = Math.min(10, vendorStats.size()); // 只取前10个
        for (int i = 0; i < maxVendors; i++) {
            Object[] row = vendorStats.get(i);
            if (row[0] != null && row[1] != null) {
                vendorDistribution.put(row[0].toString(), (Long) row[1]);
            }
        }
        stats.setVendorDistribution(vendorDistribution);
        
        return stats;
    }
    
    /**
     * 定时清理过期检测记录
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupOldDetections() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30); // 保留30天
            detectionRepository.deleteOldDetections(cutoffTime);
            logger.info("清理过期检测记录完成，截止时间: {}", cutoffTime);
        } catch (Exception e) {
            logger.error("清理过期检测记录失败", e);
        }
    }
    
    /**
     * 定时检测超时的检测任务
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void handleTimeoutDetections() {
        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(10); // 10分钟超时
            List<AssetDetection> timeoutDetections = detectionRepository.findTimeoutDetections(timeoutThreshold);
            
            for (AssetDetection detection : timeoutDetections) {
                detection.markAsTimeout();
                detectionRepository.save(detection);
            }
            
            if (!timeoutDetections.isEmpty()) {
                logger.info("处理超时检测任务: {} 个", timeoutDetections.size());
            }
        } catch (Exception e) {
            logger.error("处理超时检测任务失败", e);
        }
    }
    
    /**
     * 更新资产状态
     */
    private void updateAssetStatus(Asset asset, List<AssetDetection> detections) {
        try {
            // 根据检测结果更新资产状态
            boolean hasOnlineDetection = detections.stream()
                    .anyMatch(d -> d.getResult() == AssetDetection.DetectionResult.ONLINE);
            
            if (hasOnlineDetection) {
                asset.setStatus(Asset.Status.ACTIVE);
                asset.setLastScanTime(LocalDateTime.now());
            } else {
                asset.setStatus(Asset.Status.INACTIVE);
            }
            
            assetRepository.save(asset);
        } catch (Exception e) {
            logger.error("更新资产状态失败: {}", asset.getId(), e);
        }
    }
    
    // 内部类：检测结果
    public static class DetectionResult {
        private Long assetId;
        private List<AssetDetection> detections = new ArrayList<>();
        private List<AssetFingerprint> fingerprints = new ArrayList<>();
        private boolean success;
        private String errorMessage;
        
        // Getters and Setters
        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }
        
        public List<AssetDetection> getDetections() { return detections; }
        public void setDetections(List<AssetDetection> detections) { this.detections = detections; }
        
        public List<AssetFingerprint> getFingerprints() { return fingerprints; }
        public void setFingerprints(List<AssetFingerprint> fingerprints) { this.fingerprints = fingerprints; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
    
    // 内部类：检测统计信息
    public static class DetectionStatistics {
        private long totalDetections;
        private long onlineAssets;
        private long offlineAssets;
        private long recentDetections;
        private double averageResponseTime;
        private Map<String, Long> resultDistribution = new HashMap<>();
        
        // Getters and Setters
        public long getTotalDetections() { return totalDetections; }
        public void setTotalDetections(long totalDetections) { this.totalDetections = totalDetections; }
        
        public long getOnlineAssets() { return onlineAssets; }
        public void setOnlineAssets(long onlineAssets) { this.onlineAssets = onlineAssets; }
        
        public long getOfflineAssets() { return offlineAssets; }
        public void setOfflineAssets(long offlineAssets) { this.offlineAssets = offlineAssets; }
        
        public long getRecentDetections() { return recentDetections; }
        public void setRecentDetections(long recentDetections) { this.recentDetections = recentDetections; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public Map<String, Long> getResultDistribution() { return resultDistribution; }
        public void setResultDistribution(Map<String, Long> resultDistribution) { this.resultDistribution = resultDistribution; }
    }
    
    // 内部类：指纹统计信息
    public static class FingerprintStatistics {
        private long totalFingerprints;
        private Map<String, Long> typeDistribution = new HashMap<>();
        private Map<String, Long> technologyUsage = new HashMap<>();
        private Map<String, Long> vendorDistribution = new HashMap<>();
        
        // Getters and Setters
        public long getTotalFingerprints() { return totalFingerprints; }
        public void setTotalFingerprints(long totalFingerprints) { this.totalFingerprints = totalFingerprints; }
        
        public Map<String, Long> getTypeDistribution() { return typeDistribution; }
        public void setTypeDistribution(Map<String, Long> typeDistribution) { this.typeDistribution = typeDistribution; }
        
        public Map<String, Long> getTechnologyUsage() { return technologyUsage; }
        public void setTechnologyUsage(Map<String, Long> technologyUsage) { this.technologyUsage = technologyUsage; }
        
        public Map<String, Long> getVendorDistribution() { return vendorDistribution; }
        public void setVendorDistribution(Map<String, Long> vendorDistribution) { this.vendorDistribution = vendorDistribution; }
    }
}
