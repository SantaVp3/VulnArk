package com.vulnark.entity;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Schema(description = "资产实体")
@Entity
@Table(name = "assets")
public class Asset {
    
    @Schema(description = "资产ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "资产名称")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Schema(description = "资产描述")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Schema(description = "资产类型")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;
    
    @Schema(description = "资产状态")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;
    
    @Schema(description = "IP地址")
    @Column(length = 45)
    private String ipAddress;
    
    @Schema(description = "域名")
    @Column(length = 255)
    private String domain;
    
    @Schema(description = "端口")
    private Integer port;
    
    @Schema(description = "协议")
    @Column(length = 20)
    private String protocol;
    
    @Schema(description = "服务")
    @Column(length = 100)
    private String service;
    
    @Schema(description = "版本")
    @Column(length = 50)
    private String version;
    
    @Schema(description = "操作系统")
    @Column(length = 100)
    private String operatingSystem;
    
    @Schema(description = "重要性等级")
    @Enumerated(EnumType.STRING)
    private Importance importance = Importance.MEDIUM;
    
    @Schema(description = "所属项目ID")
    @Column(nullable = false)
    private Long projectId;
    
    @Schema(description = "负责人ID")
    private Long ownerId;
    
    @Schema(description = "位置")
    @Column(length = 200)
    private String location;
    
    @Schema(description = "供应商")
    @Column(length = 100)
    private String vendor;
    
    @Schema(description = "资产标签")
    @Column(length = 500)
    private String tags;
    
    @Schema(description = "最后扫描时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastScanTime;
    
    @Schema(description = "漏洞数量")
    private Integer vulnerabilityCount = 0;
    
    @Schema(description = "风险评分")
    private Double riskScore = 0.0;
    
    @Schema(description = "备注")
    @Column(columnDefinition = "TEXT")
    private String notes;
    
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
    
    // 枚举定义
    public enum AssetType {
        SERVER, WORKSTATION, NETWORK_DEVICE, DATABASE, WEB_APPLICATION, 
        MOBILE_APPLICATION, IOT_DEVICE, CLOUD_SERVICE, OTHER
    }
    
    public enum Status {
        ACTIVE, INACTIVE, MAINTENANCE, DECOMMISSIONED
    }
    
    public enum Importance {
        LOW, MEDIUM, HIGH, CRITICAL
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
    public Asset() {}
    
    public Asset(String name, AssetType type, Long projectId) {
        this.name = name;
        this.type = type;
        this.projectId = projectId;
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
    
    public AssetType getType() {
        return type;
    }
    
    public void setType(AssetType type) {
        this.type = type;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public Integer getPort() {
        return port;
    }
    
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getOperatingSystem() {
        return operatingSystem;
    }
    
    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }
    
    public Importance getImportance() {
        return importance;
    }
    
    public void setImportance(Importance importance) {
        this.importance = importance;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getVendor() {
        return vendor;
    }
    
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public LocalDateTime getLastScanTime() {
        return lastScanTime;
    }
    
    public void setLastScanTime(LocalDateTime lastScanTime) {
        this.lastScanTime = lastScanTime;
    }
    
    public Integer getVulnerabilityCount() {
        return vulnerabilityCount;
    }
    
    public void setVulnerabilityCount(Integer vulnerabilityCount) {
        this.vulnerabilityCount = vulnerabilityCount;
    }
    
    public Double getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
