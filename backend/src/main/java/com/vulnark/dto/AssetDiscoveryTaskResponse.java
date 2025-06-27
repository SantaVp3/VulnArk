package com.vulnark.dto;

import com.vulnark.entity.AssetDiscoveryTask;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产发现任务响应DTO
 */
public class AssetDiscoveryTaskResponse {

    private Long id;
    private String name;
    private String description;
    private AssetDiscoveryTask.TargetType targetType;
    private String targetTypeDescription;
    private String targets;
    private AssetDiscoveryTask.ScanType scanType;
    private String scanTypeDescription;
    private String scanPorts;
    private String scanOptions;
    private AssetDiscoveryTask.ScheduleType scheduleType;
    private String scheduleTypeDescription;
    private String scheduleConfig;
    private AssetDiscoveryTask.TaskStatus status;
    private String statusDescription;
    private BigDecimal progress;
    private LocalDateTime lastRunTime;
    private LocalDateTime nextRunTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Long createdBy;
    private String createdByName;

    // Constructors
    public AssetDiscoveryTaskResponse() {}

    /**
     * 从实体对象创建响应DTO
     */
    public static AssetDiscoveryTaskResponse fromEntity(AssetDiscoveryTask task) {
        AssetDiscoveryTaskResponse response = new AssetDiscoveryTaskResponse();
        response.setId(task.getId());
        response.setName(task.getName());
        response.setDescription(task.getDescription());
        response.setTargetType(task.getTargetType());
        response.setTargetTypeDescription(task.getTargetType().getDescription());
        response.setTargets(task.getTargets());
        response.setScanType(task.getScanType());
        response.setScanTypeDescription(task.getScanType().getDescription());
        response.setScanPorts(task.getScanPorts());
        response.setScanOptions(task.getScanOptions());
        response.setScheduleType(task.getScheduleType());
        response.setScheduleTypeDescription(task.getScheduleType().getDescription());
        response.setScheduleConfig(task.getScheduleConfig());
        response.setStatus(task.getStatus());
        response.setStatusDescription(task.getStatus().getDescription());
        response.setProgress(task.getProgress());
        response.setLastRunTime(task.getLastRunTime());
        response.setNextRunTime(task.getNextRunTime());
        response.setCreatedTime(task.getCreatedTime());
        response.setUpdatedTime(task.getUpdatedTime());
        response.setCreatedBy(task.getCreatedBy());
        return response;
    }

    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AssetDiscoveryTask.TargetType getTargetType() { return targetType; }
    public void setTargetType(AssetDiscoveryTask.TargetType targetType) { this.targetType = targetType; }

    public String getTargetTypeDescription() { return targetTypeDescription; }
    public void setTargetTypeDescription(String targetTypeDescription) { this.targetTypeDescription = targetTypeDescription; }

    public String getTargets() { return targets; }
    public void setTargets(String targets) { this.targets = targets; }

    public AssetDiscoveryTask.ScanType getScanType() { return scanType; }
    public void setScanType(AssetDiscoveryTask.ScanType scanType) { this.scanType = scanType; }

    public String getScanTypeDescription() { return scanTypeDescription; }
    public void setScanTypeDescription(String scanTypeDescription) { this.scanTypeDescription = scanTypeDescription; }

    public String getScanPorts() { return scanPorts; }
    public void setScanPorts(String scanPorts) { this.scanPorts = scanPorts; }

    public String getScanOptions() { return scanOptions; }
    public void setScanOptions(String scanOptions) { this.scanOptions = scanOptions; }

    public AssetDiscoveryTask.ScheduleType getScheduleType() { return scheduleType; }
    public void setScheduleType(AssetDiscoveryTask.ScheduleType scheduleType) { this.scheduleType = scheduleType; }

    public String getScheduleTypeDescription() { return scheduleTypeDescription; }
    public void setScheduleTypeDescription(String scheduleTypeDescription) { this.scheduleTypeDescription = scheduleTypeDescription; }

    public String getScheduleConfig() { return scheduleConfig; }
    public void setScheduleConfig(String scheduleConfig) { this.scheduleConfig = scheduleConfig; }

    public AssetDiscoveryTask.TaskStatus getStatus() { return status; }
    public void setStatus(AssetDiscoveryTask.TaskStatus status) { this.status = status; }

    public String getStatusDescription() { return statusDescription; }
    public void setStatusDescription(String statusDescription) { this.statusDescription = statusDescription; }

    public BigDecimal getProgress() { return progress; }
    public void setProgress(BigDecimal progress) { this.progress = progress; }

    public LocalDateTime getLastRunTime() { return lastRunTime; }
    public void setLastRunTime(LocalDateTime lastRunTime) { this.lastRunTime = lastRunTime; }

    public LocalDateTime getNextRunTime() { return nextRunTime; }
    public void setNextRunTime(LocalDateTime nextRunTime) { this.nextRunTime = nextRunTime; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
}
