package com.vulnark.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Schema(description = "扫描任务目标关联实体")
@Entity
@Table(name = "scan_task_targets")
public class ScanTaskTarget {
    
    @Schema(description = "关联ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "扫描任务")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_task_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ScanTask scanTask;
    
    @Schema(description = "目标资产")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Asset asset;
    
    @Schema(description = "扫描状态")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanStatus status = ScanStatus.PENDING;
    
    @Schema(description = "扫描进度（0-100）")
    private Integer progress = 0;
    
    @Schema(description = "发现的漏洞数量")
    private Integer vulnerabilityCount = 0;
    
    @Schema(description = "高危漏洞数量")
    private Integer highRiskCount = 0;
    
    @Schema(description = "中危漏洞数量")
    private Integer mediumRiskCount = 0;
    
    @Schema(description = "低危漏洞数量")
    private Integer lowRiskCount = 0;
    
    @Schema(description = "信息级漏洞数量")
    private Integer infoRiskCount = 0;
    
    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;
    
    @Schema(description = "错误信息")
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Schema(description = "扫描结果摘要")
    @Column(columnDefinition = "TEXT")
    private String resultSummary;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    
    // 扫描状态枚举
    public enum ScanStatus {
        PENDING("等待扫描"),
        RUNNING("扫描中"),
        COMPLETED("扫描完成"),
        FAILED("扫描失败"),
        SKIPPED("已跳过");
        
        private final String description;
        
        ScanStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
    
    // Constructors
    public ScanTaskTarget() {}
    
    public ScanTaskTarget(ScanTask scanTask, Asset asset) {
        this.scanTask = scanTask;
        this.asset = asset;
    }
    
    // 便捷方法
    public boolean isCompleted() {
        return status == ScanStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == ScanStatus.FAILED;
    }
    
    public boolean isRunning() {
        return status == ScanStatus.RUNNING;
    }
    
    public void markAsStarted() {
        this.status = ScanStatus.RUNNING;
        this.startTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    public void markAsCompleted() {
        this.status = ScanStatus.COMPLETED;
        this.progress = 100;
        this.completedTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = ScanStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    public void updateProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.updatedTime = LocalDateTime.now();
    }
    
    public void updateVulnerabilityCounts(int high, int medium, int low, int info) {
        this.highRiskCount = high;
        this.mediumRiskCount = medium;
        this.lowRiskCount = low;
        this.infoRiskCount = info;
        this.vulnerabilityCount = high + medium + low + info;
        this.updatedTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public ScanTask getScanTask() { return scanTask; }
    public void setScanTask(ScanTask scanTask) { this.scanTask = scanTask; }
    
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    
    public ScanStatus getStatus() { return status; }
    public void setStatus(ScanStatus status) { this.status = status; }
    
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    
    public Integer getVulnerabilityCount() { return vulnerabilityCount; }
    public void setVulnerabilityCount(Integer vulnerabilityCount) { this.vulnerabilityCount = vulnerabilityCount; }
    
    public Integer getHighRiskCount() { return highRiskCount; }
    public void setHighRiskCount(Integer highRiskCount) { this.highRiskCount = highRiskCount; }
    
    public Integer getMediumRiskCount() { return mediumRiskCount; }
    public void setMediumRiskCount(Integer mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }
    
    public Integer getLowRiskCount() { return lowRiskCount; }
    public void setLowRiskCount(Integer lowRiskCount) { this.lowRiskCount = lowRiskCount; }
    
    public Integer getInfoRiskCount() { return infoRiskCount; }
    public void setInfoRiskCount(Integer infoRiskCount) { this.infoRiskCount = infoRiskCount; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}
