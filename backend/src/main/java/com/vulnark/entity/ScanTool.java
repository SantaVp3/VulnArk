package com.vulnark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 扫描工具实体
 */
@Entity
@Table(name = "scan_tools")
public class ScanTool {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "current_version")
    private String currentVersion;
    
    @Column(name = "latest_version")
    private String latestVersion;
    
    @Column(name = "install_path")
    private String installPath;
    
    @Column(name = "config_path")
    private String configPath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ToolStatus status = ToolStatus.NOT_INSTALLED;
    
    @Column(name = "auto_update")
    private Boolean autoUpdate = true;
    
    @Column(name = "download_url")
    private String downloadUrl;
    
    @Column(name = "checksum")
    private String checksum;
    
    @Column(name = "last_check_time")
    private LocalDateTime lastCheckTime;
    
    @Column(name = "last_update_time")
    private LocalDateTime lastUpdateTime;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "download_progress")
    private Integer downloadProgress = 0;
    
    public enum ToolStatus {
        NOT_INSTALLED,  // 未安装
        INSTALLING,     // 安装中
        INSTALLED,      // 已安装
        UPDATING,       // 更新中
        ERROR,          // 错误
        OUTDATED        // 版本过期
    }
    
    // 构造函数
    public ScanTool() {}
    
    public ScanTool(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
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
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getCurrentVersion() {
        return currentVersion;
    }
    
    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }
    
    public String getLatestVersion() {
        return latestVersion;
    }
    
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }
    
    public String getInstallPath() {
        return installPath;
    }
    
    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }
    
    public String getConfigPath() {
        return configPath;
    }
    
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
    
    public ToolStatus getStatus() {
        return status;
    }
    
    public void setStatus(ToolStatus status) {
        this.status = status;
    }
    
    public Boolean getAutoUpdate() {
        return autoUpdate;
    }
    
    public void setAutoUpdate(Boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    public LocalDateTime getLastCheckTime() {
        return lastCheckTime;
    }
    
    public void setLastCheckTime(LocalDateTime lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }
    
    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public Integer getDownloadProgress() {
        return downloadProgress;
    }
    
    public void setDownloadProgress(Integer downloadProgress) {
        this.downloadProgress = downloadProgress;
    }
    
    /**
     * 检查是否需要更新
     */
    public boolean needsUpdate() {
        if (currentVersion == null || latestVersion == null) {
            return false;
        }
        return !currentVersion.equals(latestVersion);
    }
    
    /**
     * 检查是否已安装
     */
    public boolean isInstalled() {
        return status == ToolStatus.INSTALLED || status == ToolStatus.OUTDATED;
    }
}
