package com.vulnark.dto;

import com.vulnark.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "项目请求DTO")
public class ProjectRequest {
    
    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称长度不能超过100个字符")
    private String name;
    
    @Schema(description = "项目描述")
    private String description;
    
    @Schema(description = "项目负责人ID")
    @NotNull(message = "项目负责人ID不能为空")
    private Long ownerId;
    
    @Schema(description = "项目状态")
    private Project.Status status;
    
    @Schema(description = "项目类型")
    @Size(max = 50, message = "项目类型长度不能超过50个字符")
    private String type;
    
    @Schema(description = "项目优先级")
    private Project.Priority priority;
    
    @Schema(description = "开始日期")
    private LocalDate startDate;
    
    @Schema(description = "结束日期")
    private LocalDate endDate;
    
    @Schema(description = "预算")
    @DecimalMin(value = "0.0", message = "预算不能小于0")
    private Double budget;
    
    @Schema(description = "项目标签")
    @Size(max = 500, message = "项目标签长度不能超过500个字符")
    private String tags;
    
    @Schema(description = "项目成员数量")
    @Min(value = 0, message = "项目成员数量不能小于0")
    private Integer memberCount;
    
    @Schema(description = "项目进度百分比")
    @Min(value = 0, message = "项目进度不能小于0")
    @Max(value = 100, message = "项目进度不能大于100")
    private Integer progress;
    
    // Constructors
    public ProjectRequest() {}
    
    // Getters and Setters
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
    
    public Project.Status getStatus() {
        return status;
    }
    
    public void setStatus(Project.Status status) {
        this.status = status;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Project.Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Project.Priority priority) {
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
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
}
