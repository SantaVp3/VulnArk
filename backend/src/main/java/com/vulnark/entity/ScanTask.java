package com.vulnark.entity;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Schema(description = "扫描任务实体")
@Entity
@Table(name = "scan_tasks")
public class ScanTask {
    
    @Schema(description = "扫描任务ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "任务名称")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Schema(description = "任务描述")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Schema(description = "扫描类型")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanType scanType;

    @Schema(description = "扫描引擎类型")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanEngine scanEngineType = ScanEngine.INTERNAL;

    @Schema(description = "扫描模板")
    @Enumerated(EnumType.STRING)
    private ScanTemplate scanTemplate;

    @Schema(description = "任务状态")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.CREATED;
    
    @Schema(description = "所属项目ID")
    @Column(nullable = false)
    private Long projectId;

    @Schema(description = "目标资产数量")
    private Integer targetCount = 0;

    @Schema(description = "总漏洞数量")
    private Integer totalVulnerabilityCount = 0;

    @Schema(description = "高危漏洞数量")
    private Integer highRiskCount = 0;

    @Schema(description = "中危漏洞数量")
    private Integer mediumRiskCount = 0;

    @Schema(description = "低危漏洞数量")
    private Integer lowRiskCount = 0;

    @Schema(description = "信息级漏洞数量")
    private Integer infoRiskCount = 0;
    
    @Schema(description = "扫描配置ID")
    private Long scanConfigId;

    @Schema(description = "外部扫描任务ID")
    @Column(length = 100)
    private String externalTaskId;

    @Schema(description = "扫描参数")
    @Column(columnDefinition = "TEXT")
    private String scanParameters;

    @Schema(description = "扫描结果文件路径")
    @Column(length = 500)
    private String resultFilePath;

    @Schema(description = "创建者")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;
    
    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStartTime;

    @Schema(description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;
    
    @Schema(description = "扫描结果")
    @Column(columnDefinition = "LONGTEXT")
    private String scanResult;
    
    @Schema(description = "发现的漏洞数量")
    private Integer vulnerabilityCount = 0;
    
    @Schema(description = "发现的端口数量")
    private Integer portCount = 0;
    
    @Schema(description = "发现的服务数量")
    private Integer serviceCount = 0;
    
    @Schema(description = "错误信息")
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Schema(description = "进度百分比")
    private Integer progress = 0;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    
    @Schema(description = "逻辑删除标记")
    @Column(nullable = false)
    private Boolean deleted = false;
    
    // 扫描类型枚举
    public enum ScanType {
        PORT_SCAN("端口扫描"),
        WEB_SCAN("Web应用扫描"),
        SYSTEM_SCAN("系统漏洞扫描"),
        COMPREHENSIVE_SCAN("综合扫描"),
        CUSTOM_SCAN("自定义扫描");

        private final String description;

        ScanType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 扫描引擎枚举
    public enum ScanEngine {
        NESSUS("Nessus"),
        OPENVAS("OpenVAS"),
        AWVS("AWVS"),
        NUCLEI("Nuclei"),
        NMAP("Nmap"),
        INTERNAL("内置引擎");

        private final String description;

        ScanEngine(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 扫描模板枚举
    public enum ScanTemplate {
        QUICK_SCAN("快速扫描"),
        FULL_SCAN("全面扫描"),
        WEB_APP_SCAN("Web应用扫描"),
        NETWORK_SCAN("网络扫描"),
        COMPLIANCE_SCAN("合规扫描"),
        CUSTOM("自定义模板");

        private final String description;

        ScanTemplate(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 任务状态枚举
    public enum TaskStatus {
        CREATED("已创建"),
        QUEUED("排队中"),
        RUNNING("扫描中"),
        PAUSED("已暂停"),
        COMPLETED("已完成"),
        FAILED("扫描失败"),
        CANCELLED("已取消"),
        TIMEOUT("扫描超时");

        private final String description;

        TaskStatus(String description) {
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
    public ScanTask() {}

    public ScanTask(String name, ScanType scanType, ScanEngine scanEngineType, Long projectId) {
        this.name = name;
        this.scanType = scanType;
        this.scanEngineType = scanEngineType;
        this.projectId = projectId;
    }

    // 便捷方法
    public boolean isRunning() {
        return status == TaskStatus.RUNNING;
    }

    public boolean isCompleted() {
        return status == TaskStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TaskStatus.FAILED || status == TaskStatus.TIMEOUT;
    }

    public boolean canStart() {
        return status == TaskStatus.CREATED || status == TaskStatus.QUEUED;
    }

    public boolean canPause() {
        return status == TaskStatus.RUNNING;
    }

    public boolean canResume() {
        return status == TaskStatus.PAUSED;
    }

    public boolean canCancel() {
        return status == TaskStatus.CREATED || status == TaskStatus.QUEUED ||
               status == TaskStatus.RUNNING || status == TaskStatus.PAUSED;
    }

    public void updateProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.updatedTime = LocalDateTime.now();
    }

    public void markAsStarted() {
        this.status = TaskStatus.RUNNING;
        this.actualStartTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = TaskStatus.COMPLETED;
        this.progress = 100;
        this.completedTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = TaskStatus.CANCELLED;
        this.completedTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }

    public Long getDurationMinutes() {
        if (actualStartTime == null) return null;
        LocalDateTime endTime = completedTime != null ? completedTime : LocalDateTime.now();
        return java.time.Duration.between(actualStartTime, endTime).toMinutes();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public ScanEngine getScanEngineType() {
        return scanEngineType;
    }

    public void setScanEngineType(ScanEngine scanEngineType) {
        this.scanEngineType = scanEngineType;
    }

    public ScanTemplate getScanTemplate() {
        return scanTemplate;
    }

    public void setScanTemplate(ScanTemplate scanTemplate) {
        this.scanTemplate = scanTemplate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }


    public Integer getTargetCount() { return targetCount; }
    public void setTargetCount(Integer targetCount) { this.targetCount = targetCount; }

    public Integer getHighRiskCount() { return highRiskCount; }
    public void setHighRiskCount(Integer highRiskCount) { this.highRiskCount = highRiskCount; }

    public Integer getMediumRiskCount() { return mediumRiskCount; }
    public void setMediumRiskCount(Integer mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }

    public Integer getLowRiskCount() { return lowRiskCount; }
    public void setLowRiskCount(Integer lowRiskCount) { this.lowRiskCount = lowRiskCount; }

    public Integer getInfoRiskCount() { return infoRiskCount; }
    public void setInfoRiskCount(Integer infoRiskCount) { this.infoRiskCount = infoRiskCount; }

    public Long getScanConfigId() { return scanConfigId; }
    public void setScanConfigId(Long scanConfigId) { this.scanConfigId = scanConfigId; }

    public String getExternalTaskId() { return externalTaskId; }
    public void setExternalTaskId(String externalTaskId) { this.externalTaskId = externalTaskId; }

    public String getScanParameters() { return scanParameters; }
    public void setScanParameters(String scanParameters) { this.scanParameters = scanParameters; }

    public String getResultFilePath() { return resultFilePath; }
    public void setResultFilePath(String resultFilePath) { this.resultFilePath = resultFilePath; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    
    public String getScanResult() {
        return scanResult;
    }
    
    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }
    
    public Integer getTotalVulnerabilityCount() {
        return totalVulnerabilityCount;
    }

    public void setTotalVulnerabilityCount(Integer totalVulnerabilityCount) {
        this.totalVulnerabilityCount = totalVulnerabilityCount;
    }
    
    public Integer getPortCount() {
        return portCount;
    }
    
    public void setPortCount(Integer portCount) {
        this.portCount = portCount;
    }
    
    public Integer getServiceCount() {
        return serviceCount;
    }
    
    public void setServiceCount(Integer serviceCount) {
        this.serviceCount = serviceCount;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
