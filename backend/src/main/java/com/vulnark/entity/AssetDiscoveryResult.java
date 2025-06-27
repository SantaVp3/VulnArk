package com.vulnark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产发现结果实体
 */
@Entity
@Table(name = "asset_discovery_results")
public class AssetDiscoveryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发现任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 扫描目标
     */
    @Column(name = "target", nullable = false, length = 500)
    private String target;

    /**
     * IP地址
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * 主机名
     */
    @Column(name = "hostname", length = 255)
    private String hostname;

    /**
     * MAC地址
     */
    @Column(name = "mac_address", length = 17)
    private String macAddress;

    /**
     * 是否在线
     */
    @Column(name = "is_alive", nullable = false)
    private Boolean isAlive = false;

    /**
     * 响应时间(ms)
     */
    @Column(name = "response_time")
    private Integer responseTime;

    /**
     * 开放端口列表（JSON格式）
     */
    @Column(name = "open_ports", columnDefinition = "JSON")
    private String openPorts;

    /**
     * 检测到的服务（JSON格式）
     */
    @Column(name = "services", columnDefinition = "JSON")
    private String services;

    /**
     * 操作系统
     */
    @Column(name = "operating_system", length = 200)
    private String operatingSystem;

    /**
     * 设备类型
     */
    @Column(name = "device_type", length = 100)
    private String deviceType;

    /**
     * 厂商信息
     */
    @Column(name = "vendor", length = 100)
    private String vendor;

    /**
     * 识别置信度
     */
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore = BigDecimal.ZERO;

    /**
     * 原始扫描数据（JSON格式）
     */
    @Column(name = "raw_data", columnDefinition = "JSON")
    private String rawData;

    /**
     * 关联的资产ID
     */
    @Column(name = "asset_id")
    private Long assetId;

    /**
     * 关联状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "correlation_status", nullable = false)
    private CorrelationStatus correlationStatus = CorrelationStatus.NEW;

    /**
     * 发现时间
     */
    @Column(name = "discovered_time", nullable = false)
    private LocalDateTime discoveredTime = LocalDateTime.now();

    /**
     * 关联状态枚举
     */
    public enum CorrelationStatus {
        NEW("新发现"),
        MATCHED("已匹配"),
        UPDATED("已更新"),
        IGNORED("已忽略");

        private final String description;

        CorrelationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Constructors
    public AssetDiscoveryResult() {}

    public AssetDiscoveryResult(Long taskId, String target, String ipAddress) {
        this.taskId = taskId;
        this.target = target;
        this.ipAddress = ipAddress;
    }

    @PrePersist
    protected void onCreate() {
        discoveredTime = LocalDateTime.now();
    }

    // Getter and Setter methods
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public Boolean getIsAlive() { return isAlive; }
    public void setIsAlive(Boolean isAlive) { this.isAlive = isAlive; }

    public Integer getResponseTime() { return responseTime; }
    public void setResponseTime(Integer responseTime) { this.responseTime = responseTime; }

    public String getOpenPorts() { return openPorts; }
    public void setOpenPorts(String openPorts) { this.openPorts = openPorts; }

    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public CorrelationStatus getCorrelationStatus() { return correlationStatus; }
    public void setCorrelationStatus(CorrelationStatus correlationStatus) { this.correlationStatus = correlationStatus; }

    public LocalDateTime getDiscoveredTime() { return discoveredTime; }
    public void setDiscoveredTime(LocalDateTime discoveredTime) { this.discoveredTime = discoveredTime; }
}
