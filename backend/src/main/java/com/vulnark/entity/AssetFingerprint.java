package com.vulnark.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Schema(description = "资产指纹识别实体")
@Entity
@Table(name = "asset_fingerprints")
public class AssetFingerprint {
    
    @Schema(description = "指纹ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Schema(description = "资产ID")
    @Column(nullable = false)
    private Long assetId;
    
    @Schema(description = "指纹类型")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FingerprintType type;
    
    @Schema(description = "识别到的技术/产品名称")
    @Column(nullable = false, length = 200)
    private String name;
    
    @Schema(description = "版本信息")
    @Column(length = 100)
    private String version;
    
    @Schema(description = "厂商信息")
    @Column(length = 100)
    private String vendor;
    
    @Schema(description = "置信度（0-100）")
    @Column(nullable = false)
    private Integer confidence;
    
    @Schema(description = "识别方法")
    @Enumerated(EnumType.STRING)
    private IdentificationMethod method;
    
    @Schema(description = "特征信息")
    @Column(columnDefinition = "TEXT")
    private String signature;
    
    @Schema(description = "端口号")
    private Integer port;
    
    @Schema(description = "协议")
    @Column(length = 20)
    private String protocol;
    
    @Schema(description = "服务横幅")
    @Column(columnDefinition = "TEXT")
    private String banner;
    
    @Schema(description = "HTTP响应头")
    @Column(columnDefinition = "TEXT")
    private String httpHeaders;
    
    @Schema(description = "页面标题")
    @Column(length = 500)
    private String pageTitle;
    
    @Schema(description = "错误页面信息")
    @Column(columnDefinition = "TEXT")
    private String errorPage;
    
    @Schema(description = "特征文件路径")
    @Column(length = 500)
    private String signatureFile;
    
    @Schema(description = "额外信息")
    @Column(columnDefinition = "TEXT")
    private String extraInfo;
    
    @Schema(description = "是否活跃")
    @Column(nullable = false)
    private Boolean active = true;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    
    // 指纹类型枚举
    public enum FingerprintType {
        WEB_SERVER("Web服务器"),
        APPLICATION_SERVER("应用服务器"),
        DATABASE("数据库"),
        OPERATING_SYSTEM("操作系统"),
        WEB_FRAMEWORK("Web框架"),
        CMS("内容管理系统"),
        PROGRAMMING_LANGUAGE("编程语言"),
        MIDDLEWARE("中间件"),
        LOAD_BALANCER("负载均衡器"),
        WAF("Web应用防火墙"),
        CDN("内容分发网络"),
        CACHE("缓存系统"),
        MONITORING("监控系统"),
        SECURITY_PRODUCT("安全产品"),
        NETWORK_DEVICE("网络设备"),
        IOT_DEVICE("物联网设备"),
        OTHER("其他");
        
        private final String description;
        
        FingerprintType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 识别方法枚举
    public enum IdentificationMethod {
        HTTP_HEADER("HTTP响应头"),
        SERVER_BANNER("服务横幅"),
        ERROR_PAGE("错误页面"),
        SIGNATURE_FILE("特征文件"),
        PAGE_CONTENT("页面内容"),
        COOKIE("Cookie信息"),
        REDIRECT("重定向"),
        SSL_CERTIFICATE("SSL证书"),
        PORT_SERVICE("端口服务"),
        FAVICON("网站图标"),
        ROBOTS_TXT("robots.txt"),
        SITEMAP("站点地图"),
        META_TAG("Meta标签"),
        JAVASCRIPT("JavaScript"),
        CSS("CSS样式"),
        CUSTOM_RULE("自定义规则");
        
        private final String description;
        
        IdentificationMethod(String description) {
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
    
    // Constructors
    public AssetFingerprint() {}
    
    public AssetFingerprint(Long assetId, FingerprintType type, String name, Integer confidence) {
        this.assetId = assetId;
        this.type = type;
        this.name = name;
        this.confidence = confidence;
    }
    
    // 便捷方法
    public boolean isHighConfidence() {
        return confidence >= 80;
    }
    
    public boolean isMediumConfidence() {
        return confidence >= 50 && confidence < 80;
    }
    
    public boolean isLowConfidence() {
        return confidence < 50;
    }
    
    public String getFullName() {
        if (version != null && !version.isEmpty()) {
            return name + " " + version;
        }
        return name;
    }
    
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder(name);
        if (version != null && !version.isEmpty()) {
            sb.append(" ").append(version);
        }
        if (vendor != null && !vendor.isEmpty()) {
            sb.append(" (").append(vendor).append(")");
        }
        return sb.toString();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }
    
    public FingerprintType getType() { return type; }
    public void setType(FingerprintType type) { this.type = type; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    
    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }
    
    public IdentificationMethod getMethod() { return method; }
    public void setMethod(IdentificationMethod method) { this.method = method; }
    
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    
    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }
    
    public String getHttpHeaders() { return httpHeaders; }
    public void setHttpHeaders(String httpHeaders) { this.httpHeaders = httpHeaders; }
    
    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
    
    public String getErrorPage() { return errorPage; }
    public void setErrorPage(String errorPage) { this.errorPage = errorPage; }
    
    public String getSignatureFile() { return signatureFile; }
    public void setSignatureFile(String signatureFile) { this.signatureFile = signatureFile; }
    
    public String getExtraInfo() { return extraInfo; }
    public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
}
