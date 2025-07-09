package com.vulnark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "baseline_results")
public class BaselineResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private BaselineTask task;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;
    
    @Column(nullable = false)
    private String checkId; // 检查项ID
    
    @Column(nullable = false)
    private String checkName; // 检查项名称
    
    @Column(nullable = false)
    private String category; // 检查分类
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Column(columnDefinition = "TEXT")
    private String description; // 检查项描述
    
    @Column(columnDefinition = "TEXT")
    private String expectedValue; // 期望值
    
    @Column(columnDefinition = "TEXT")
    private String actualValue; // 实际值
    
    @Column(columnDefinition = "TEXT")
    private String evidence; // 检查证据
    
    @Column(columnDefinition = "TEXT")
    private String recommendation; // 修复建议
    
    @Column(columnDefinition = "TEXT")
    private String reference; // 参考标准
    
    private Double score; // 得分
    
    @Column(nullable = false)
    private LocalDateTime checkTime;
    
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
    
    public enum Status {
        PASS,
        FAIL,
        WARNING,
        NOT_APPLICABLE,
        ERROR
    }
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (checkTime == null) {
            checkTime = LocalDateTime.now();
        }
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
    
    public BaselineTask getTask() {
        return task;
    }
    
    public void setTask(BaselineTask task) {
        this.task = task;
    }
    
    public Agent getAgent() {
        return agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }
    
    public String getCheckId() {
        return checkId;
    }
    
    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }
    
    public String getCheckName() {
        return checkName;
    }
    
    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getExpectedValue() {
        return expectedValue;
    }
    
    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }
    
    public String getActualValue() {
        return actualValue;
    }
    
    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }
    
    public String getEvidence() {
        return evidence;
    }
    
    public void setEvidence(String evidence) {
        this.evidence = evidence;
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
    
    public Double getScore() {
        return score;
    }
    
    public void setScore(Double score) {
        this.score = score;
    }
    
    public LocalDateTime getCheckTime() {
        return checkTime;
    }
    
    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
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
