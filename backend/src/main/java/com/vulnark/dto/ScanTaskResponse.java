package com.vulnark.dto;

import com.vulnark.entity.ScanTask;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 扫描任务响应DTO
 */
@Schema(description = "扫描任务响应")
public class ScanTaskResponse {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "任务描述")
    private String description;

    @Schema(description = "扫描类型")
    private ScanTask.ScanType scanType;

    @Schema(description = "扫描类型描述")
    private String scanTypeDescription;

    @Schema(description = "扫描引擎")
    private ScanTask.ScanEngine scanEngine;

    @Schema(description = "扫描引擎描述")
    private String scanEngineDescription;

    @Schema(description = "扫描模板")
    private ScanTask.ScanTemplate scanTemplate;

    @Schema(description = "扫描模板描述")
    private String scanTemplateDescription;

    @Schema(description = "任务状态")
    private ScanTask.TaskStatus status;

    @Schema(description = "任务状态描述")
    private String statusDescription;

    @Schema(description = "执行进度（0-100）")
    private Integer progress;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "目标资产列表")
    private String targetAssets;

    @Schema(description = "扫描参数")
    private String scanParameters;

    @Schema(description = "扫描选项")
    private String scanOptions;

    @Schema(description = "漏洞总数")
    private Integer totalVulnerabilityCount;

    @Schema(description = "高危漏洞数")
    private Integer highRiskCount;

    @Schema(description = "中危漏洞数")
    private Integer mediumRiskCount;

    @Schema(description = "低危漏洞数")
    private Integer lowRiskCount;

    @Schema(description = "信息级漏洞数")
    private Integer infoRiskCount;

    @Schema(description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledStartTime;

    @Schema(description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    @Schema(description = "预估执行时间（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "实际执行时间（分钟）")
    private Integer actualDuration;

    @Schema(description = "外部任务ID")
    private String externalTaskId;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建者ID")
    private Long createdById;

    @Schema(description = "创建者姓名")
    private String createdByName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    // 构造函数
    public ScanTaskResponse() {}

    // 便捷方法
    public boolean isRunning() {
        return status == ScanTask.TaskStatus.RUNNING;
    }

    public boolean isCompleted() {
        return status == ScanTask.TaskStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == ScanTask.TaskStatus.FAILED || status == ScanTask.TaskStatus.TIMEOUT;
    }

    public boolean canStart() {
        return status == ScanTask.TaskStatus.CREATED || status == ScanTask.TaskStatus.QUEUED;
    }

    public boolean canCancel() {
        return status == ScanTask.TaskStatus.RUNNING || status == ScanTask.TaskStatus.QUEUED;
    }

    public String getStatusColor() {
        if (status == null) return "gray";
        
        switch (status) {
            case RUNNING:
                return "blue";
            case COMPLETED:
                return "green";
            case FAILED:
            case TIMEOUT:
                return "red";
            case CANCELLED:
                return "orange";
            case QUEUED:
                return "purple";
            default:
                return "gray";
        }
    }

    public String getSeverityDistribution() {
        if (totalVulnerabilityCount == null || totalVulnerabilityCount == 0) {
            return "无漏洞";
        }
        
        StringBuilder sb = new StringBuilder();
        if (highRiskCount != null && highRiskCount > 0) {
            sb.append("高危: ").append(highRiskCount).append(" ");
        }
        if (mediumRiskCount != null && mediumRiskCount > 0) {
            sb.append("中危: ").append(mediumRiskCount).append(" ");
        }
        if (lowRiskCount != null && lowRiskCount > 0) {
            sb.append("低危: ").append(lowRiskCount).append(" ");
        }
        if (infoRiskCount != null && infoRiskCount > 0) {
            sb.append("信息: ").append(infoRiskCount).append(" ");
        }
        
        return sb.toString().trim();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ScanTask.ScanType getScanType() { return scanType; }
    public void setScanType(ScanTask.ScanType scanType) { 
        this.scanType = scanType;
        this.scanTypeDescription = scanType != null ? scanType.getDescription() : null;
    }

    public String getScanTypeDescription() { return scanTypeDescription; }
    public void setScanTypeDescription(String scanTypeDescription) { this.scanTypeDescription = scanTypeDescription; }

    public ScanTask.ScanEngine getScanEngine() { return scanEngine; }
    public void setScanEngine(ScanTask.ScanEngine scanEngine) { 
        this.scanEngine = scanEngine;
        this.scanEngineDescription = scanEngine != null ? scanEngine.getDescription() : null;
    }

    public String getScanEngineDescription() { return scanEngineDescription; }
    public void setScanEngineDescription(String scanEngineDescription) { this.scanEngineDescription = scanEngineDescription; }

    public ScanTask.ScanTemplate getScanTemplate() { return scanTemplate; }
    public void setScanTemplate(ScanTask.ScanTemplate scanTemplate) { 
        this.scanTemplate = scanTemplate;
        this.scanTemplateDescription = scanTemplate != null ? scanTemplate.getDescription() : null;
    }

    public String getScanTemplateDescription() { return scanTemplateDescription; }
    public void setScanTemplateDescription(String scanTemplateDescription) { this.scanTemplateDescription = scanTemplateDescription; }

    public ScanTask.TaskStatus getStatus() { return status; }
    public void setStatus(ScanTask.TaskStatus status) { 
        this.status = status;
        this.statusDescription = status != null ? status.getDescription() : null;
    }

    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getTargetAssets() { return targetAssets; }
    public void setTargetAssets(String targetAssets) { this.targetAssets = targetAssets; }

    public String getScanParameters() { return scanParameters; }
    public void setScanParameters(String scanParameters) { this.scanParameters = scanParameters; }

    public String getScanOptions() { return scanOptions; }
    public void setScanOptions(String scanOptions) { this.scanOptions = scanOptions; }

    public Integer getTotalVulnerabilityCount() { return totalVulnerabilityCount; }
    public void setTotalVulnerabilityCount(Integer totalVulnerabilityCount) { this.totalVulnerabilityCount = totalVulnerabilityCount; }

    public Integer getHighRiskCount() { return highRiskCount; }
    public void setHighRiskCount(Integer highRiskCount) { this.highRiskCount = highRiskCount; }

    public Integer getMediumRiskCount() { return mediumRiskCount; }
    public void setMediumRiskCount(Integer mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }

    public Integer getLowRiskCount() { return lowRiskCount; }
    public void setLowRiskCount(Integer lowRiskCount) { this.lowRiskCount = lowRiskCount; }

    public Integer getInfoRiskCount() { return infoRiskCount; }
    public void setInfoRiskCount(Integer infoRiskCount) { this.infoRiskCount = infoRiskCount; }

    public LocalDateTime getScheduledStartTime() { return scheduledStartTime; }
    public void setScheduledStartTime(LocalDateTime scheduledStartTime) { this.scheduledStartTime = scheduledStartTime; }

    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) { this.actualStartTime = actualStartTime; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

    public Integer getActualDuration() { return actualDuration; }
    public void setActualDuration(Integer actualDuration) { this.actualDuration = actualDuration; }

    public String getExternalTaskId() { return externalTaskId; }
    public void setExternalTaskId(String externalTaskId) { this.externalTaskId = externalTaskId; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}
