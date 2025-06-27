package com.vulnark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产发现任务实体
 */
@Entity
@Table(name = "asset_discovery_tasks")
public class AssetDiscoveryTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 任务描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 目标类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    /**
     * 扫描目标（JSON格式）
     */
    @Column(name = "targets", nullable = false, columnDefinition = "TEXT")
    private String targets;

    /**
     * 扫描类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scan_type", nullable = false)
    private ScanType scanType;

    /**
     * 扫描端口范围
     */
    @Column(name = "scan_ports", length = 1000)
    private String scanPorts;

    /**
     * 扫描选项配置（JSON格式）
     */
    @Column(name = "scan_options", columnDefinition = "JSON")
    private String scanOptions;

    /**
     * 调度类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType = ScheduleType.ONCE;

    /**
     * 调度配置（JSON格式）
     */
    @Column(name = "schedule_config", columnDefinition = "JSON")
    private String scheduleConfig;

    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * 执行进度
     */
    @Column(name = "progress", precision = 5, scale = 2)
    private BigDecimal progress = BigDecimal.ZERO;

    /**
     * 最后执行时间
     */
    @Column(name = "last_run_time")
    private LocalDateTime lastRunTime;

    /**
     * 下次执行时间
     */
    @Column(name = "next_run_time")
    private LocalDateTime nextRunTime;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime = LocalDateTime.now();

    /**
     * 创建者ID
     */
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    /**
     * 逻辑删除标记
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    /**
     * 目标类型枚举
     */
    public enum TargetType {
        IP_RANGE("IP范围"),
        SUBNET("子网"),
        DOMAIN("域名"),
        URL_LIST("URL列表"),
        CUSTOM("自定义");

        private final String description;

        TargetType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 扫描类型枚举
     */
    public enum ScanType {
        PING_SWEEP("主机发现"),
        PORT_SCAN("端口扫描"),
        SERVICE_DETECTION("服务检测"),
        FULL_SCAN("全面扫描");

        private final String description;

        ScanType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 调度类型枚举
     */
    public enum ScheduleType {
        ONCE("一次性"),
        DAILY("每日"),
        WEEKLY("每周"),
        MONTHLY("每月"),
        CUSTOM("自定义");

        private final String description;

        ScheduleType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING("待执行"),
        RUNNING("执行中"),
        COMPLETED("已完成"),
        FAILED("执行失败"),
        CANCELLED("已取消");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public AssetDiscoveryTask() {}

    public AssetDiscoveryTask(String name, TargetType targetType, String targets, ScanType scanType) {
        this.name = name;
        this.targetType = targetType;
        this.targets = targets;
        this.scanType = scanType;
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

    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public String getTargets() { return targets; }
    public void setTargets(String targets) { this.targets = targets; }

    public ScanType getScanType() { return scanType; }
    public void setScanType(ScanType scanType) { this.scanType = scanType; }

    public String getScanPorts() { return scanPorts; }
    public void setScanPorts(String scanPorts) { this.scanPorts = scanPorts; }

    public String getScanOptions() { return scanOptions; }
    public void setScanOptions(String scanOptions) { this.scanOptions = scanOptions; }

    public ScheduleType getScheduleType() { return scheduleType; }
    public void setScheduleType(ScheduleType scheduleType) { this.scheduleType = scheduleType; }

    public String getScheduleConfig() { return scheduleConfig; }
    public void setScheduleConfig(String scheduleConfig) { this.scheduleConfig = scheduleConfig; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

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

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}
