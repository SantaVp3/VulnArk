package com.vulnark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 基线扫描实体
 */
@Entity
@Table(name = "baseline_scans")
public class BaselineScan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "scan_name", nullable = false)
    private String scanName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "asset_id", nullable = false)
    private Long assetId;
    
    @Column(name = "scan_type", nullable = false)
    private String scanType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScanStatus status = ScanStatus.PENDING;
    
    @Column(name = "total_checks")
    private Integer totalChecks = 0;
    
    @Column(name = "passed_checks")
    private Integer passedChecks = 0;
    
    @Column(name = "failed_checks")
    private Integer failedChecks = 0;
    
    @Column(name = "warning_checks")
    private Integer warningChecks = 0;
    
    @Column(name = "compliance_score")
    private Double complianceScore = 0.0;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    public enum ScanStatus {
        PENDING,    // 等待执行
        RUNNING,    // 执行中
        COMPLETED,  // 已完成
        FAILED,     // 执行失败
        CANCELLED   // 已取消
    }
    
    // 构造函数
    public BaselineScan() {
        this.createdTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getScanName() {
        return scanName;
    }
    
    public void setScanName(String scanName) {
        this.scanName = scanName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getAssetId() {
        return assetId;
    }
    
    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }
    
    public String getScanType() {
        return scanType;
    }
    
    public void setScanType(String scanType) {
        this.scanType = scanType;
    }
    
    public ScanStatus getStatus() {
        return status;
    }
    
    public void setStatus(ScanStatus status) {
        this.status = status;
    }
    
    public Integer getTotalChecks() {
        return totalChecks;
    }
    
    public void setTotalChecks(Integer totalChecks) {
        this.totalChecks = totalChecks;
    }
    
    public Integer getPassedChecks() {
        return passedChecks;
    }
    
    public void setPassedChecks(Integer passedChecks) {
        this.passedChecks = passedChecks;
    }
    
    public Integer getFailedChecks() {
        return failedChecks;
    }
    
    public void setFailedChecks(Integer failedChecks) {
        this.failedChecks = failedChecks;
    }
    
    public Integer getWarningChecks() {
        return warningChecks;
    }
    
    public void setWarningChecks(Integer warningChecks) {
        this.warningChecks = warningChecks;
    }
    
    public Double getComplianceScore() {
        return complianceScore;
    }
    
    public void setComplianceScore(Double complianceScore) {
        this.complianceScore = complianceScore;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
