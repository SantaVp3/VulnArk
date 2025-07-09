package com.vulnark.service;

import com.vulnark.entity.BaselineRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BaselineRuleService {
    
    /**
     * 创建基线检查规则
     */
    BaselineRule createRule(BaselineRule rule);
    
    /**
     * 更新基线检查规则
     */
    BaselineRule updateRule(String ruleId, BaselineRule rule);
    
    /**
     * 删除基线检查规则
     */
    void deleteRule(String ruleId);
    
    /**
     * 根据规则ID获取规则
     */
    Optional<BaselineRule> getRule(String ruleId);
    
    /**
     * 获取所有规则
     */
    Page<BaselineRule> getAllRules(Pageable pageable);
    
    /**
     * 搜索规则
     */
    Page<BaselineRule> searchRules(String keyword, Pageable pageable);
    
    /**
     * 根据平台获取启用的规则
     */
    List<BaselineRule> getRulesByPlatform(BaselineRule.Platform platform);
    
    /**
     * 根据标准获取启用的规则
     */
    List<BaselineRule> getRulesByStandard(String standard);
    
    /**
     * 根据标准和版本获取启用的规则
     */
    List<BaselineRule> getRulesByStandardAndVersion(String standard, String version);
    
    /**
     * 根据分类获取启用的规则
     */
    List<BaselineRule> getRulesByCategory(String category);
    
    /**
     * 根据严重程度获取启用的规则
     */
    List<BaselineRule> getRulesBySeverity(BaselineRule.Severity severity);
    
    /**
     * 启用/禁用规则
     */
    void toggleRuleStatus(String ruleId, boolean enabled);
    
    /**
     * 获取所有标准
     */
    List<String> getAllStandards();
    
    /**
     * 获取所有分类
     */
    List<String> getAllCategories();
    
    /**
     * 根据标准获取版本列表
     */
    List<String> getVersionsByStandard(String standard);
    
    /**
     * 批量导入规则
     */
    void importRules(List<BaselineRule> rules);
    
    /**
     * 初始化默认规则
     */
    void initializeDefaultRules();
    
    /**
     * 获取规则统计信息
     */
    RuleStats getRuleStats();
    
    /**
     * 规则统计信息
     */
    class RuleStats {
        private long totalRules;
        private long enabledRules;
        private long windowsRules;
        private long linuxRules;
        private long criticalRules;
        private long highRules;
        private long mediumRules;
        private long lowRules;
        
        public RuleStats(long totalRules, long enabledRules, long windowsRules, long linuxRules,
                        long criticalRules, long highRules, long mediumRules, long lowRules) {
            this.totalRules = totalRules;
            this.enabledRules = enabledRules;
            this.windowsRules = windowsRules;
            this.linuxRules = linuxRules;
            this.criticalRules = criticalRules;
            this.highRules = highRules;
            this.mediumRules = mediumRules;
            this.lowRules = lowRules;
        }
        
        // Getters
        public long getTotalRules() { return totalRules; }
        public long getEnabledRules() { return enabledRules; }
        public long getWindowsRules() { return windowsRules; }
        public long getLinuxRules() { return linuxRules; }
        public long getCriticalRules() { return criticalRules; }
        public long getHighRules() { return highRules; }
        public long getMediumRules() { return mediumRules; }
        public long getLowRules() { return lowRules; }
    }
}
