package com.vulnark.service.scan.impl;

import com.vulnark.entity.*;
import com.vulnark.service.scan.ScanEngine;
import com.vulnark.service.scan.ScanEngineException;
import com.vulnark.service.detection.AssetDetectionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内置扫描引擎实现
 * 基于现有的资产检测功能，提供基础的漏洞扫描能力
 */
@Service
public class InternalScanEngine implements ScanEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalScanEngine.class);
    
    @Autowired
    private AssetDetectionEngine detectionEngine;
    
    // 存储正在进行的扫描任务
    private final Map<String, ScanTaskContext> activeTasks = new ConcurrentHashMap<>();
    
    @Override
    public ScanTask.ScanEngine getEngineType() {
        return ScanTask.ScanEngine.INTERNAL;
    }
    
    @Override
    public String getEngineName() {
        return "VulnArk Internal Scanner";
    }
    
    @Override
    public String getEngineVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean isAvailable() {
        return true; // 内置引擎始终可用
    }
    
    @Override
    public List<ScanTask.ScanType> getSupportedScanTypes() {
        return Arrays.asList(
            ScanTask.ScanType.PORT_SCAN,
            ScanTask.ScanType.SYSTEM_SCAN,
            ScanTask.ScanType.COMPREHENSIVE_SCAN
        );
    }
    
    @Override
    public String createScanTask(ScanTask scanTask, List<Asset> assets, ScanConfig config) throws ScanEngineException {
        try {
            String taskId = "internal_" + scanTask.getId() + "_" + System.currentTimeMillis();
            
            ScanTaskContext context = new ScanTaskContext();
            context.setTaskId(taskId);
            context.setScanTask(scanTask);
            context.setAssets(assets);
            context.setConfig(config);
            context.setStatus(ScanTask.TaskStatus.CREATED);
            context.setProgress(0);
            context.setCreatedTime(LocalDateTime.now());
            
            activeTasks.put(taskId, context);
            
            logger.info("创建内置扫描任务: {} (资产数量: {})", taskId, assets.size());
            return taskId;
            
        } catch (Exception e) {
            throw new ScanEngineException("INTERNAL", "CREATE_TASK_FAILED", 
                "创建扫描任务失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void startScan(String externalTaskId) throws ScanEngineException {
        ScanTaskContext context = activeTasks.get(externalTaskId);
        if (context == null) {
            throw new ScanEngineException("INTERNAL", "TASK_NOT_FOUND", 
                "扫描任务不存在: " + externalTaskId);
        }
        
        try {
            context.setStatus(ScanTask.TaskStatus.RUNNING);
            context.setStartTime(LocalDateTime.now());
            
            // 异步执行扫描
            CompletableFuture.runAsync(() -> {
                try {
                    performScan(context);
                } catch (Exception e) {
                    logger.error("扫描任务执行失败: {}", externalTaskId, e);
                    context.setStatus(ScanTask.TaskStatus.FAILED);
                    context.setErrorMessage(e.getMessage());
                }
            });
            
            logger.info("启动内置扫描任务: {}", externalTaskId);
            
        } catch (Exception e) {
            throw new ScanEngineException("INTERNAL", "START_SCAN_FAILED", 
                "启动扫描失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void pauseScan(String externalTaskId) throws ScanEngineException {
        ScanTaskContext context = activeTasks.get(externalTaskId);
        if (context == null) {
            throw new ScanEngineException("INTERNAL", "TASK_NOT_FOUND", 
                "扫描任务不存在: " + externalTaskId);
        }
        
        context.setStatus(ScanTask.TaskStatus.PAUSED);
        context.setPaused(true);
        logger.info("暂停内置扫描任务: {}", externalTaskId);
    }
    
    @Override
    public void resumeScan(String externalTaskId) throws ScanEngineException {
        ScanTaskContext context = activeTasks.get(externalTaskId);
        if (context == null) {
            throw new ScanEngineException("INTERNAL", "TASK_NOT_FOUND", 
                "扫描任务不存在: " + externalTaskId);
        }
        
        context.setStatus(ScanTask.TaskStatus.RUNNING);
        context.setPaused(false);
        logger.info("恢复内置扫描任务: {}", externalTaskId);
    }
    
    @Override
    public void stopScan(String externalTaskId) throws ScanEngineException {
        ScanTaskContext context = activeTasks.get(externalTaskId);
        if (context == null) {
            throw new ScanEngineException("INTERNAL", "TASK_NOT_FOUND", 
                "扫描任务不存在: " + externalTaskId);
        }
        
        context.setStatus(ScanTask.TaskStatus.CANCELLED);
        context.setStopped(true);
        logger.info("停止内置扫描任务: {}", externalTaskId);
    }
    
    @Override
    public ScanStatus getScanStatus(String externalTaskId) throws ScanEngineException {
        ScanTaskContext context = activeTasks.get(externalTaskId);
        if (context == null) {
            throw new ScanEngineException("INTERNAL", "TASK_NOT_FOUND", 
                "扫描任务不存在: " + externalTaskId);
        }
        
        ScanStatus status = new ScanStatus();
        status.setStatus(context.getStatus());
        status.setProgress(context.getProgress());
        status.setMessage(context.getStatusMessage());
        status.setVulnerabilityCount(context.getVulnerabilities().size());
        
        // 统计各风险等级漏洞数量
        long highCount = context.getVulnerabilities().stream()
            .mapToLong(v -> v.getRiskLevel() == Vulnerability.RiskLevel.HIGH ? 1 : 0).sum();
        long mediumCount = context.getVulnerabilities().stream()
            .mapToLong(v -> v.getRiskLevel() == Vulnerability.RiskLevel.MEDIUM ? 1 : 0).sum();
        long lowCount = context.getVulnerabilities().stream()
            .mapToLong(v -> v.getRiskLevel() == Vulnerability.RiskLevel.LOW ? 1 : 0).sum();
        
        status.setHighRiskCount(highCount);
        status.setMediumRiskCount(mediumCount);
        status.setLowRiskCount(lowCount);
        
        return status;
    }
    
    @Override
    public int getScanProgress(String externalTaskId) throws ScanEngineException {
        ScanTaskContext context = activeTasks.get(externalTaskId);
        if (context == null) {
            throw new ScanEngineException("INTERNAL", "TASK_NOT_FOUND", 
                "扫描任务不存在: " + externalTaskId);
        }
        
        return context.getProgress();
    }
    
    @Override
    public ScanResult getScanResult(String externalTaskId) throws ScanEngineException {
        ScanTaskContext context = activeTasks.get(externalTaskId);
        if (context == null) {
            throw new ScanEngineException("INTERNAL", "TASK_NOT_FOUND", 
                "扫描任务不存在: " + externalTaskId);
        }
        
        ScanResult result = new ScanResult();
        result.setTaskId(externalTaskId);
        result.setStatus(context.getStatus());
        result.setVulnerabilities(context.getVulnerabilities());
        result.setSummary(generateSummary(context));
        
        if (context.getStartTime() != null && context.getEndTime() != null) {
            result.setScanDuration(
                java.time.Duration.between(context.getStartTime(), context.getEndTime()).toMinutes());
        }
        
        return result;
    }
    
    @Override
    public CompletableFuture<ScanResult> getScanResultAsync(String externalTaskId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getScanResult(externalTaskId);
            } catch (ScanEngineException e) {
                throw new RuntimeException(e);
            }
        });
    }
    
    @Override
    public List<Vulnerability> parseResultFile(String filePath, String fileType) throws ScanEngineException {
        // 内置引擎不需要解析外部文件
        return new ArrayList<>();
    }
    
    @Override
    public ValidationResult validateConfig(ScanConfig config) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        if (config.getTimeoutMinutes() == null || config.getTimeoutMinutes() <= 0) {
            errors.add("超时时间必须大于0");
        }
        
        if (config.getMaxConcurrency() == null || config.getMaxConcurrency() <= 0) {
            errors.add("最大并发数必须大于0");
        }
        
        if (config.getTimeoutMinutes() != null && config.getTimeoutMinutes() > 480) {
            warnings.add("扫描超时时间过长，建议不超过8小时");
        }
        
        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
    
    @Override
    public ScanConfig getDefaultConfig(ScanTask.ScanType scanType) {
        ScanConfig config = new ScanConfig();
        config.setName("默认" + scanType.getDescription() + "配置");
        config.setScanEngine(ScanTask.ScanEngine.INTERNAL);
        config.setScanType(scanType);
        config.setTimeoutMinutes(60);
        config.setMaxConcurrency(5);
        config.setEnabled(true);
        
        switch (scanType) {
            case PORT_SCAN:
                config.setPortRange("1-1000");
                config.setServiceDetection(true);
                config.setVulnerabilityScan(false);
                break;
            case SYSTEM_SCAN:
                config.setPortRange("1-65535");
                config.setServiceDetection(true);
                config.setVulnerabilityScan(true);
                config.setOsDetection(true);
                break;
            case COMPREHENSIVE_SCAN:
                config.setPortRange("1-65535");
                config.setServiceDetection(true);
                config.setVulnerabilityScan(true);
                config.setOsDetection(true);
                config.setWebAppScan(true);
                break;
        }
        
        return config;
    }
    
    @Override
    public void cleanup(String externalTaskId) {
        activeTasks.remove(externalTaskId);
        logger.info("清理内置扫描任务: {}", externalTaskId);
    }
    
    /**
     * 执行扫描
     */
    private void performScan(ScanTaskContext context) {
        try {
            List<Asset> assets = context.getAssets();
            int totalAssets = assets.size();
            int completedAssets = 0;
            
            context.setStatusMessage("开始扫描资产...");
            
            for (Asset asset : assets) {
                if (context.isStopped()) {
                    break;
                }
                
                // 等待暂停状态
                while (context.isPaused() && !context.isStopped()) {
                    Thread.sleep(1000);
                }
                
                if (context.isStopped()) {
                    break;
                }
                
                // 扫描单个资产
                scanAsset(context, asset);
                
                completedAssets++;
                int progress = (completedAssets * 100) / totalAssets;
                context.setProgress(progress);
                context.setStatusMessage(String.format("已扫描 %d/%d 个资产", completedAssets, totalAssets));
                
                // 模拟扫描延迟
                Thread.sleep(1000);
            }
            
            if (context.isStopped()) {
                context.setStatus(ScanTask.TaskStatus.CANCELLED);
                context.setStatusMessage("扫描已取消");
            } else {
                context.setStatus(ScanTask.TaskStatus.COMPLETED);
                context.setProgress(100);
                context.setStatusMessage("扫描完成");
            }
            
            context.setEndTime(LocalDateTime.now());
            
        } catch (Exception e) {
            logger.error("扫描执行失败", e);
            context.setStatus(ScanTask.TaskStatus.FAILED);
            context.setErrorMessage(e.getMessage());
            context.setEndTime(LocalDateTime.now());
        }
    }
    
    /**
     * 扫描单个资产
     */
    private void scanAsset(ScanTaskContext context, Asset asset) {
        try {
            // 基于资产检测结果生成模拟漏洞
            List<Vulnerability> vulnerabilities = generateMockVulnerabilities(asset, context.getScanTask());
            context.getVulnerabilities().addAll(vulnerabilities);
            
        } catch (Exception e) {
            logger.error("扫描资产失败: {}", asset.getName(), e);
        }
    }
    
    /**
     * 生成模拟漏洞（用于演示）
     */
    private List<Vulnerability> generateMockVulnerabilities(Asset asset, ScanTask scanTask) {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        
        // 根据资产类型生成不同的漏洞
        if (asset.getType() == Asset.AssetType.WEB_APPLICATION) {
            // Web应用漏洞
            if (Math.random() > 0.7) {
                Vulnerability vuln = createMockVulnerability(
                    "SQL注入漏洞", 
                    "在登录页面发现SQL注入漏洞",
                    Vulnerability.RiskLevel.HIGH,
                    asset, scanTask
                );
                vulnerabilities.add(vuln);
            }
            
            if (Math.random() > 0.5) {
                Vulnerability vuln = createMockVulnerability(
                    "跨站脚本(XSS)漏洞", 
                    "在搜索功能中发现反射型XSS漏洞",
                    Vulnerability.RiskLevel.MEDIUM,
                    asset, scanTask
                );
                vulnerabilities.add(vuln);
            }
        }
        
        if (asset.getType() == Asset.AssetType.SERVER) {
            // 服务器漏洞
            if (Math.random() > 0.8) {
                Vulnerability vuln = createMockVulnerability(
                    "SSH弱密码", 
                    "SSH服务使用弱密码，存在暴力破解风险",
                    Vulnerability.RiskLevel.HIGH,
                    asset, scanTask
                );
                vulnerabilities.add(vuln);
            }
            
            if (Math.random() > 0.6) {
                Vulnerability vuln = createMockVulnerability(
                    "未授权访问", 
                    "发现未授权的管理接口",
                    Vulnerability.RiskLevel.MEDIUM,
                    asset, scanTask
                );
                vulnerabilities.add(vuln);
            }
        }
        
        return vulnerabilities;
    }
    
    /**
     * 创建模拟漏洞
     */
    private Vulnerability createMockVulnerability(String title, String description, 
                                                 Vulnerability.RiskLevel riskLevel, 
                                                 Asset asset, ScanTask scanTask) {
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setTitle(title);
        vulnerability.setDescription(description);
        vulnerability.setRiskLevel(riskLevel);
        vulnerability.setStatus(Vulnerability.Status.OPEN);
        vulnerability.setAssetId(asset.getId());
        vulnerability.setScanTaskId(scanTask.getId());
        vulnerability.setSource(Vulnerability.VulnerabilitySource.SCAN);
        vulnerability.setScanEngine("INTERNAL");
        vulnerability.setDiscoveredDate(LocalDateTime.now().toLocalDate());
        
        return vulnerability;
    }
    
    /**
     * 生成扫描摘要
     */
    private String generateSummary(ScanTaskContext context) {
        int totalVulns = context.getVulnerabilities().size();
        long highCount = context.getVulnerabilities().stream()
            .mapToLong(v -> v.getRiskLevel() == Vulnerability.RiskLevel.HIGH ? 1 : 0).sum();
        long mediumCount = context.getVulnerabilities().stream()
            .mapToLong(v -> v.getRiskLevel() == Vulnerability.RiskLevel.MEDIUM ? 1 : 0).sum();
        long lowCount = context.getVulnerabilities().stream()
            .mapToLong(v -> v.getRiskLevel() == Vulnerability.RiskLevel.LOW ? 1 : 0).sum();
        
        return String.format("扫描完成，共发现 %d 个漏洞：高危 %d 个，中危 %d 个，低危 %d 个", 
                           totalVulns, highCount, mediumCount, lowCount);
    }
    
    /**
     * 扫描任务上下文
     */
    private static class ScanTaskContext {
        private String taskId;
        private ScanTask scanTask;
        private List<Asset> assets;
        private ScanConfig config;
        private ScanTask.TaskStatus status;
        private int progress;
        private String statusMessage;
        private String errorMessage;
        private LocalDateTime createdTime;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean paused;
        private boolean stopped;
        private List<Vulnerability> vulnerabilities = new ArrayList<>();
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        
        public ScanTask getScanTask() { return scanTask; }
        public void setScanTask(ScanTask scanTask) { this.scanTask = scanTask; }
        
        public List<Asset> getAssets() { return assets; }
        public void setAssets(List<Asset> assets) { this.assets = assets; }
        
        public ScanConfig getConfig() { return config; }
        public void setConfig(ScanConfig config) { this.config = config; }
        
        public ScanTask.TaskStatus getStatus() { return status; }
        public void setStatus(ScanTask.TaskStatus status) { this.status = status; }
        
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        
        public String getStatusMessage() { return statusMessage; }
        public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public boolean isPaused() { return paused; }
        public void setPaused(boolean paused) { this.paused = paused; }
        
        public boolean isStopped() { return stopped; }
        public void setStopped(boolean stopped) { this.stopped = stopped; }
        
        public List<Vulnerability> getVulnerabilities() { return vulnerabilities; }
        public void setVulnerabilities(List<Vulnerability> vulnerabilities) { this.vulnerabilities = vulnerabilities; }
    }
}
