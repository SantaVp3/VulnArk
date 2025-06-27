package com.vulnark.dto;

import com.vulnark.entity.ScanTask;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 扫描任务请求DTO
 */
@Schema(description = "扫描任务请求")
public class ScanTaskRequest {

    @Schema(description = "任务名称", required = true)
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    private String name;

    @Schema(description = "任务描述")
    @Size(max = 1000, message = "任务描述长度不能超过1000个字符")
    private String description;

    @Schema(description = "扫描类型", required = true)
    @NotNull(message = "扫描类型不能为空")
    private ScanTask.ScanType scanType;

    @Schema(description = "扫描引擎", required = true)
    @NotNull(message = "扫描引擎不能为空")
    private ScanTask.ScanEngine scanEngine;

    @Schema(description = "扫描模板")
    private ScanTask.ScanTemplate scanTemplate;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "目标资产列表", example = "[\"192.168.1.1\", \"192.168.1.2\", \"example.com\"]")
    private String targetAssets;

    @Schema(description = "扫描参数", example = "{\"timeout\": 300, \"threads\": 10}")
    private String scanParameters;

    @Schema(description = "扫描选项", example = "{\"skipPing\": false, \"detectOS\": true}")
    private String scanOptions;

    @Schema(description = "计划开始时间")
    private LocalDateTime scheduledStartTime;

    @Schema(description = "预估执行时间（分钟）")
    private Integer estimatedDuration;

    @Schema(description = "扫描配置ID")
    private Long scanConfigId;

    // 构造函数
    public ScanTaskRequest() {}

    public ScanTaskRequest(String name, ScanTask.ScanType scanType, ScanTask.ScanEngine scanEngine) {
        this.name = name;
        this.scanType = scanType;
        this.scanEngine = scanEngine;
    }

    // Getters and Setters
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

    public ScanTask.ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanTask.ScanType scanType) {
        this.scanType = scanType;
    }

    public ScanTask.ScanEngine getScanEngine() {
        return scanEngine;
    }

    public void setScanEngine(ScanTask.ScanEngine scanEngine) {
        this.scanEngine = scanEngine;
    }

    public ScanTask.ScanTemplate getScanTemplate() {
        return scanTemplate;
    }

    public void setScanTemplate(ScanTask.ScanTemplate scanTemplate) {
        this.scanTemplate = scanTemplate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTargetAssets() {
        return targetAssets;
    }

    public void setTargetAssets(String targetAssets) {
        this.targetAssets = targetAssets;
    }

    public String getScanParameters() {
        return scanParameters;
    }

    public void setScanParameters(String scanParameters) {
        this.scanParameters = scanParameters;
    }

    public String getScanOptions() {
        return scanOptions;
    }

    public void setScanOptions(String scanOptions) {
        this.scanOptions = scanOptions;
    }

    public LocalDateTime getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(LocalDateTime scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Long getScanConfigId() {
        return scanConfigId;
    }

    public void setScanConfigId(Long scanConfigId) {
        this.scanConfigId = scanConfigId;
    }

    @Override
    public String toString() {
        return "ScanTaskRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", scanType=" + scanType +
                ", scanEngine=" + scanEngine +
                ", scanTemplate=" + scanTemplate +
                ", projectId=" + projectId +
                ", targetAssets='" + targetAssets + '\'' +
                ", scanParameters='" + scanParameters + '\'' +
                ", scanOptions='" + scanOptions + '\'' +
                ", scheduledStartTime=" + scheduledStartTime +
                ", estimatedDuration=" + estimatedDuration +
                ", scanConfigId=" + scanConfigId +
                '}';
    }
}
