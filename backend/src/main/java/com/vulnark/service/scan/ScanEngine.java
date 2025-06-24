package com.vulnark.service.scan;

import com.vulnark.entity.Asset;
import com.vulnark.entity.ScanConfig;
import com.vulnark.entity.ScanTask;
import com.vulnark.entity.Vulnerability;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 扫描引擎接口
 * 定义了所有扫描引擎必须实现的基本方法
 */
public interface ScanEngine {
    
    /**
     * 获取扫描引擎类型
     */
    ScanTask.ScanEngine getEngineType();
    
    /**
     * 获取扫描引擎名称
     */
    String getEngineName();
    
    /**
     * 获取扫描引擎版本
     */
    String getEngineVersion();
    
    /**
     * 检查扫描引擎是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取支持的扫描类型
     */
    List<ScanTask.ScanType> getSupportedScanTypes();
    
    /**
     * 创建扫描任务
     * @param scanTask 扫描任务
     * @param assets 目标资产列表
     * @param config 扫描配置
     * @return 外部任务ID
     */
    String createScanTask(ScanTask scanTask, List<Asset> assets, ScanConfig config) throws ScanEngineException;
    
    /**
     * 启动扫描任务
     * @param externalTaskId 外部任务ID
     */
    void startScan(String externalTaskId) throws ScanEngineException;
    
    /**
     * 暂停扫描任务
     * @param externalTaskId 外部任务ID
     */
    void pauseScan(String externalTaskId) throws ScanEngineException;
    
    /**
     * 恢复扫描任务
     * @param externalTaskId 外部任务ID
     */
    void resumeScan(String externalTaskId) throws ScanEngineException;
    
    /**
     * 停止扫描任务
     * @param externalTaskId 外部任务ID
     */
    void stopScan(String externalTaskId) throws ScanEngineException;
    
    /**
     * 获取扫描任务状态
     * @param externalTaskId 外部任务ID
     * @return 扫描状态信息
     */
    ScanStatus getScanStatus(String externalTaskId) throws ScanEngineException;
    
    /**
     * 获取扫描进度
     * @param externalTaskId 外部任务ID
     * @return 进度百分比（0-100）
     */
    int getScanProgress(String externalTaskId) throws ScanEngineException;
    
    /**
     * 获取扫描结果
     * @param externalTaskId 外部任务ID
     * @return 扫描结果
     */
    ScanResult getScanResult(String externalTaskId) throws ScanEngineException;
    
    /**
     * 异步获取扫描结果
     * @param externalTaskId 外部任务ID
     * @return 扫描结果的Future
     */
    CompletableFuture<ScanResult> getScanResultAsync(String externalTaskId);
    
    /**
     * 解析扫描结果文件
     * @param filePath 结果文件路径
     * @param fileType 文件类型
     * @return 解析后的漏洞列表
     */
    List<Vulnerability> parseResultFile(String filePath, String fileType) throws ScanEngineException;
    
    /**
     * 验证扫描配置
     * @param config 扫描配置
     * @return 验证结果
     */
    ValidationResult validateConfig(ScanConfig config);
    
    /**
     * 获取默认扫描配置
     * @param scanType 扫描类型
     * @return 默认配置
     */
    ScanConfig getDefaultConfig(ScanTask.ScanType scanType);
    
    /**
     * 清理扫描任务资源
     * @param externalTaskId 外部任务ID
     */
    void cleanup(String externalTaskId);
    
    /**
     * 扫描状态信息
     */
    class ScanStatus {
        private ScanTask.TaskStatus status;
        private int progress;
        private String message;
        private long vulnerabilityCount;
        private long highRiskCount;
        private long mediumRiskCount;
        private long lowRiskCount;
        private long infoRiskCount;
        
        public ScanStatus() {}
        
        public ScanStatus(ScanTask.TaskStatus status, int progress, String message) {
            this.status = status;
            this.progress = progress;
            this.message = message;
        }
        
        // Getters and Setters
        public ScanTask.TaskStatus getStatus() { return status; }
        public void setStatus(ScanTask.TaskStatus status) { this.status = status; }
        
        public int getProgress() { return progress; }
        public void setProgress(int progress) { this.progress = progress; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getVulnerabilityCount() { return vulnerabilityCount; }
        public void setVulnerabilityCount(long vulnerabilityCount) { this.vulnerabilityCount = vulnerabilityCount; }
        
        public long getHighRiskCount() { return highRiskCount; }
        public void setHighRiskCount(long highRiskCount) { this.highRiskCount = highRiskCount; }
        
        public long getMediumRiskCount() { return mediumRiskCount; }
        public void setMediumRiskCount(long mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }
        
        public long getLowRiskCount() { return lowRiskCount; }
        public void setLowRiskCount(long lowRiskCount) { this.lowRiskCount = lowRiskCount; }
        
        public long getInfoRiskCount() { return infoRiskCount; }
        public void setInfoRiskCount(long infoRiskCount) { this.infoRiskCount = infoRiskCount; }
    }
    
    /**
     * 扫描结果信息
     */
    class ScanResult {
        private String taskId;
        private ScanTask.TaskStatus status;
        private List<Vulnerability> vulnerabilities;
        private String resultFilePath;
        private String summary;
        private long scanDuration;
        
        public ScanResult() {}
        
        public ScanResult(String taskId, ScanTask.TaskStatus status, List<Vulnerability> vulnerabilities) {
            this.taskId = taskId;
            this.status = status;
            this.vulnerabilities = vulnerabilities;
        }
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        
        public ScanTask.TaskStatus getStatus() { return status; }
        public void setStatus(ScanTask.TaskStatus status) { this.status = status; }
        
        public List<Vulnerability> getVulnerabilities() { return vulnerabilities; }
        public void setVulnerabilities(List<Vulnerability> vulnerabilities) { this.vulnerabilities = vulnerabilities; }
        
        public String getResultFilePath() { return resultFilePath; }
        public void setResultFilePath(String resultFilePath) { this.resultFilePath = resultFilePath; }
        
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public long getScanDuration() { return scanDuration; }
        public void setScanDuration(long scanDuration) { this.scanDuration = scanDuration; }
    }
    
    /**
     * 配置验证结果
     */
    class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        
        public ValidationResult() {}
        
        public ValidationResult(boolean valid) {
            this.valid = valid;
        }
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
}
