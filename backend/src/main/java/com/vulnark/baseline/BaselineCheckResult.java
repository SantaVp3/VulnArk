package com.vulnark.baseline;

import com.vulnark.entity.BaselineCheckItem;

import java.util.List;

/**
 * 基线检查结果
 */
public class BaselineCheckResult {
    
    private Long baselineCheckId;
    private Long assetId;
    private List<BaselineCheckItem> checkItems;
    private Integer totalItems = 0;
    private Integer passedItems = 0;
    private Integer failedItems = 0;
    private Integer warningItems = 0;
    private Integer skippedItems = 0;
    private Double complianceScore = 0.0;
    private String errorMessage;
    
    // 构造函数
    public BaselineCheckResult() {}
    
    // Getters and Setters
    public Long getBaselineCheckId() {
        return baselineCheckId;
    }
    
    public void setBaselineCheckId(Long baselineCheckId) {
        this.baselineCheckId = baselineCheckId;
    }
    
    public Long getAssetId() {
        return assetId;
    }
    
    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }
    
    public List<BaselineCheckItem> getCheckItems() {
        return checkItems;
    }
    
    public void setCheckItems(List<BaselineCheckItem> checkItems) {
        this.checkItems = checkItems;
    }
    
    public Integer getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
    
    public Integer getPassedItems() {
        return passedItems;
    }
    
    public void setPassedItems(Integer passedItems) {
        this.passedItems = passedItems;
    }
    
    public Integer getFailedItems() {
        return failedItems;
    }
    
    public void setFailedItems(Integer failedItems) {
        this.failedItems = failedItems;
    }
    
    public Integer getWarningItems() {
        return warningItems;
    }
    
    public void setWarningItems(Integer warningItems) {
        this.warningItems = warningItems;
    }
    
    public Integer getSkippedItems() {
        return skippedItems;
    }
    
    public void setSkippedItems(Integer skippedItems) {
        this.skippedItems = skippedItems;
    }
    
    public Double getComplianceScore() {
        return complianceScore;
    }
    
    public void setComplianceScore(Double complianceScore) {
        this.complianceScore = complianceScore;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
