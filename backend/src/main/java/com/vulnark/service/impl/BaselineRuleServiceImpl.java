package com.vulnark.service.impl;

import com.vulnark.entity.BaselineRule;
import com.vulnark.repository.BaselineRuleRepository;
import com.vulnark.service.BaselineRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class BaselineRuleServiceImpl implements BaselineRuleService {
    
    private static final Logger logger = LoggerFactory.getLogger(BaselineRuleServiceImpl.class);
    
    @Autowired
    private BaselineRuleRepository baselineRuleRepository;
    
    @Override
    public BaselineRule createRule(BaselineRule rule) {
        logger.info("创建基线检查规则: {}", rule.getName());
        
        // 生成唯一的规则ID
        if (rule.getRuleId() == null || rule.getRuleId().isEmpty()) {
            rule.setRuleId(UUID.randomUUID().toString());
        }
        
        // 检查规则ID是否已存在
        if (baselineRuleRepository.findByRuleId(rule.getRuleId()).isPresent()) {
            throw new RuntimeException("规则ID已存在: " + rule.getRuleId());
        }
        
        return baselineRuleRepository.save(rule);
    }
    
    @Override
    public BaselineRule updateRule(String ruleId, BaselineRule rule) {
        logger.info("更新基线检查规则: {}", ruleId);
        
        Optional<BaselineRule> existingRuleOpt = baselineRuleRepository.findByRuleId(ruleId);
        if (existingRuleOpt.isEmpty()) {
            throw new RuntimeException("规则不存在: " + ruleId);
        }
        
        BaselineRule existingRule = existingRuleOpt.get();
        
        // 更新字段
        existingRule.setName(rule.getName());
        existingRule.setCategory(rule.getCategory());
        existingRule.setDescription(rule.getDescription());
        existingRule.setSeverity(rule.getSeverity());
        existingRule.setPlatform(rule.getPlatform());
        existingRule.setStandard(rule.getStandard());
        existingRule.setVersion(rule.getVersion());
        existingRule.setCheckScript(rule.getCheckScript());
        existingRule.setExpectedValue(rule.getExpectedValue());
        existingRule.setRecommendation(rule.getRecommendation());
        existingRule.setReference(rule.getReference());
        existingRule.setEnabled(rule.getEnabled());
        existingRule.setScore(rule.getScore());
        existingRule.setTags(rule.getTags());
        
        return baselineRuleRepository.save(existingRule);
    }
    
    @Override
    public void deleteRule(String ruleId) {
        logger.info("删除基线检查规则: {}", ruleId);
        
        Optional<BaselineRule> ruleOpt = baselineRuleRepository.findByRuleId(ruleId);
        if (ruleOpt.isEmpty()) {
            throw new RuntimeException("规则不存在: " + ruleId);
        }
        
        baselineRuleRepository.delete(ruleOpt.get());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BaselineRule> getRule(String ruleId) {
        return baselineRuleRepository.findByRuleId(ruleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BaselineRule> getAllRules(Pageable pageable) {
        return baselineRuleRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<BaselineRule> searchRules(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllRules(pageable);
        }
        return baselineRuleRepository.findByKeyword(keyword.trim(), pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BaselineRule> getRulesByPlatform(BaselineRule.Platform platform) {
        return baselineRuleRepository.findByPlatformOrBothAndEnabledTrue(platform);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BaselineRule> getRulesByStandard(String standard) {
        return baselineRuleRepository.findByStandardAndEnabledTrue(standard);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BaselineRule> getRulesByStandardAndVersion(String standard, String version) {
        return baselineRuleRepository.findByStandardAndVersionAndEnabledTrue(standard, version);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BaselineRule> getRulesByCategory(String category) {
        return baselineRuleRepository.findByCategoryAndEnabledTrue(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<BaselineRule> getRulesBySeverity(BaselineRule.Severity severity) {
        return baselineRuleRepository.findBySeverityAndEnabledTrue(severity);
    }
    
    @Override
    public void toggleRuleStatus(String ruleId, boolean enabled) {
        logger.info("切换规则状态: {} -> {}", ruleId, enabled);
        
        Optional<BaselineRule> ruleOpt = baselineRuleRepository.findByRuleId(ruleId);
        if (ruleOpt.isEmpty()) {
            throw new RuntimeException("规则不存在: " + ruleId);
        }
        
        BaselineRule rule = ruleOpt.get();
        rule.setEnabled(enabled);
        baselineRuleRepository.save(rule);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAllStandards() {
        return baselineRuleRepository.findDistinctStandards();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return baselineRuleRepository.findDistinctCategories();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<String> getVersionsByStandard(String standard) {
        return baselineRuleRepository.findDistinctVersionsByStandard(standard);
    }
    
    @Override
    public void importRules(List<BaselineRule> rules) {
        logger.info("批量导入基线检查规则: {} 条", rules.size());
        
        for (BaselineRule rule : rules) {
            try {
                // 检查规则ID是否已存在
                if (rule.getRuleId() == null || rule.getRuleId().isEmpty()) {
                    rule.setRuleId(UUID.randomUUID().toString());
                }
                
                if (baselineRuleRepository.findByRuleId(rule.getRuleId()).isEmpty()) {
                    baselineRuleRepository.save(rule);
                } else {
                    logger.warn("规则ID已存在，跳过: {}", rule.getRuleId());
                }
            } catch (Exception e) {
                logger.error("导入规则失败: {}", rule.getName(), e);
            }
        }
    }
    
    @Override
    public void initializeDefaultRules() {
        logger.info("初始化默认基线检查规则");
        
        // 检查是否已有规则
        if (baselineRuleRepository.count() > 0) {
            logger.info("已存在基线检查规则，跳过初始化");
            return;
        }
        
        // 创建一些示例规则
        createSampleRules();
    }
    
    @Override
    @Transactional(readOnly = true)
    public RuleStats getRuleStats() {
        long totalRules = baselineRuleRepository.count();
        long enabledRules = baselineRuleRepository.countEnabledRules();
        long windowsRules = baselineRuleRepository.countByPlatformAndEnabledTrue(BaselineRule.Platform.WINDOWS);
        long linuxRules = baselineRuleRepository.countByPlatformAndEnabledTrue(BaselineRule.Platform.LINUX);
        long criticalRules = baselineRuleRepository.countBySeverityAndEnabledTrue(BaselineRule.Severity.CRITICAL);
        long highRules = baselineRuleRepository.countBySeverityAndEnabledTrue(BaselineRule.Severity.HIGH);
        long mediumRules = baselineRuleRepository.countBySeverityAndEnabledTrue(BaselineRule.Severity.MEDIUM);
        long lowRules = baselineRuleRepository.countBySeverityAndEnabledTrue(BaselineRule.Severity.LOW);
        
        return new RuleStats(totalRules, enabledRules, windowsRules, linuxRules,
                           criticalRules, highRules, mediumRules, lowRules);
    }
    
    private void createSampleRules() {
        // 创建一些示例规则用于演示
        // 这里只创建几个基本的规则，实际使用时应该导入完整的CIS Benchmarks等标准

        // Windows密码策略检查
        BaselineRule windowsPasswordRule = new BaselineRule();
        windowsPasswordRule.setRuleId("CIS-WIN-1.1.1");
        windowsPasswordRule.setName("密码最小长度检查");
        windowsPasswordRule.setCategory("账户策略");
        windowsPasswordRule.setDescription("检查Windows系统密码最小长度设置");
        windowsPasswordRule.setSeverity(BaselineRule.Severity.HIGH);
        windowsPasswordRule.setPlatform(BaselineRule.Platform.WINDOWS);
        windowsPasswordRule.setStandard("CIS");
        windowsPasswordRule.setVersion("1.0");
        windowsPasswordRule.setCheckScript("net accounts | findstr \"Minimum password length\"");
        windowsPasswordRule.setExpectedValue("14");
        windowsPasswordRule.setRecommendation("设置密码最小长度为14位");
        windowsPasswordRule.setReference("https://www.cisecurity.org/");
        baselineRuleRepository.save(windowsPasswordRule);

        // Linux SSH配置检查
        BaselineRule linuxSshRule = new BaselineRule();
        linuxSshRule.setRuleId("CIS-LINUX-5.2.4");
        linuxSshRule.setName("SSH Root登录检查");
        linuxSshRule.setCategory("网络配置");
        linuxSshRule.setDescription("检查SSH是否禁用root直接登录");
        linuxSshRule.setSeverity(BaselineRule.Severity.CRITICAL);
        linuxSshRule.setPlatform(BaselineRule.Platform.LINUX);
        linuxSshRule.setStandard("CIS");
        linuxSshRule.setVersion("1.0");
        linuxSshRule.setCheckScript("grep \"^PermitRootLogin\" /etc/ssh/sshd_config");
        linuxSshRule.setExpectedValue("no");
        linuxSshRule.setRecommendation("在/etc/ssh/sshd_config中设置PermitRootLogin no");
        linuxSshRule.setReference("https://www.cisecurity.org/");
        baselineRuleRepository.save(linuxSshRule);

        logger.info("默认基线检查规则初始化完成");
    }
}
