package com.vulnark.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基线检查实体
 */
@Entity
@Table(name = "baseline_checks")
@Schema(description = "基线检查")
public class BaselineCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "检查ID")
    private Long id;

    @Schema(description = "检查名称")
    @Column(nullable = false, length = 200)
    private String name;

    @Schema(description = "检查描述")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Schema(description = "检查类型")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckType checkType;

    @Schema(description = "目标资产")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Asset asset;

    @Schema(description = "检查状态")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckStatus status = CheckStatus.PENDING;

    @Schema(description = "检查结果")
    @Enumerated(EnumType.STRING)
    private CheckResult result;

    @Schema(description = "检查进度(0-100)")
    private Integer progress = 0;

    @Schema(description = "总检查项数")
    private Integer totalItems = 0;

    @Schema(description = "通过项数")
    private Integer passedItems = 0;

    @Schema(description = "失败项数")
    private Integer failedItems = 0;

    @Schema(description = "警告项数")
    private Integer warningItems = 0;

    @Schema(description = "跳过项数")
    private Integer skippedItems = 0;

    @Schema(description = "合规分数(0-100)")
    private Double complianceScore = 0.0;

    @Schema(description = "检查配置")
    @Column(columnDefinition = "TEXT")
    private String checkConfig;

    @Schema(description = "检查报告路径")
    @Column(length = 500)
    private String reportPath;

    @Schema(description = "错误信息")
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Schema(description = "创建者")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Schema(description = "是否删除")
    private Boolean deleted = false;

    // 检查类型枚举
    public enum CheckType {
        SYSTEM_SECURITY("系统安全基线"),
        NETWORK_SECURITY("网络安全基线"),
        DATABASE_SECURITY("数据库安全基线"),
        WEB_SECURITY("Web应用安全基线"),
        MIDDLEWARE_SECURITY("中间件安全基线"),
        CLOUD_SECURITY("云安全基线"),
        CUSTOM("自定义基线");

        private final String description;

        CheckType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 检查状态枚举
    public enum CheckStatus {
        PENDING("待检查"),
        RUNNING("检查中"),
        COMPLETED("已完成"),
        FAILED("检查失败"),
        CANCELLED("已取消");

        private final String description;

        CheckStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 检查结果枚举
    public enum CheckResult {
        PASS("通过"),
        FAIL("失败"),
        WARNING("警告"),
        PARTIAL("部分通过");

        private final String description;

        CheckResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造函数
    public BaselineCheck() {}

    // 业务方法
    public boolean canStart() {
        return status == CheckStatus.PENDING;
    }

    public boolean canCancel() {
        return status == CheckStatus.PENDING || status == CheckStatus.RUNNING;
    }

    public boolean isCompleted() {
        return status == CheckStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == CheckStatus.FAILED;
    }

    public void markAsStarted() {
        this.status = CheckStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = CheckStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.progress = 100;
        calculateResult();
    }

    public void markAsFailed(String errorMessage) {
        this.status = CheckStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void markAsCancelled() {
        this.status = CheckStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    public void updateProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }

    private void calculateResult() {
        if (totalItems == 0) {
            this.result = CheckResult.PASS;
            this.complianceScore = 100.0;
            return;
        }

        double passRate = (double) passedItems / totalItems;
        this.complianceScore = passRate * 100;

        if (failedItems == 0) {
            this.result = warningItems > 0 ? CheckResult.WARNING : CheckResult.PASS;
        } else if (passedItems == 0) {
            this.result = CheckResult.FAIL;
        } else {
            this.result = CheckResult.PARTIAL;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CheckType getCheckType() { return checkType; }
    public void setCheckType(CheckType checkType) { this.checkType = checkType; }

    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }

    public CheckStatus getStatus() { return status; }
    public void setStatus(CheckStatus status) { this.status = status; }

    public CheckResult getResult() { return result; }
    public void setResult(CheckResult result) { this.result = result; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }

    public Integer getPassedItems() { return passedItems; }
    public void setPassedItems(Integer passedItems) { this.passedItems = passedItems; }

    public Integer getFailedItems() { return failedItems; }
    public void setFailedItems(Integer failedItems) { this.failedItems = failedItems; }

    public Integer getWarningItems() { return warningItems; }
    public void setWarningItems(Integer warningItems) { this.warningItems = warningItems; }

    public Integer getSkippedItems() { return skippedItems; }
    public void setSkippedItems(Integer skippedItems) { this.skippedItems = skippedItems; }

    public Double getComplianceScore() { return complianceScore; }
    public void setComplianceScore(Double complianceScore) { this.complianceScore = complianceScore; }

    public String getCheckConfig() { return checkConfig; }
    public void setCheckConfig(String checkConfig) { this.checkConfig = checkConfig; }

    public String getReportPath() { return reportPath; }
    public void setReportPath(String reportPath) { this.reportPath = reportPath; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}
