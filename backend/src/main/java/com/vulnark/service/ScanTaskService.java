package com.vulnark.service;

import com.vulnark.entity.*;
import com.vulnark.repository.*;
import com.vulnark.service.scan.ScanEngine;
import com.vulnark.service.scan.ScanEngineException;
import com.vulnark.service.scan.impl.InternalScanEngine;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScanTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanTaskService.class);
    
    @Autowired
    private ScanTaskRepository scanTaskRepository;
    
    @Autowired
    private ScanTaskTargetRepository scanTaskTargetRepository;
    
    @Autowired
    private ScanConfigRepository scanConfigRepository;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    @Autowired
    private InternalScanEngine internalScanEngine;
    
    // 扫描引擎映射
    private final Map<ScanTask.ScanEngine, ScanEngine> scanEngines = new HashMap<>();
    
    public ScanTaskService() {
        // 初始化时注册扫描引擎
    }
    
    @Autowired
    public void initScanEngines(InternalScanEngine internalEngine) {
        scanEngines.put(ScanTask.ScanEngine.INTERNAL, internalEngine);
        // 后续可以添加其他扫描引擎
    }
    
    /**
     * 创建扫描任务
     */
    public ScanTask createScanTask(ScanTaskRequest request, User currentUser) {
        // 验证请求参数
        validateScanTaskRequest(request);
        
        // 创建扫描任务
        ScanTask scanTask = new ScanTask();
        scanTask.setName(request.getName());
        scanTask.setDescription(request.getDescription());
        scanTask.setScanType(request.getScanType());
        scanTask.setScanEngineType(request.getScanEngine());
        scanTask.setScanTemplate(request.getScanTemplate());
        scanTask.setCreatedBy(currentUser);
        scanTask.setScheduledStartTime(request.getScheduledStartTime());
        
        // 设置项目 - 已删除项目功能
        // if (request.getProjectId() != null) {
        //     Project project = new Project();
        //     project.setId(request.getProjectId());
        //     scanTask.setProject(project);
        // }
        
        // 设置扫描配置
        if (request.getScanConfigId() != null) {
            scanTask.setScanConfigId(request.getScanConfigId());
        } else {
            // 使用默认配置
            ScanConfig defaultConfig = getDefaultScanConfig(request.getScanType(), request.getScanEngine());
            if (defaultConfig != null) {
                scanTask.setScanConfigId(defaultConfig.getId());
            }
        }
        
        scanTask = scanTaskRepository.save(scanTask);
        
        // 创建扫描目标
        List<Asset> targetAssets = getTargetAssets(request);
        createScanTaskTargets(scanTask, targetAssets);
        
        // 更新目标数量
        scanTask.setTargetCount(targetAssets.size());
        scanTask = scanTaskRepository.save(scanTask);
        
        logger.info("创建扫描任务: {} (ID: {}), 目标资产数量: {}", 
                   scanTask.getName(), scanTask.getId(), targetAssets.size());
        
        return scanTask;
    }
    
    /**
     * 启动扫描任务
     */
    @Async
    public CompletableFuture<Void> startScanTask(Long taskId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                        .orElseThrow(() -> new RuntimeException("扫描任务不存在: " + taskId));
                
                if (!scanTask.canStart()) {
                    throw new RuntimeException("扫描任务状态不允许启动: " + scanTask.getStatus());
                }
                
                // 获取扫描引擎
                ScanEngine engine = getScanEngine(scanTask.getScanEngineType());
                if (engine == null || !engine.isAvailable()) {
                    throw new RuntimeException("扫描引擎不可用: " + scanTask.getScanEngineType());
                }
                
                // 获取目标资产
                List<ScanTaskTarget> targets = scanTaskTargetRepository.findByScanTaskId(taskId);
                List<Asset> assets = targets.stream()
                        .map(target -> target.getAsset())
                        .collect(Collectors.toList());
                
                // 获取扫描配置
                ScanConfig config = getScanConfig(scanTask.getScanConfigId());
                
                // 创建外部扫描任务
                String externalTaskId = engine.createScanTask(scanTask, assets, config);
                scanTask.setExternalTaskId(externalTaskId);
                scanTask.markAsStarted();
                scanTaskRepository.save(scanTask);
                
                // 启动扫描
                engine.startScan(externalTaskId);
                
                // 监控扫描进度
                monitorScanProgress(scanTask, engine);
                
            } catch (Exception e) {
                logger.error("启动扫描任务失败: {}", taskId, e);
                handleScanTaskError(taskId, e.getMessage());
            }
        });
    }
    
    /**
     * 暂停扫描任务
     */
    public void pauseScanTask(Long taskId) throws ScanEngineException {
        ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("扫描任务不存在: " + taskId));
        
        if (!scanTask.canPause()) {
            throw new RuntimeException("扫描任务状态不允许暂停: " + scanTask.getStatus());
        }
        
        ScanEngine engine = getScanEngine(scanTask.getScanEngineType());
        if (engine != null && scanTask.getExternalTaskId() != null) {
            engine.pauseScan(scanTask.getExternalTaskId());
        }
        
        scanTask.setStatus(ScanTask.TaskStatus.PAUSED);
        scanTaskRepository.save(scanTask);
        
        logger.info("暂停扫描任务: {}", taskId);
    }
    
    /**
     * 恢复扫描任务
     */
    public void resumeScanTask(Long taskId) throws ScanEngineException {
        ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("扫描任务不存在: " + taskId));
        
        if (!scanTask.canResume()) {
            throw new RuntimeException("扫描任务状态不允许恢复: " + scanTask.getStatus());
        }
        
        ScanEngine engine = getScanEngine(scanTask.getScanEngineType());
        if (engine != null && scanTask.getExternalTaskId() != null) {
            engine.resumeScan(scanTask.getExternalTaskId());
        }
        
        scanTask.setStatus(ScanTask.TaskStatus.RUNNING);
        scanTaskRepository.save(scanTask);
        
        logger.info("恢复扫描任务: {}", taskId);
    }
    
    /**
     * 停止扫描任务
     */
    public void stopScanTask(Long taskId) throws ScanEngineException {
        ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("扫描任务不存在: " + taskId));
        
        if (!scanTask.canCancel()) {
            throw new RuntimeException("扫描任务状态不允许停止: " + scanTask.getStatus());
        }
        
        ScanEngine engine = getScanEngine(scanTask.getScanEngineType());
        if (engine != null && scanTask.getExternalTaskId() != null) {
            engine.stopScan(scanTask.getExternalTaskId());
        }
        
        scanTask.markAsCancelled();
        scanTaskRepository.save(scanTask);
        
        logger.info("停止扫描任务: {}", taskId);
    }
    
    /**
     * 获取扫描任务列表
     */
    public Page<ScanTask> getScanTasks(ScanTaskQueryRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        
        return scanTaskRepository.findByConditions(
                request.getName(),
                request.getStatus(),
                request.getScanType(),
                request.getScanEngine(),
                request.getProjectId(),
                request.getCreatedById(),
                pageable
        );
    }
    
    /**
     * 获取扫描任务详情
     */
    public ScanTask getScanTaskById(Long taskId) {
        return scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("扫描任务不存在: " + taskId));
    }
    
    /**
     * 获取扫描任务目标
     */
    public List<ScanTaskTarget> getScanTaskTargets(Long taskId) {
        return scanTaskTargetRepository.findByScanTaskId(taskId);
    }
    
    /**
     * 获取扫描任务的漏洞
     */
    public List<Vulnerability> getScanTaskVulnerabilities(Long taskId) {
        return vulnerabilityRepository.findByScanTaskIdAndDeletedFalse(taskId);
    }
    
    /**
     * 删除扫描任务
     */
    public void deleteScanTask(Long taskId) {
        ScanTask scanTask = scanTaskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("扫描任务不存在: " + taskId));
        
        // 如果任务正在运行，先停止
        if (scanTask.isRunning()) {
            try {
                stopScanTask(taskId);
            } catch (ScanEngineException e) {
                logger.warn("停止扫描任务失败: {}", taskId, e);
            }
        }
        
        // 软删除
        scanTask.setDeleted(true);
        scanTaskRepository.save(scanTask);
        
        logger.info("删除扫描任务: {}", taskId);
    }
    
    /**
     * 获取扫描统计信息
     */
    public ScanStatistics getScanStatistics() {
        ScanStatistics stats = new ScanStatistics();
        
        // 总任务数
        stats.setTotalTasks(scanTaskRepository.countAllTasks());
        
        // 各状态任务数
        List<Object[]> statusStats = scanTaskRepository.getStatusStatistics();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusStats) {
            if (row[0] != null && row[1] != null) {
                statusMap.put(row[0].toString(), (Long) row[1]);
            }
        }
        stats.setStatusDistribution(statusMap);
        
        // 成功率
        Double successRate = scanTaskRepository.getSuccessRate();
        stats.setSuccessRate(successRate != null ? successRate : 0.0);
        
        // 平均扫描时间
        Double avgDuration = scanTaskRepository.getAverageScanDuration();
        stats.setAverageScanDuration(avgDuration != null ? avgDuration : 0.0);
        
        return stats;
    }
    
    /**
     * 定时检查扫描任务状态
     */
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void checkScanTaskStatus() {
        try {
            List<ScanTask> runningTasks = scanTaskRepository.findRunningTasks();
            
            for (ScanTask task : runningTasks) {
                try {
                    updateScanTaskStatus(task);
                } catch (Exception e) {
                    logger.error("更新扫描任务状态失败: {}", task.getId(), e);
                }
            }
            
        } catch (Exception e) {
            logger.error("检查扫描任务状态失败", e);
        }
    }
    
    /**
     * 定时清理过期任务
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupOldTasks() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
            List<ScanTask> oldTasks = scanTaskRepository.findTasksToCleanup(cutoffTime);
            
            for (ScanTask task : oldTasks) {
                // 清理扫描引擎资源
                try {
                    ScanEngine engine = getScanEngine(task.getScanEngineType());
                    if (engine != null && task.getExternalTaskId() != null) {
                        engine.cleanup(task.getExternalTaskId());
                    }
                } catch (Exception e) {
                    logger.warn("清理扫描引擎资源失败: {}", task.getId(), e);
                }
                
                // 软删除任务
                task.setDeleted(true);
                scanTaskRepository.save(task);
            }
            
            logger.info("清理过期扫描任务: {} 个", oldTasks.size());
            
        } catch (Exception e) {
            logger.error("清理过期任务失败", e);
        }
    }
    
    // 私有方法
    private void validateScanTaskRequest(ScanTaskRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        
        if (request.getScanType() == null) {
            throw new IllegalArgumentException("扫描类型不能为空");
        }
        
        if (request.getScanEngine() == null) {
            throw new IllegalArgumentException("扫描引擎不能为空");
        }
        
        if (request.getAssetIds() == null || request.getAssetIds().isEmpty()) {
            throw new IllegalArgumentException("目标资产不能为空");
        }
    }
    
    private List<Asset> getTargetAssets(ScanTaskRequest request) {
        List<Asset> assets = new ArrayList<>();
        
        if (request.getAssetIds() != null && !request.getAssetIds().isEmpty()) {
            assets = assetRepository.findByIdInAndDeletedFalse(request.getAssetIds());
        }
        
        // 项目功能已删除，忽略项目资产
        // if (request.getProjectId() != null && request.isIncludeProjectAssets()) {
        //     List<Asset> projectAssets = assetRepository.findByProjectIdAndDeletedFalse(request.getProjectId());
        //     assets.addAll(projectAssets);
        // }
        
        // 去重
        return assets.stream().distinct().collect(Collectors.toList());
    }
    
    private void createScanTaskTargets(ScanTask scanTask, List<Asset> assets) {
        for (Asset asset : assets) {
            ScanTaskTarget target = new ScanTaskTarget(scanTask, asset);
            scanTaskTargetRepository.save(target);
        }
    }
    
    private ScanEngine getScanEngine(ScanTask.ScanEngine engineType) {
        return scanEngines.get(engineType);
    }
    
    private ScanConfig getScanConfig(Long configId) {
        if (configId == null) {
            return null;
        }
        return scanConfigRepository.findByIdAndDeletedFalse(configId).orElse(null);
    }
    
    private ScanConfig getDefaultScanConfig(ScanTask.ScanType scanType, ScanTask.ScanEngine scanEngine) {
        List<ScanConfig> configs = scanConfigRepository.findByScanTypeAndEnabledTrueAndDeletedFalse(scanType);
        return configs.stream()
                .filter(config -> config.getScanEngine() == scanEngine)
                .filter(ScanConfig::getIsDefault)
                .findFirst()
                .orElse(null);
    }
    
    private void monitorScanProgress(ScanTask scanTask, ScanEngine engine) {
        // 这里可以实现更复杂的进度监控逻辑
        // 目前简化处理
    }
    
    private void updateScanTaskStatus(ScanTask task) throws ScanEngineException {
        ScanEngine engine = getScanEngine(task.getScanEngineType());
        if (engine == null || task.getExternalTaskId() == null) {
            return;
        }
        
        ScanEngine.ScanStatus status = engine.getScanStatus(task.getExternalTaskId());
        
        // 更新任务状态
        task.setStatus(status.getStatus());
        task.updateProgress(status.getProgress());
        
        // 更新漏洞统计
        task.setTotalVulnerabilityCount((int) status.getVulnerabilityCount());
        task.setHighRiskCount((int) status.getHighRiskCount());
        task.setMediumRiskCount((int) status.getMediumRiskCount());
        task.setLowRiskCount((int) status.getLowRiskCount());
        task.setInfoRiskCount((int) status.getInfoRiskCount());
        
        scanTaskRepository.save(task);
        
        // 如果任务完成，处理结果
        if (task.isCompleted()) {
            processScanResult(task, engine);
        }
    }
    
    private void processScanResult(ScanTask task, ScanEngine engine) {
        try {
            ScanEngine.ScanResult result = engine.getScanResult(task.getExternalTaskId());
            
            // 保存漏洞到数据库
            if (result.getVulnerabilities() != null) {
                for (Vulnerability vulnerability : result.getVulnerabilities()) {
                    // vulnerability.setProjectId(task.getProject() != null ? task.getProject().getId() : null); // 已删除项目功能
                    vulnerability.setScanTaskId(task.getId());
                    vulnerabilityRepository.save(vulnerability);
                }
            }
            
            // 清理扫描引擎资源
            engine.cleanup(task.getExternalTaskId());
            
        } catch (Exception e) {
            logger.error("处理扫描结果失败: {}", task.getId(), e);
        }
    }
    
    private void handleScanTaskError(Long taskId, String errorMessage) {
        try {
            ScanTask scanTask = scanTaskRepository.findById(taskId).orElse(null);
            if (scanTask != null) {
                scanTask.markAsFailed(errorMessage);
                scanTaskRepository.save(scanTask);
            }
        } catch (Exception e) {
            logger.error("处理扫描任务错误失败: {}", taskId, e);
        }
    }
    
    // 内部类
    public static class ScanTaskRequest {
        private String name;
        private String description;
        private ScanTask.ScanType scanType;
        private ScanTask.ScanEngine scanEngine;
        private ScanTask.ScanTemplate scanTemplate;
        private Long projectId;
        private Long scanConfigId;
        private List<Long> assetIds;
        private boolean includeProjectAssets = false;
        private LocalDateTime scheduledStartTime;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public ScanTask.ScanType getScanType() { return scanType; }
        public void setScanType(ScanTask.ScanType scanType) { this.scanType = scanType; }
        
        public ScanTask.ScanEngine getScanEngine() { return scanEngine; }
        public void setScanEngine(ScanTask.ScanEngine scanEngine) { this.scanEngine = scanEngine; }
        
        public ScanTask.ScanTemplate getScanTemplate() { return scanTemplate; }
        public void setScanTemplate(ScanTask.ScanTemplate scanTemplate) { this.scanTemplate = scanTemplate; }
        
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        
        public Long getScanConfigId() { return scanConfigId; }
        public void setScanConfigId(Long scanConfigId) { this.scanConfigId = scanConfigId; }
        
        public List<Long> getAssetIds() { return assetIds; }
        public void setAssetIds(List<Long> assetIds) { this.assetIds = assetIds; }
        
        public boolean isIncludeProjectAssets() { return includeProjectAssets; }
        public void setIncludeProjectAssets(boolean includeProjectAssets) { this.includeProjectAssets = includeProjectAssets; }
        
        public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
        public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }
    }
    
    public static class ScanTaskQueryRequest {
        private String name;
        private ScanTask.TaskStatus status;
        private ScanTask.ScanType scanType;
        private ScanTask.ScanEngine scanEngine;
        private Long projectId;
        private Long createdById;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public ScanTask.TaskStatus getStatus() { return status; }
        public void setStatus(ScanTask.TaskStatus status) { this.status = status; }
        
        public ScanTask.ScanType getScanType() { return scanType; }
        public void setScanType(ScanTask.ScanType scanType) { this.scanType = scanType; }
        
        public ScanTask.ScanEngine getScanEngine() { return scanEngine; }
        public void setScanEngine(ScanTask.ScanEngine scanEngine) { this.scanEngine = scanEngine; }
        
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        
        public Long getCreatedById() { return createdById; }
        public void setCreatedById(Long createdById) { this.createdById = createdById; }
    }
    
    public static class ScanStatistics {
        private long totalTasks;
        private Map<String, Long> statusDistribution = new HashMap<>();
        private double successRate;
        private double averageScanDuration;
        
        // Getters and Setters
        public long getTotalTasks() { return totalTasks; }
        public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
        
        public Map<String, Long> getStatusDistribution() { return statusDistribution; }
        public void setStatusDistribution(Map<String, Long> statusDistribution) { this.statusDistribution = statusDistribution; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getAverageScanDuration() { return averageScanDuration; }
        public void setAverageScanDuration(double averageScanDuration) { this.averageScanDuration = averageScanDuration; }
    }
}
