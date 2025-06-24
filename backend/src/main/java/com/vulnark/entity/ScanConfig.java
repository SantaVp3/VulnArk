package com.vulnark.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Schema(description = "扫描配置实体")
@Entity
@Table(name = "scan_configs")
public class ScanConfig {
    
    @Schema(description = "配置ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "配置名称")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Schema(description = "配置描述")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Schema(description = "扫描引擎")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanTask.ScanEngine scanEngine;
    
    @Schema(description = "扫描类型")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanTask.ScanType scanType;
    
    @Schema(description = "扫描模板")
    @Enumerated(EnumType.STRING)
    private ScanTask.ScanTemplate scanTemplate;
    
    @Schema(description = "是否为默认配置")
    @Column(nullable = false)
    private Boolean isDefault = false;
    
    @Schema(description = "是否启用")
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Schema(description = "扫描参数配置")
    @Column(columnDefinition = "TEXT")
    private String parameters;
    
    @Schema(description = "超时时间（分钟）")
    private Integer timeoutMinutes = 60;
    
    @Schema(description = "最大并发数")
    private Integer maxConcurrency = 5;
    
    @Schema(description = "端口范围")
    @Column(length = 200)
    private String portRange;
    
    @Schema(description = "排除的端口")
    @Column(length = 200)
    private String excludePorts;
    
    @Schema(description = "扫描深度")
    @Enumerated(EnumType.STRING)
    private ScanDepth scanDepth = ScanDepth.NORMAL;
    
    @Schema(description = "是否扫描UDP端口")
    @Column(nullable = false)
    private Boolean scanUdp = false;
    
    @Schema(description = "是否进行服务识别")
    @Column(nullable = false)
    private Boolean serviceDetection = true;
    
    @Schema(description = "是否进行操作系统识别")
    @Column(nullable = false)
    private Boolean osDetection = true;
    
    @Schema(description = "是否进行脚本扫描")
    @Column(nullable = false)
    private Boolean scriptScan = false;
    
    @Schema(description = "是否进行漏洞扫描")
    @Column(nullable = false)
    private Boolean vulnerabilityScan = true;
    
    @Schema(description = "是否进行Web应用扫描")
    @Column(nullable = false)
    private Boolean webAppScan = false;
    
    @Schema(description = "自定义扫描脚本")
    @Column(columnDefinition = "TEXT")
    private String customScripts;
    
    @Schema(description = "排除的漏洞类型")
    @Column(columnDefinition = "TEXT")
    private String excludeVulnTypes;
    
    @Schema(description = "包含的漏洞类型")
    @Column(columnDefinition = "TEXT")
    private String includeVulnTypes;
    
    @Schema(description = "扫描策略")
    @Column(columnDefinition = "TEXT")
    private String scanPolicy;
    
    @Schema(description = "创建者")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;
    
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
    
    // 扫描深度枚举
    public enum ScanDepth {
        LIGHT("轻量扫描"),
        NORMAL("标准扫描"),
        DEEP("深度扫描"),
        COMPREHENSIVE("全面扫描");
        
        private final String description;
        
        ScanDepth(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
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
    public ScanConfig() {}
    
    public ScanConfig(String name, ScanTask.ScanEngine scanEngine, ScanTask.ScanType scanType) {
        this.name = name;
        this.scanEngine = scanEngine;
        this.scanType = scanType;
    }
    
    // 便捷方法
    public boolean isWebScanConfig() {
        return scanType == ScanTask.ScanType.WEB_SCAN || webAppScan;
    }
    
    public boolean isPortScanConfig() {
        return scanType == ScanTask.ScanType.PORT_SCAN;
    }
    
    public boolean isVulnerabilityScanConfig() {
        return scanType == ScanTask.ScanType.SYSTEM_SCAN || vulnerabilityScan;
    }
    
    public String getDisplayName() {
        return name + " (" + scanEngine.getDescription() + ")";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ScanTask.ScanEngine getScanEngine() { return scanEngine; }
    public void setScanEngine(ScanTask.ScanEngine scanEngine) { this.scanEngine = scanEngine; }
    
    public ScanTask.ScanType getScanType() { return scanType; }
    public void setScanType(ScanTask.ScanType scanType) { this.scanType = scanType; }
    
    public ScanTask.ScanTemplate getScanTemplate() { return scanTemplate; }
    public void setScanTemplate(ScanTask.ScanTemplate scanTemplate) { this.scanTemplate = scanTemplate; }
    
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    
    public Integer getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(Integer timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
    
    public Integer getMaxConcurrency() { return maxConcurrency; }
    public void setMaxConcurrency(Integer maxConcurrency) { this.maxConcurrency = maxConcurrency; }
    
    public String getPortRange() { return portRange; }
    public void setPortRange(String portRange) { this.portRange = portRange; }
    
    public String getExcludePorts() { return excludePorts; }
    public void setExcludePorts(String excludePorts) { this.excludePorts = excludePorts; }
    
    public ScanDepth getScanDepth() { return scanDepth; }
    public void setScanDepth(ScanDepth scanDepth) { this.scanDepth = scanDepth; }
    
    public Boolean getScanUdp() { return scanUdp; }
    public void setScanUdp(Boolean scanUdp) { this.scanUdp = scanUdp; }
    
    public Boolean getServiceDetection() { return serviceDetection; }
    public void setServiceDetection(Boolean serviceDetection) { this.serviceDetection = serviceDetection; }
    
    public Boolean getOsDetection() { return osDetection; }
    public void setOsDetection(Boolean osDetection) { this.osDetection = osDetection; }
    
    public Boolean getScriptScan() { return scriptScan; }
    public void setScriptScan(Boolean scriptScan) { this.scriptScan = scriptScan; }
    
    public Boolean getVulnerabilityScan() { return vulnerabilityScan; }
    public void setVulnerabilityScan(Boolean vulnerabilityScan) { this.vulnerabilityScan = vulnerabilityScan; }
    
    public Boolean getWebAppScan() { return webAppScan; }
    public void setWebAppScan(Boolean webAppScan) { this.webAppScan = webAppScan; }
    
    public String getCustomScripts() { return customScripts; }
    public void setCustomScripts(String customScripts) { this.customScripts = customScripts; }
    
    public String getExcludeVulnTypes() { return excludeVulnTypes; }
    public void setExcludeVulnTypes(String excludeVulnTypes) { this.excludeVulnTypes = excludeVulnTypes; }
    
    public String getIncludeVulnTypes() { return includeVulnTypes; }
    public void setIncludeVulnTypes(String includeVulnTypes) { this.includeVulnTypes = includeVulnTypes; }
    
    public String getScanPolicy() { return scanPolicy; }
    public void setScanPolicy(String scanPolicy) { this.scanPolicy = scanPolicy; }
    
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}
