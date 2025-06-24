package com.vulnark.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Schema(description = "资产检测记录实体")
@Entity
@Table(name = "asset_detections")
public class AssetDetection {
    
    @Schema(description = "检测记录ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "资产ID")
    @Column(nullable = false)
    private Long assetId;
    
    @Schema(description = "检测类型")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetectionType type;
    
    @Schema(description = "检测状态")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DetectionStatus status;
    
    @Schema(description = "检测结果")
    @Enumerated(EnumType.STRING)
    private DetectionResult result;
    
    @Schema(description = "目标地址")
    @Column(nullable = false, length = 500)
    private String target;
    
    @Schema(description = "端口号")
    private Integer port;
    
    @Schema(description = "响应时间（毫秒）")
    private Long responseTime;
    
    @Schema(description = "错误信息")
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Schema(description = "检测详情")
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Schema(description = "HTTP状态码")
    private Integer httpStatusCode;
    
    @Schema(description = "服务横幅信息")
    @Column(columnDefinition = "TEXT")
    private String banner;
    
    @Schema(description = "检测开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Schema(description = "检测结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    // 检测类型枚举
    public enum DetectionType {
        PING("PING检测"),
        TCP_PORT("TCP端口检测"),
        HTTP_SERVICE("HTTP服务检测"),
        HTTPS_SERVICE("HTTPS服务检测"),
        SSH_SERVICE("SSH服务检测"),
        DATABASE_SERVICE("数据库服务检测"),
        CUSTOM_PORT("自定义端口检测");
        
        private final String description;
        
        DetectionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 检测状态枚举
    public enum DetectionStatus {
        PENDING("等待中"),
        RUNNING("检测中"),
        COMPLETED("已完成"),
        FAILED("检测失败"),
        TIMEOUT("检测超时"),
        CANCELLED("已取消");
        
        private final String description;
        
        DetectionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 检测结果枚举
    public enum DetectionResult {
        ONLINE("在线"),
        OFFLINE("离线"),
        TIMEOUT("超时"),
        UNREACHABLE("不可达"),
        FILTERED("被过滤"),
        UNKNOWN("未知");
        
        private final String description;
        
        DetectionResult(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }
    
    // Constructors
    public AssetDetection() {}
    
    public AssetDetection(Long assetId, DetectionType type, String target) {
        this.assetId = assetId;
        this.type = type;
        this.target = target;
        this.status = DetectionStatus.PENDING;
        this.startTime = LocalDateTime.now();
    }
    
    public AssetDetection(Long assetId, DetectionType type, String target, Integer port) {
        this(assetId, type, target);
        this.port = port;
    }
    
    // 便捷方法
    public void markAsStarted() {
        this.status = DetectionStatus.RUNNING;
        this.startTime = LocalDateTime.now();
    }
    
    public void markAsCompleted(DetectionResult result) {
        this.status = DetectionStatus.COMPLETED;
        this.result = result;
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.responseTime = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = DetectionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.responseTime = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
    
    public void markAsTimeout() {
        this.status = DetectionStatus.TIMEOUT;
        this.result = DetectionResult.TIMEOUT;
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.responseTime = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
    
    public boolean isCompleted() {
        return status == DetectionStatus.COMPLETED || 
               status == DetectionStatus.FAILED || 
               status == DetectionStatus.TIMEOUT;
    }
    
    public boolean isOnline() {
        return result == DetectionResult.ONLINE;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    
    public DetectionType getType() { return type; }
    public void setType(DetectionType type) { this.type = type; }
    
    public DetectionStatus getStatus() { return status; }
    public void setStatus(DetectionStatus status) { this.status = status; }
    
    public DetectionResult getResult() { return result; }
    public void setResult(DetectionResult result) { this.result = result; }
    
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    
    public Long getResponseTime() { return responseTime; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public Integer getHttpStatusCode() { return httpStatusCode; }
    public void setHttpStatusCode(Integer httpStatusCode) { this.httpStatusCode = httpStatusCode; }
    
    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
}
