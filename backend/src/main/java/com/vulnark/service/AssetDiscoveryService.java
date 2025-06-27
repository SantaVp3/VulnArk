package com.vulnark.service;

import com.vulnark.entity.AssetDiscoveryResult;
import com.vulnark.entity.AssetDiscoveryTask;
import com.vulnark.repository.AssetDiscoveryResultRepository;
import com.vulnark.repository.AssetDiscoveryTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 资产发现服务
 */
@Service
@Transactional
public class AssetDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(AssetDiscoveryService.class);

    @Autowired
    private AssetDiscoveryTaskRepository taskRepository;

    @Autowired
    private AssetDiscoveryResultRepository resultRepository;

    @Autowired
    private AssetDiscoveryEngine discoveryEngine;

    @Autowired
    private AssetCorrelationService correlationService;

    /**
     * 创建发现任务
     */
    public AssetDiscoveryTask createTask(AssetDiscoveryTask task, Long userId) {
        task.setCreatedBy(userId);
        task.setStatus(AssetDiscoveryTask.TaskStatus.PENDING);
        task.setProgress(BigDecimal.ZERO);
        
        // 设置下次执行时间
        if (task.getScheduleType() != AssetDiscoveryTask.ScheduleType.ONCE) {
            task.setNextRunTime(calculateNextRunTime(task));
        }
        
        AssetDiscoveryTask savedTask = taskRepository.save(task);
        logger.info("创建资产发现任务: {} (ID: {})", savedTask.getName(), savedTask.getId());
        
        return savedTask;
    }

    /**
     * 更新任务
     */
    public AssetDiscoveryTask updateTask(Long taskId, AssetDiscoveryTask taskUpdate) {
        Optional<AssetDiscoveryTask> optionalTask = taskRepository.findByIdAndDeletedFalse(taskId);
        if (optionalTask.isEmpty()) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        
        AssetDiscoveryTask existingTask = optionalTask.get();
        
        // 更新可修改的字段
        existingTask.setName(taskUpdate.getName());
        existingTask.setDescription(taskUpdate.getDescription());
        existingTask.setTargetType(taskUpdate.getTargetType());
        existingTask.setTargets(taskUpdate.getTargets());
        existingTask.setScanType(taskUpdate.getScanType());
        existingTask.setScanPorts(taskUpdate.getScanPorts());
        existingTask.setScanOptions(taskUpdate.getScanOptions());
        existingTask.setScheduleType(taskUpdate.getScheduleType());
        existingTask.setScheduleConfig(taskUpdate.getScheduleConfig());
        
        // 重新计算下次执行时间
        if (existingTask.getScheduleType() != AssetDiscoveryTask.ScheduleType.ONCE) {
            existingTask.setNextRunTime(calculateNextRunTime(existingTask));
        }
        
        return taskRepository.save(existingTask);
    }

    /**
     * 删除任务（软删除）
     */
    public void deleteTask(Long taskId) {
        Optional<AssetDiscoveryTask> optionalTask = taskRepository.findByIdAndDeletedFalse(taskId);
        if (optionalTask.isEmpty()) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        
        AssetDiscoveryTask task = optionalTask.get();
        task.setDeleted(true);
        taskRepository.save(task);
        
        logger.info("删除资产发现任务: {} (ID: {})", task.getName(), taskId);
    }

    /**
     * 获取任务详情
     */
    public AssetDiscoveryTask getTaskById(Long taskId) {
        return taskRepository.findByIdAndDeletedFalse(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));
    }

    /**
     * 分页查询任务
     */
    public Page<AssetDiscoveryTask> getTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return taskRepository.findByDeletedFalse(pageable);
    }

    /**
     * 执行任务
     */
    @Async
    public CompletableFuture<Void> executeTask(Long taskId) {
        return CompletableFuture.runAsync(() -> {
            try {
                AssetDiscoveryTask task = getTaskById(taskId);
                
                // 更新任务状态
                updateTaskStatus(taskId, AssetDiscoveryTask.TaskStatus.RUNNING);
                task.setLastRunTime(LocalDateTime.now());
                taskRepository.save(task);
                
                logger.info("开始执行资产发现任务: {} (ID: {})", task.getName(), taskId);
                
                List<AssetDiscoveryResult> results;
                
                // 根据扫描类型执行相应的发现逻辑
                switch (task.getScanType()) {
                    case PING_SWEEP:
                        results = discoveryEngine.performHostDiscovery(task);
                        break;
                    case PORT_SCAN:
                        results = discoveryEngine.performPortScan(task);
                        break;
                    case SERVICE_DETECTION:
                        results = discoveryEngine.performServiceDetection(task);
                        break;
                    case FULL_SCAN:
                        results = discoveryEngine.performFullScan(task);
                        break;
                    default:
                        throw new RuntimeException("不支持的扫描类型: " + task.getScanType());
                }
                
                // 保存发现结果
                for (AssetDiscoveryResult result : results) {
                    resultRepository.save(result);
                }
                
                // 执行资产关联
                correlationService.correlateResults(results);
                
                // 更新任务状态和进度
                updateTaskStatus(taskId, AssetDiscoveryTask.TaskStatus.COMPLETED);
                updateTaskProgress(taskId, BigDecimal.valueOf(100));
                
                // 设置下次执行时间
                if (task.getScheduleType() != AssetDiscoveryTask.ScheduleType.ONCE) {
                    task.setNextRunTime(calculateNextRunTime(task));
                    taskRepository.save(task);
                }
                
                logger.info("资产发现任务执行完成: {} (ID: {}), 发现 {} 个结果", 
                           task.getName(), taskId, results.size());
                
            } catch (Exception e) {
                logger.error("资产发现任务执行失败: {}", e.getMessage(), e);
                updateTaskStatus(taskId, AssetDiscoveryTask.TaskStatus.FAILED);
            }
        });
    }

    /**
     * 取消任务
     */
    public void cancelTask(Long taskId) {
        updateTaskStatus(taskId, AssetDiscoveryTask.TaskStatus.CANCELLED);
        logger.info("取消资产发现任务: {}", taskId);
    }

    /**
     * 获取任务结果
     */
    public Page<AssetDiscoveryResult> getTaskResults(Long taskId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return resultRepository.findByTaskId(taskId, pageable);
    }

    /**
     * 获取任务统计信息
     */
    public TaskStatistics getTaskStatistics(Long taskId) {
        TaskStatistics stats = new TaskStatistics();
        
        stats.setTotalResults(resultRepository.countByTaskId(taskId));
        stats.setAliveHosts(resultRepository.countAliveHostsByTask(taskId));
        
        List<Object[]> correlationStats = resultRepository.countByCorrelationStatusAndTask(taskId);
        for (Object[] stat : correlationStats) {
            AssetDiscoveryResult.CorrelationStatus status = (AssetDiscoveryResult.CorrelationStatus) stat[0];
            Long count = (Long) stat[1];
            
            switch (status) {
                case NEW:
                    stats.setNewAssets(count);
                    break;
                case MATCHED:
                    stats.setMatchedAssets(count);
                    break;
                case UPDATED:
                    stats.setUpdatedAssets(count);
                    break;
                case IGNORED:
                    stats.setIgnoredAssets(count);
                    break;
            }
        }
        
        return stats;
    }

    /**
     * 获取系统统计信息
     */
    public SystemStatistics getSystemStatistics() {
        SystemStatistics stats = new SystemStatistics();
        
        List<Object[]> statusStats = taskRepository.countTasksByStatus();
        for (Object[] stat : statusStats) {
            AssetDiscoveryTask.TaskStatus status = (AssetDiscoveryTask.TaskStatus) stat[0];
            Long count = (Long) stat[1];
            
            switch (status) {
                case PENDING:
                    stats.setPendingTasks(count);
                    break;
                case RUNNING:
                    stats.setRunningTasks(count);
                    break;
                case COMPLETED:
                    stats.setCompletedTasks(count);
                    break;
                case FAILED:
                    stats.setFailedTasks(count);
                    break;
                case CANCELLED:
                    stats.setCancelledTasks(count);
                    break;
            }
        }
        
        // 最近24小时创建的任务数
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        stats.setRecentTasks(taskRepository.countRecentTasks(yesterday));
        
        // 新发现的资产数
        List<AssetDiscoveryResult> newAssets = resultRepository.findNewDiscoveredAssets(PageRequest.of(0, 1000));
        stats.setNewDiscoveredAssets((long) newAssets.size());
        
        return stats;
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(Long taskId, AssetDiscoveryTask.TaskStatus status) {
        taskRepository.updateTaskStatus(taskId, status, LocalDateTime.now());
    }

    /**
     * 更新任务进度
     */
    private void updateTaskProgress(Long taskId, BigDecimal progress) {
        taskRepository.updateTaskProgress(taskId, progress, LocalDateTime.now());
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextRunTime(AssetDiscoveryTask task) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (task.getScheduleType()) {
            case DAILY:
                return now.plusDays(1);
            case WEEKLY:
                return now.plusWeeks(1);
            case MONTHLY:
                return now.plusMonths(1);
            case CUSTOM:
                // 这里可以解析自定义的调度配置
                return now.plusHours(1); // 默认1小时后
            default:
                return null;
        }
    }

    /**
     * 任务统计信息
     */
    public static class TaskStatistics {
        private long totalResults;
        private long aliveHosts;
        private long newAssets;
        private long matchedAssets;
        private long updatedAssets;
        private long ignoredAssets;

        // Getter and Setter methods
        public long getTotalResults() { return totalResults; }
        public void setTotalResults(long totalResults) { this.totalResults = totalResults; }
        public long getAliveHosts() { return aliveHosts; }
        public void setAliveHosts(long aliveHosts) { this.aliveHosts = aliveHosts; }
        public long getNewAssets() { return newAssets; }
        public void setNewAssets(long newAssets) { this.newAssets = newAssets; }
        public long getMatchedAssets() { return matchedAssets; }
        public void setMatchedAssets(long matchedAssets) { this.matchedAssets = matchedAssets; }
        public long getUpdatedAssets() { return updatedAssets; }
        public void setUpdatedAssets(long updatedAssets) { this.updatedAssets = updatedAssets; }
        public long getIgnoredAssets() { return ignoredAssets; }
        public void setIgnoredAssets(long ignoredAssets) { this.ignoredAssets = ignoredAssets; }
    }

    /**
     * 系统统计信息
     */
    public static class SystemStatistics {
        private long pendingTasks;
        private long runningTasks;
        private long completedTasks;
        private long failedTasks;
        private long cancelledTasks;
        private long recentTasks;
        private long newDiscoveredAssets;

        // Getter and Setter methods
        public long getPendingTasks() { return pendingTasks; }
        public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }
        public long getRunningTasks() { return runningTasks; }
        public void setRunningTasks(long runningTasks) { this.runningTasks = runningTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
        public long getFailedTasks() { return failedTasks; }
        public void setFailedTasks(long failedTasks) { this.failedTasks = failedTasks; }
        public long getCancelledTasks() { return cancelledTasks; }
        public void setCancelledTasks(long cancelledTasks) { this.cancelledTasks = cancelledTasks; }
        public long getRecentTasks() { return recentTasks; }
        public void setRecentTasks(long recentTasks) { this.recentTasks = recentTasks; }
        public long getNewDiscoveredAssets() { return newDiscoveredAssets; }
        public void setNewDiscoveredAssets(long newDiscoveredAssets) { this.newDiscoveredAssets = newDiscoveredAssets; }
    }
}
