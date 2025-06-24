package com.vulnark.dto;

import com.vulnark.entity.Asset;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "资产查询请求DTO")
public class AssetQueryRequest {
    
    @Schema(description = "资产名称关键词")
    private String name;
    
    @Schema(description = "资产类型")
    private Asset.AssetType type;
    
    @Schema(description = "资产状态")
    private Asset.Status status;
    
    @Schema(description = "重要性等级")
    private Asset.Importance importance;
    
    @Schema(description = "所属项目ID")
    private Long projectId;
    
    @Schema(description = "负责人ID")
    private Long ownerId;
    
    @Schema(description = "IP地址关键词")
    private String ipAddress;
    
    @Schema(description = "域名关键词")
    private String domain;
    
    @Schema(description = "搜索关键词")
    private String keyword;
    
    @Schema(description = "页码", example = "0")
    private Integer page = 0;
    
    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
    
    @Schema(description = "排序字段", example = "createdTime")
    private String sortBy = "createdTime";
    
    @Schema(description = "排序方向", example = "desc")
    private String sortDir = "desc";
    
    // Constructors
    public AssetQueryRequest() {}
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Asset.AssetType getType() {
        return type;
    }
    
    public void setType(Asset.AssetType type) {
        this.type = type;
    }
    
    public Asset.Status getStatus() {
        return status;
    }
    
    public void setStatus(Asset.Status status) {
        this.status = status;
    }
    
    public Asset.Importance getImportance() {
        return importance;
    }
    
    public void setImportance(Asset.Importance importance) {
        this.importance = importance;
    }
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortDir() {
        return sortDir;
    }
    
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
