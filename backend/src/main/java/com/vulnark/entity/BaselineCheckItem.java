package com.vulnark.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 基线检查项实体
 */
@Entity
@Table(name = "baseline_check_items")
@Schema(description = "基线检查项")
public class BaselineCheckItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "检查项ID")
    private Long id;

    @Schema(description = "基线检查")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baseline_check_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BaselineCheck baselineCheck;

    @Schema(description = "检查项编号")
    @Column(nullable = false, length = 50)
    private String itemCode;

    @Schema(description = "检查项名称")
    @Column(nullable = false, length = 200)
    private String itemName;

    @Schema(description = "检查项描述")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Schema(description = "检查分类")
    @Column(length = 100)
    private String category;

    @Schema(description = "严重级别")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeverityLevel severity = SeverityLevel.MEDIUM;

    @Schema(description = "检查状态")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.PENDING;

    @Schema(description = "检查结果")
    @Enumerated(EnumType.STRING)
    private ItemResult result;

    @Schema(description = "期望值")
    @Column(columnDefinition = "TEXT")
    private String expectedValue;

    @Schema(description = "实际值")
    @Column(columnDefinition = "TEXT")
    private String actualValue;

    @Schema(description = "检查命令/脚本")
    @Column(columnDefinition = "TEXT")
    private String checkCommand;

    @Schema(description = "检查详情")
    @Column(columnDefinition = "TEXT")
    private String checkDetails;

    @Schema(description = "修复建议")
    @Column(columnDefinition = "TEXT")
    private String remediation;

    @Schema(description = "参考链接")
    @Column(length = 500)
    private String reference;

    @Schema(description = "错误信息")
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Schema(description = "检查时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    @Schema(description = "创建时间")
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    // 严重级别枚举
    public enum SeverityLevel {
        CRITICAL("严重"),
        HIGH("高"),
        MEDIUM("中"),
        LOW("低"),
        INFO("信息");

        private final String description;

        SeverityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 检查项状态枚举
    public enum ItemStatus {
        PENDING("待检查"),
        RUNNING("检查中"),
        COMPLETED("已完成"),
        SKIPPED("已跳过"),
        ERROR("检查错误");

        private final String description;

        ItemStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 检查项结果枚举
    public enum ItemResult {
        PASS("通过"),
        FAIL("失败"),
        WARNING("警告"),
        NOT_APPLICABLE("不适用");

        private final String description;

        ItemResult(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造函数
    public BaselineCheckItem() {}

    // 业务方法
    public void markAsCompleted(ItemResult result, String actualValue) {
        this.status = ItemStatus.COMPLETED;
        this.result = result;
        this.actualValue = actualValue;
        this.checkTime = LocalDateTime.now();
    }

    public void markAsSkipped(String reason) {
        this.status = ItemStatus.SKIPPED;
        this.errorMessage = reason;
        this.checkTime = LocalDateTime.now();
    }

    public void markAsError(String errorMessage) {
        this.status = ItemStatus.ERROR;
        this.errorMessage = errorMessage;
        this.checkTime = LocalDateTime.now();
    }

    public boolean isPassed() {
        return result == ItemResult.PASS;
    }

    public boolean isFailed() {
        return result == ItemResult.FAIL;
    }

    public boolean isWarning() {
        return result == ItemResult.WARNING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BaselineCheck getBaselineCheck() { return baselineCheck; }
    public void setBaselineCheck(BaselineCheck baselineCheck) { this.baselineCheck = baselineCheck; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public SeverityLevel getSeverity() { return severity; }
    public void setSeverity(SeverityLevel severity) { this.severity = severity; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public ItemResult getResult() { return result; }
    public void setResult(ItemResult result) { this.result = result; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public String getActualValue() { return actualValue; }
    public void setActualValue(String actualValue) { this.actualValue = actualValue; }

    public String getCheckCommand() { return checkCommand; }
    public void setCheckCommand(String checkCommand) { this.checkCommand = checkCommand; }

    public String getCheckDetails() { return checkDetails; }
    public void setCheckDetails(String checkDetails) { this.checkDetails = checkDetails; }

    public String getRemediation() { return remediation; }
    public void setRemediation(String remediation) { this.remediation = remediation; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCheckTime() { return checkTime; }
    public void setCheckTime(LocalDateTime checkTime) { this.checkTime = checkTime; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}
