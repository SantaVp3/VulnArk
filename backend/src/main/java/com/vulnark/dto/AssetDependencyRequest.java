package com.vulnark.dto;

import com.vulnark.entity.AssetDependency;
import jakarta.validation.constraints.NotNull;
/**
 * 资产依赖关系请求DTO
 */
public class AssetDependencyRequest {
    
    /**
     * 源资产ID（依赖方）
     */
    @NotNull(message = "源资产ID不能为空")
    private Long sourceAssetId;
    
    /**
     * 目标资产ID（被依赖方）
     */
    @NotNull(message = "目标资产ID不能为空")
    private Long targetAssetId;
    
    /**
     * 依赖类型
     */
    @NotNull(message = "依赖类型不能为空")
    private AssetDependency.DependencyType dependencyType;
    
    /**
     * 依赖强度/重要性
     */
    @NotNull(message = "依赖强度不能为空")
    private AssetDependency.DependencyStrength dependencyStrength;
    
    /**
     * 依赖描述
     */
    private String description;
    
    /**
     * 端口信息（如果适用）
     */
    private Integer port;
    
    /**
     * 协议信息（如果适用）
     */
    private String protocol;
    
    /**
     * 服务名称（如果适用）
     */
    private String serviceName;
    
    /**
     * 是否为关键依赖
     */
    private Boolean isCritical = false;
    
    /**
     * 依赖状态
     */
    private AssetDependency.DependencyStatus status = AssetDependency.DependencyStatus.ACTIVE;

    // Getter and Setter methods
    public Long getSourceAssetId() {
        return sourceAssetId;
    }

    public void setSourceAssetId(Long sourceAssetId) {
        this.sourceAssetId = sourceAssetId;
    }

    public Long getTargetAssetId() {
        return targetAssetId;
    }

    public void setTargetAssetId(Long targetAssetId) {
        this.targetAssetId = targetAssetId;
    }

    public AssetDependency.DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(AssetDependency.DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public AssetDependency.DependencyStrength getDependencyStrength() {
        return dependencyStrength;
    }

    public void setDependencyStrength(AssetDependency.DependencyStrength dependencyStrength) {
        this.dependencyStrength = dependencyStrength;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getIsCritical() {
        return isCritical;
    }

    public void setIsCritical(Boolean isCritical) {
        this.isCritical = isCritical;
    }

    public AssetDependency.DependencyStatus getStatus() {
        return status;
    }

    public void setStatus(AssetDependency.DependencyStatus status) {
        this.status = status;
    }
}
