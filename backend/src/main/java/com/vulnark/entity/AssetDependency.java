package com.vulnark.entity;

import jakarta.persistence.*;
// import lombok.Data;
// import lombok.EqualsAndHashCode;
// import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 资产依赖关系实体
 * 用于描述资产之间的依赖关系
 */
@Entity
@Table(name = "asset_dependencies")
public class AssetDependency {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 源资产ID（依赖方）
     */
    @Column(name = "source_asset_id", nullable = false)
    private Long sourceAssetId;
    
    /**
     * 目标资产ID（被依赖方）
     */
    @Column(name = "target_asset_id", nullable = false)
    private Long targetAssetId;
    
    /**
     * 依赖类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false)
    private DependencyType dependencyType;
    
    /**
     * 依赖强度/重要性
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_strength", nullable = false)
    private DependencyStrength dependencyStrength;
    
    /**
     * 依赖描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 端口信息（如果适用）
     */
    @Column(name = "port")
    private Integer port;
    
    /**
     * 协议信息（如果适用）
     */
    @Column(name = "protocol", length = 50)
    private String protocol;
    
    /**
     * 服务名称（如果适用）
     */
    @Column(name = "service_name", length = 100)
    private String serviceName;
    
    /**
     * 是否为关键依赖
     */
    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false;
    
    /**
     * 依赖状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DependencyStatus status = DependencyStatus.ACTIVE;
    
    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    /**
     * 创建者ID
     */
    @Column(name = "created_by")
    private Long createdBy;
    
    /**
     * 逻辑删除标记
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    // 关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_asset_id", insertable = false, updatable = false)
    private Asset sourceAsset;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_asset_id", insertable = false, updatable = false)
    private Asset targetAsset;
    
    /**
     * 依赖类型枚举
     */
    public enum DependencyType {
        NETWORK("网络依赖"),
        DATABASE("数据库依赖"),
        SERVICE("服务依赖"),
        APPLICATION("应用依赖"),
        INFRASTRUCTURE("基础设施依赖"),
        DATA_FLOW("数据流依赖"),
        AUTHENTICATION("认证依赖"),
        STORAGE("存储依赖"),
        MONITORING("监控依赖"),
        BACKUP("备份依赖"),
        OTHER("其他依赖");
        
        private final String description;
        
        DependencyType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 依赖强度枚举
     */
    public enum DependencyStrength {
        WEAK("弱依赖"),
        MEDIUM("中等依赖"),
        STRONG("强依赖"),
        CRITICAL("关键依赖");
        
        private final String description;
        
        DependencyStrength(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 依赖状态枚举
     */
    public enum DependencyStatus {
        ACTIVE("活跃"),
        INACTIVE("非活跃"),
        BROKEN("已断开"),
        DEPRECATED("已废弃");
        
        private final String description;
        
        DependencyStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdTime = now;
        this.updatedTime = now;
        if (this.deleted == null) {
            this.deleted = false;
        }
        if (this.isCritical == null) {
            this.isCritical = false;
        }
        if (this.status == null) {
            this.status = DependencyStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
    
    /**
     * 检查是否为循环依赖
     */
    public boolean isCircularDependency() {
        return this.sourceAssetId.equals(this.targetAssetId);
    }
    
    /**
     * 获取依赖关系的唯一标识
     */
    public String getDependencyKey() {
        return sourceAssetId + "->" + targetAssetId;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public DependencyStrength getDependencyStrength() {
        return dependencyStrength;
    }

    public void setDependencyStrength(DependencyStrength dependencyStrength) {
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

    public DependencyStatus getStatus() {
        return status;
    }

    public void setStatus(DependencyStatus status) {
        this.status = status;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Asset getSourceAsset() {
        return sourceAsset;
    }

    public void setSourceAsset(Asset sourceAsset) {
        this.sourceAsset = sourceAsset;
    }

    public Asset getTargetAsset() {
        return targetAsset;
    }

    public void setTargetAsset(Asset targetAsset) {
        this.targetAsset = targetAsset;
    }
}
