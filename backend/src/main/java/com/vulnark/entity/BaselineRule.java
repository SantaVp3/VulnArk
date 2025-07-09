package com.vulnark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "baseline_rules")
public class BaselineRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String ruleId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String category;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;
    
    @Column(nullable = false)
    private String standard; // CIS, NIST, 等保等
    
    @Column(nullable = false)
    private String version; // 标准版本
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String checkScript; // 检查脚本内容
    
    @Column(columnDefinition = "TEXT")
    private String expectedValue; // 期望值
    
    @Column(columnDefinition = "TEXT")
    private String recommendation; // 修复建议
    
    @Column(columnDefinition = "TEXT")
    private String reference; // 参考链接
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    private Double score = 1.0; // 检查项得分
    
    @Column(columnDefinition = "TEXT")
    private String tags; // 标签，JSON格式
    
    @Column(nullable = false)
    private LocalDateTime createdTime;
    
    @Column(nullable = false)
    private LocalDateTime updatedTime;
    
    public enum Severity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        INFO
    }
    
    public enum Platform {
        WINDOWS,
        LINUX,
        BOTH
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
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public Platform getPlatform() {
        return platform;
    }
    
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
    
    public String getStandard() {
        return standard;
    }
    
    public void setStandard(String standard) {
        this.standard = standard;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getCheckScript() {
        return checkScript;
    }
    
    public void setCheckScript(String checkScript) {
        this.checkScript = checkScript;
    }
    
    public String getExpectedValue() {
        return expectedValue;
    }
    
    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
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
}
