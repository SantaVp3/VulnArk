package com.vulnark.dto;

import com.vulnark.entity.AssetDiscoveryTask;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 资产发现任务请求DTO
 */
public class AssetDiscoveryTaskRequest {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 目标类型
     */
    @NotNull(message = "目标类型不能为空")
    private AssetDiscoveryTask.TargetType targetType;

    /**
     * 扫描目标
     */
    @NotBlank(message = "扫描目标不能为空")
    private String targets;

    /**
     * 扫描类型
     */
    @NotNull(message = "扫描类型不能为空")
    private AssetDiscoveryTask.ScanType scanType;

    /**
     * 扫描端口范围
     */
    private String scanPorts;

    /**
     * 扫描选项配置
     */
    private String scanOptions;

    /**
     * 调度类型
     */
    @NotNull(message = "调度类型不能为空")
    private AssetDiscoveryTask.ScheduleType scheduleType = AssetDiscoveryTask.ScheduleType.ONCE;

    /**
     * 调度配置
     */
    private String scheduleConfig;

    // Constructors
    public AssetDiscoveryTaskRequest() {}

    // Getter and Setter methods
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

    public AssetDiscoveryTask.TargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(AssetDiscoveryTask.TargetType targetType) {
        this.targetType = targetType;
    }

    public String getTargets() {
        return targets;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    public AssetDiscoveryTask.ScanType getScanType() {
        return scanType;
    }

    public void setScanType(AssetDiscoveryTask.ScanType scanType) {
        this.scanType = scanType;
    }

    public String getScanPorts() {
        return scanPorts;
    }

    public void setScanPorts(String scanPorts) {
        this.scanPorts = scanPorts;
    }

    public String getScanOptions() {
        return scanOptions;
    }

    public void setScanOptions(String scanOptions) {
        this.scanOptions = scanOptions;
    }

    public AssetDiscoveryTask.ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(AssetDiscoveryTask.ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getScheduleConfig() {
        return scheduleConfig;
    }

    public void setScheduleConfig(String scheduleConfig) {
        this.scheduleConfig = scheduleConfig;
    }

    /**
     * 转换为实体对象
     */
    public AssetDiscoveryTask toEntity() {
        AssetDiscoveryTask task = new AssetDiscoveryTask();
        task.setName(this.name);
        task.setDescription(this.description);
        task.setTargetType(this.targetType);
        task.setTargets(this.targets);
        task.setScanType(this.scanType);
        task.setScanPorts(this.scanPorts);
        task.setScanOptions(this.scanOptions);
        task.setScheduleType(this.scheduleType);
        task.setScheduleConfig(this.scheduleConfig);
        return task;
    }
}
