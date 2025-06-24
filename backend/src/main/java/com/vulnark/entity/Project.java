package com.vulnark.entity;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "项目实体")
@Entity
@Table(name = "projects")
public class Project {
    
    @Schema(description = "项目ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "项目名称")
    @Column(nullable = false, length = 100)
    private String name;
    
    @Schema(description = "项目描述")
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Schema(description = "项目负责人ID")
    @Column(nullable = false)
    private Long ownerId;
    
    @Schema(description = "项目状态")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;
    
    @Schema(description = "项目类型")
    @Column(length = 50)
    private String type;
    
    @Schema(description = "项目优先级")
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    
    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @Schema(description = "预算")
    private Double budget;
    
    @Schema(description = "项目标签")
    @Column(length = 500)
    private String tags;
    
    @Schema(description = "项目成员数量")
    private Integer memberCount = 0;
    
    @Schema(description = "漏洞数量")
    private Integer vulnerabilityCount = 0;
    
    @Schema(description = "资产数量")
    private Integer assetCount = 0;
    
    @Schema(description = "项目进度百分比")
    private Integer progress = 0;
    
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
    
    // 枚举定义
    public enum Status {
        ACTIVE, INACTIVE, COMPLETED, ARCHIVED, SUSPENDED
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
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
    public Project() {}
    
    public Project(String name, String description, Long ownerId) {
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public Double getBudget() {
        return budget;
    }
    
    public void setBudget(Double budget) {
        this.budget = budget;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Integer getMemberCount() {
        return memberCount;
    }
    
    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
    
    public Integer getVulnerabilityCount() {
        return vulnerabilityCount;
    }
    
    public void setVulnerabilityCount(Integer vulnerabilityCount) {
        this.vulnerabilityCount = vulnerabilityCount;
    }
    
    public Integer getAssetCount() {
        return assetCount;
    }
    
    public void setAssetCount(Integer assetCount) {
        this.assetCount = assetCount;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
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
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
