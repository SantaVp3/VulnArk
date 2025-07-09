package com.vulnark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 扫描结果实体
 */
@Entity
@Table(name = "scan_results")
public class ScanResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scan_id", nullable = false)
    private Long scanId;
    
    @Column(name = "tool_name", nullable = false, length = 50)
    private String toolName;
    
    @Column(name = "vuln_type", length = 100)
    private String vulnType;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "url", nullable = false, length = 500)
    private String url;
    
    @Column(name = "severity", nullable = false, length = 20)
    private String severity;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;
    
    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution;
    
    @Column(name = "reference", columnDefinition = "TEXT")
    private String reference;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // 构造函数
    public ScanResult() {
        this.createdTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    public ScanResult(Long scanId, String toolName, String title, String url, String severity) {
        this();
        this.scanId = scanId;
        this.toolName = toolName;
        this.title = title;
        this.url = url;
        this.severity = severity;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getScanId() {
        return scanId;
    }
    
    public void setScanId(Long scanId) {
        this.scanId = scanId;
    }
    
    public String getToolName() {
        return toolName;
    }
    
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    public String getVulnType() {
        return vulnType;
    }
    
    public void setVulnType(String vulnType) {
        this.vulnType = vulnType;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPayload() {
        return payload;
    }
    
    public void setPayload(String payload) {
        this.payload = payload;
    }
    
    public String getSolution() {
        return solution;
    }
    
    public void setSolution(String solution) {
        this.solution = solution;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
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
    
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
}
