package com.vulnark.service;

import com.vulnark.entity.Agent;
import com.vulnark.entity.BaselineTask;
import com.vulnark.entity.BaselineResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BaselineTaskService {
    
    /**
     * 创建基线检查任务
     */
    BaselineTask createTask(String agentId, BaselineTask.TaskType taskType, String configuration);
    
    /**
     * 批量创建基线检查任务
     */
    List<BaselineTask> createTasksForAgents(List<String> agentIds, BaselineTask.TaskType taskType, String configuration);
    
    /**
     * 获取任务详情
     */
    Optional<BaselineTask> getTask(String taskId);
    
    /**
     * 获取所有任务
     */
    Page<BaselineTask> getAllTasks(Pageable pageable);
    
    /**
     * 根据Agent获取任务
     */
    List<BaselineTask> getTasksByAgent(String agentId);
    
    /**
     * 根据状态获取任务
     */
    List<BaselineTask> getTasksByStatus(BaselineTask.Status status);
    
    /**
     * 更新任务状态
     */
    void updateTaskStatus(String taskId, BaselineTask.Status status);
    
    /**
     * 开始执行任务
     */
    void startTask(String taskId);
    
    /**
     * 完成任务
     */
    void completeTask(String taskId);
    
    /**
     * 任务执行失败
     */
    void failTask(String taskId, String errorMessage);
    
    /**
     * 取消任务
     */
    void cancelTask(String taskId);
    
    /**
     * 删除任务
     */
    void deleteTask(String taskId);
    
    /**
     * 提交任务结果
     */
    void submitTaskResults(String taskId, List<BaselineResult> results);
    
    /**
     * 获取任务结果
     */
    List<BaselineResult> getTaskResults(String taskId);
    
    /**
     * 重新执行任务
     */
    BaselineTask retryTask(String taskId);
    
    /**
     * 获取任务统计信息
     */
    TaskStats getTaskStats();
    
    /**
     * 清理过期任务
     */
    void cleanupExpiredTasks();
    
    /**
     * 任务统计信息
     */
    class TaskStats {
        private long totalTasks;
        private long pendingTasks;
        private long runningTasks;
        private long completedTasks;
        private long failedTasks;
        private long cancelledTasks;
        
        public TaskStats(long totalTasks, long pendingTasks, long runningTasks,
                        long completedTasks, long failedTasks, long cancelledTasks) {
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.runningTasks = runningTasks;
            this.completedTasks = completedTasks;
            this.failedTasks = failedTasks;
            this.cancelledTasks = cancelledTasks;
        }
        
        // Getters
        public long getTotalTasks() { return totalTasks; }
        public long getPendingTasks() { return pendingTasks; }
        public long getRunningTasks() { return runningTasks; }
        public long getCompletedTasks() { return completedTasks; }
        public long getFailedTasks() { return failedTasks; }
        public long getCancelledTasks() { return cancelledTasks; }
    }
}
