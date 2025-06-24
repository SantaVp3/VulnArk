package com.vulnark.dto;

import com.vulnark.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "项目查询请求DTO")
public class ProjectQueryRequest {
    
    @Schema(description = "项目名称关键词")
    private String name;
    
    @Schema(description = "项目状态")
    private Project.Status status;
    
    @Schema(description = "项目优先级")
    private Project.Priority priority;
    
    @Schema(description = "项目类型关键词")
    private String type;
    
    @Schema(description = "项目负责人ID")
    private Long ownerId;
    
    @Schema(description = "开始日期")
    private LocalDate startDate;
    
    @Schema(description = "结束日期")
    private LocalDate endDate;
    
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
    public ProjectQueryRequest() {}
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Project.Status getStatus() {
        return status;
    }
    
    public void setStatus(Project.Status status) {
        this.status = status;
    }
    
    public Project.Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Project.Priority priority) {
        this.priority = priority;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
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
