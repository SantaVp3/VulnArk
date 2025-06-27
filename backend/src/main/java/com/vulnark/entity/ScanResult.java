package com.vulnark.entity;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * 扫描结果实体
 */
@Schema(description = "扫描结果实体")
@Entity
@Table(name = "scan_results")
public class ScanResult {

    @Schema(description = "扫描结果ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "关联扫描任务")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_task_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ScanTask scanTask;

    @Schema(description = "目标主机")
    @Column(nullable = false, length = 255)
    private String targetHost;

    @Schema(description = "目标端口")
    private Integer targetPort;

    @Schema(description = "漏洞名称")
    @Column(nullable = false, length = 500)
    private String vulnerabilityName;

    @Schema(description = "漏洞描述")
    @Column(columnDefinition = "TEXT")
    private String vulnerabilityDescription;

    @Schema(description = "漏洞严重程度")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Schema(description = "漏洞类型")
    @Column(length = 100)
    private String vulnerabilityType;

    @Schema(description = "CVE编号")
    @Column(length = 50)
    private String cveId;

    @Schema(description = "CVSS评分")
    private Double cvssScore;

    @Schema(description = "CVSS向量")
    @Column(length = 200)
    private String cvssVector;

    @Schema(description = "漏洞状态")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VulnerabilityStatus status = VulnerabilityStatus.OPEN;

    @Schema(description = "解决方案")
    @Column(columnDefinition = "TEXT")
    private String solution;

    @Schema(description = "参考链接")
    @Column(name = "reference_links", columnDefinition = "TEXT")
    private String references;

    @Schema(description = "漏洞证明")
    @Column(columnDefinition = "TEXT")
    private String proof;

    @Schema(description = "请求内容")
    @Column(columnDefinition = "TEXT")
    private String request;

    @Schema(description = "响应内容")
    @Column(columnDefinition = "TEXT")
    private String response;

    @Schema(description = "插件ID")
    @Column(length = 50)
    private String pluginId;

    @Schema(description = "插件名称")
    @Column(length = 200)
    private String pluginName;

    @Schema(description = "插件系列")
    @Column(length = 100)
    private String pluginFamily;

    @Schema(description = "外部扫描结果ID")
    @Column(length = 100)
    private String externalResultId;

    @Schema(description = "原始扫描数据")
    @Column(columnDefinition = "LONGTEXT")
    private String rawData;

    @Schema(description = "风险评级")
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Schema(description = "是否误报")
    @Column(nullable = false)
    private Boolean falsePositive = false;

    @Schema(description = "确认状态")
    @Enumerated(EnumType.STRING)
    private ConfirmationStatus confirmationStatus = ConfirmationStatus.UNCONFIRMED;

    @Schema(description = "关联资产")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Asset asset;

    @Schema(description = "关联漏洞")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vulnerability_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vulnerability vulnerability;

    @Schema(description = "发现时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime discoveredTime;

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

    // 严重程度枚举
    public enum Severity {
        CRITICAL("严重"),
        HIGH("高危"),
        MEDIUM("中危"),
        LOW("低危"),
        INFO("信息");

        private final String description;

        Severity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 漏洞状态枚举
    public enum VulnerabilityStatus {
        OPEN("开放"),
        FIXED("已修复"),
        ACCEPTED("已接受"),
        IGNORED("已忽略"),
        RETEST("待复测");

        private final String description;

        VulnerabilityStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 风险等级枚举
    public enum RiskLevel {
        CRITICAL("严重风险"),
        HIGH("高风险"),
        MEDIUM("中等风险"),
        LOW("低风险"),
        NONE("无风险");

        private final String description;

        RiskLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 确认状态枚举
    public enum ConfirmationStatus {
        UNCONFIRMED("未确认"),
        CONFIRMED("已确认"),
        FALSE_POSITIVE("误报"),
        DUPLICATE("重复");

        private final String description;

        ConfirmationStatus(String description) {
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
        if (discoveredTime == null) {
            discoveredTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }

    // 构造函数
    public ScanResult() {}

    public ScanResult(ScanTask scanTask, String targetHost, String vulnerabilityName, Severity severity) {
        this.scanTask = scanTask;
        this.targetHost = targetHost;
        this.vulnerabilityName = vulnerabilityName;
        this.severity = severity;
        this.discoveredTime = LocalDateTime.now();
    }

    // 便捷方法
    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }

    public boolean isHighRisk() {
        return severity == Severity.CRITICAL || severity == Severity.HIGH;
    }

    public boolean isConfirmed() {
        return confirmationStatus == ConfirmationStatus.CONFIRMED;
    }

    public boolean isFalsePositive() {
        return falsePositive || confirmationStatus == ConfirmationStatus.FALSE_POSITIVE;
    }

    public void markAsFalsePositive() {
        this.falsePositive = true;
        this.confirmationStatus = ConfirmationStatus.FALSE_POSITIVE;
        this.updatedTime = LocalDateTime.now();
    }

    public void markAsConfirmed() {
        this.confirmationStatus = ConfirmationStatus.CONFIRMED;
        this.updatedTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScanTask getScanTask() { return scanTask; }
    public void setScanTask(ScanTask scanTask) { this.scanTask = scanTask; }

    public String getTargetHost() { return targetHost; }
    public void setTargetHost(String targetHost) { this.targetHost = targetHost; }

    public Integer getTargetPort() { return targetPort; }
    public void setTargetPort(Integer targetPort) { this.targetPort = targetPort; }

    public String getVulnerabilityName() { return vulnerabilityName; }
    public void setVulnerabilityName(String vulnerabilityName) { this.vulnerabilityName = vulnerabilityName; }

    public String getVulnerabilityDescription() { return vulnerabilityDescription; }
    public void setVulnerabilityDescription(String vulnerabilityDescription) { this.vulnerabilityDescription = vulnerabilityDescription; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getVulnerabilityType() { return vulnerabilityType; }
    public void setVulnerabilityType(String vulnerabilityType) { this.vulnerabilityType = vulnerabilityType; }

    public String getCveId() { return cveId; }
    public void setCveId(String cveId) { this.cveId = cveId; }

    public Double getCvssScore() { return cvssScore; }
    public void setCvssScore(Double cvssScore) { this.cvssScore = cvssScore; }

    public String getCvssVector() { return cvssVector; }
    public void setCvssVector(String cvssVector) { this.cvssVector = cvssVector; }

    public VulnerabilityStatus getStatus() { return status; }
    public void setStatus(VulnerabilityStatus status) { this.status = status; }

    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }

    public String getReferences() { return references; }
    public void setReferences(String references) { this.references = references; }

    public String getProof() { return proof; }
    public void setProof(String proof) { this.proof = proof; }

    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getPluginId() { return pluginId; }
    public void setPluginId(String pluginId) { this.pluginId = pluginId; }

    public String getPluginName() { return pluginName; }
    public void setPluginName(String pluginName) { this.pluginName = pluginName; }

    public String getPluginFamily() { return pluginFamily; }
    public void setPluginFamily(String pluginFamily) { this.pluginFamily = pluginFamily; }

    public String getExternalResultId() { return externalResultId; }
    public void setExternalResultId(String externalResultId) { this.externalResultId = externalResultId; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public Boolean getFalsePositive() { return falsePositive; }
    public void setFalsePositive(Boolean falsePositive) { this.falsePositive = falsePositive; }

    public ConfirmationStatus getConfirmationStatus() { return confirmationStatus; }
    public void setConfirmationStatus(ConfirmationStatus confirmationStatus) { this.confirmationStatus = confirmationStatus; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public Vulnerability getVulnerability() { return vulnerability; }
    public void setVulnerability(Vulnerability vulnerability) { this.vulnerability = vulnerability; }

    public LocalDateTime getDiscoveredTime() { return discoveredTime; }
    public void setDiscoveredTime(LocalDateTime discoveredTime) { this.discoveredTime = discoveredTime; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}
