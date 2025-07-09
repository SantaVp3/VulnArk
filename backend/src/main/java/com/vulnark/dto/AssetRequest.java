package com.vulnark.dto;

import com.vulnark.entity.Asset;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Schema(description = "资产请求DTO")
public class AssetRequest {
    
    @Schema(description = "资产名称")
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 100, message = "资产名称长度不能超过100个字符")
    private String name;
    
    @Schema(description = "资产描述")
    private String description;
    
    @Schema(description = "资产类型")
    @NotNull(message = "资产类型不能为空")
    private Asset.AssetType type;
    
    @Schema(description = "资产状态")
    private Asset.Status status;
    
    @Schema(description = "IP地址")
    @NotBlank(message = "IP地址不能为空")
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", 
             message = "IP地址格式不正确")
    private String ipAddress;
    
    @Schema(description = "域名")
    @Size(max = 255, message = "域名长度不能超过255个字符")
    private String domain;
    
    @Schema(description = "端口")
    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号不能大于65535")
    private Integer port;
    
    @Schema(description = "协议")
    @Size(max = 20, message = "协议长度不能超过20个字符")
    private String protocol;
    
    @Schema(description = "服务")
    @Size(max = 100, message = "服务长度不能超过100个字符")
    private String service;
    
    @Schema(description = "版本")
    @Size(max = 50, message = "版本长度不能超过50个字符")
    private String version;
    
    @Schema(description = "操作系统")
    @Size(max = 100, message = "操作系统长度不能超过100个字符")
    private String operatingSystem;
    
    @Schema(description = "重要性等级")
    private Asset.Importance importance;
    
    @Schema(description = "项目ID")
    private Long projectId = 1L;
    
    @Schema(description = "负责人ID")
    private Long ownerId;
    
    @Schema(description = "位置")
    @Size(max = 200, message = "位置长度不能超过200个字符")
    private String location;
    
    @Schema(description = "供应商")
    @Size(max = 100, message = "供应商长度不能超过100个字符")
    private String vendor;
    
    @Schema(description = "资产标签")
    @Size(max = 500, message = "资产标签长度不能超过500个字符")
    private String tags;
    
    @Schema(description = "最后扫描时间")
    private LocalDateTime lastScanTime;
    
    @Schema(description = "风险评分")
    @DecimalMin(value = "0.0", message = "风险评分不能小于0")
    @DecimalMax(value = "10.0", message = "风险评分不能大于10")
    private Double riskScore;
    
    @Schema(description = "备注")
    private String notes;
    
    // Constructors
    public AssetRequest() {}
    
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
    
    public Asset.AssetType getType() {
        return type;
    }
    
    public void setType(Asset.AssetType type) {
        this.type = type;
    }
    
    public Asset.Status getStatus() {
        return status;
    }
    
    public void setStatus(Asset.Status status) {
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
    
    public Asset.Importance getImportance() {
        return importance;
    }
    
    public void setImportance(Asset.Importance importance) {
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
    
    @Override
    public String toString() {
        return "AssetRequest{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", ipAddress='" + ipAddress + '\'' +
                ", domain='" + domain + '\'' +
                ", ownerId=" + ownerId +
                '}';
    }
}
