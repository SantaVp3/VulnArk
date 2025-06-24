package com.vulnark.service;

import com.vulnark.entity.ScanConfig;
import com.vulnark.entity.ScanTask;
import com.vulnark.entity.User;
import com.vulnark.repository.ScanConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ScanConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanConfigService.class);
    
    @Autowired
    private ScanConfigRepository scanConfigRepository;
    
    /**
     * 创建扫描配置
     */
    public ScanConfig createScanConfig(ScanConfigRequest request, User currentUser) {
        // 验证请求参数
        validateScanConfigRequest(request);
        
        // 检查名称是否已存在
        if (scanConfigRepository.existsByNameAndDeletedFalse(request.getName(), null)) {
            throw new IllegalArgumentException("配置名称已存在: " + request.getName());
        }
        
        ScanConfig config = new ScanConfig();
        updateScanConfigFromRequest(config, request);
        config.setCreatedBy(currentUser);
        
        config = scanConfigRepository.save(config);
        
        logger.info("创建扫描配置: {} (ID: {})", config.getName(), config.getId());
        return config;
    }
    
    /**
     * 更新扫描配置
     */
    public ScanConfig updateScanConfig(Long configId, ScanConfigRequest request, User currentUser) {
        ScanConfig config = scanConfigRepository.findByIdAndDeletedFalse(configId)
                .orElseThrow(() -> new RuntimeException("扫描配置不存在: " + configId));
        
        // 验证请求参数
        validateScanConfigRequest(request);
        
        // 检查名称是否已存在（排除当前配置）
        if (scanConfigRepository.existsByNameAndDeletedFalse(request.getName(), configId)) {
            throw new IllegalArgumentException("配置名称已存在: " + request.getName());
        }
        
        updateScanConfigFromRequest(config, request);
        config = scanConfigRepository.save(config);
        
        logger.info("更新扫描配置: {} (ID: {})", config.getName(), config.getId());
        return config;
    }
    
    /**
     * 获取扫描配置列表
     */
    public Page<ScanConfig> getScanConfigs(ScanConfigQueryRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        
        return scanConfigRepository.findByConditions(
                request.getName(),
                request.getScanEngine(),
                request.getScanType(),
                request.getEnabled(),
                request.getCreatedById(),
                pageable
        );
    }
    
    /**
     * 获取扫描配置详情
     */
    public ScanConfig getScanConfigById(Long configId) {
        return scanConfigRepository.findByIdAndDeletedFalse(configId)
                .orElseThrow(() -> new RuntimeException("扫描配置不存在: " + configId));
    }
    
    /**
     * 删除扫描配置
     */
    public void deleteScanConfig(Long configId) {
        ScanConfig config = scanConfigRepository.findByIdAndDeletedFalse(configId)
                .orElseThrow(() -> new RuntimeException("扫描配置不存在: " + configId));
        
        // 软删除
        config.setDeleted(true);
        scanConfigRepository.save(config);
        
        logger.info("删除扫描配置: {} (ID: {})", config.getName(), config.getId());
    }
    
    /**
     * 启用/禁用扫描配置
     */
    public ScanConfig toggleScanConfig(Long configId, boolean enabled) {
        ScanConfig config = scanConfigRepository.findByIdAndDeletedFalse(configId)
                .orElseThrow(() -> new RuntimeException("扫描配置不存在: " + configId));
        
        config.setEnabled(enabled);
        config = scanConfigRepository.save(config);
        
        logger.info("{} 扫描配置: {} (ID: {})", enabled ? "启用" : "禁用", config.getName(), config.getId());
        return config;
    }
    
    /**
     * 设置默认配置
     */
    public ScanConfig setDefaultConfig(Long configId) {
        ScanConfig config = scanConfigRepository.findByIdAndDeletedFalse(configId)
                .orElseThrow(() -> new RuntimeException("扫描配置不存在: " + configId));
        
        // 取消同类型的其他默认配置
        List<ScanConfig> defaultConfigs = scanConfigRepository.findByScanTypeAndEnabledTrueAndDeletedFalse(config.getScanType());
        for (ScanConfig defaultConfig : defaultConfigs) {
            if (defaultConfig.getIsDefault() && !defaultConfig.getId().equals(configId)) {
                defaultConfig.setIsDefault(false);
                scanConfigRepository.save(defaultConfig);
            }
        }
        
        config.setIsDefault(true);
        config = scanConfigRepository.save(config);
        
        logger.info("设置默认扫描配置: {} (ID: {})", config.getName(), config.getId());
        return config;
    }
    
    /**
     * 获取启用的扫描配置
     */
    public List<ScanConfig> getEnabledScanConfigs() {
        return scanConfigRepository.findByEnabledTrueAndDeletedFalse();
    }
    
    /**
     * 根据扫描引擎获取配置
     */
    public List<ScanConfig> getScanConfigsByEngine(ScanTask.ScanEngine scanEngine) {
        return scanConfigRepository.findByScanEngineAndEnabledTrueAndDeletedFalse(scanEngine);
    }
    
    /**
     * 根据扫描类型获取配置
     */
    public List<ScanConfig> getScanConfigsByType(ScanTask.ScanType scanType) {
        return scanConfigRepository.findByScanTypeAndEnabledTrueAndDeletedFalse(scanType);
    }
    
    /**
     * 获取默认配置
     */
    public List<ScanConfig> getDefaultConfigs() {
        return scanConfigRepository.findByIsDefaultTrueAndDeletedFalse();
    }
    
    /**
     * 获取推荐配置
     */
    public List<ScanConfig> getRecommendedConfigs(ScanTask.ScanType scanType, ScanTask.ScanEngine scanEngine) {
        Pageable pageable = PageRequest.of(0, 10);
        return scanConfigRepository.findRecommendedConfigs(scanType, scanEngine, pageable);
    }
    
    /**
     * 复制扫描配置
     */
    public ScanConfig copyScanConfig(Long configId, String newName, User currentUser) {
        ScanConfig originalConfig = scanConfigRepository.findByIdAndDeletedFalse(configId)
                .orElseThrow(() -> new RuntimeException("扫描配置不存在: " + configId));
        
        // 检查新名称是否已存在
        if (scanConfigRepository.existsByNameAndDeletedFalse(newName, null)) {
            throw new IllegalArgumentException("配置名称已存在: " + newName);
        }
        
        ScanConfig newConfig = new ScanConfig();
        copyConfigProperties(originalConfig, newConfig);
        newConfig.setName(newName);
        newConfig.setCreatedBy(currentUser);
        newConfig.setIsDefault(false); // 复制的配置不能是默认配置
        
        newConfig = scanConfigRepository.save(newConfig);
        
        logger.info("复制扫描配置: {} -> {} (ID: {})", originalConfig.getName(), newName, newConfig.getId());
        return newConfig;
    }
    
    /**
     * 获取配置统计信息
     */
    public ScanConfigStatistics getConfigStatistics() {
        ScanConfigStatistics stats = new ScanConfigStatistics();
        
        stats.setTotalConfigs(scanConfigRepository.countAllConfigs());
        stats.setEnabledConfigs(scanConfigRepository.countEnabledConfigs());
        
        // 按引擎统计
        List<Object[]> engineStats = scanConfigRepository.getScanEngineStatistics();
        for (Object[] row : engineStats) {
            if (row[0] != null && row[1] != null) {
                stats.getEngineDistribution().put(row[0].toString(), (Long) row[1]);
            }
        }
        
        // 按类型统计
        List<Object[]> typeStats = scanConfigRepository.getScanTypeStatistics();
        for (Object[] row : typeStats) {
            if (row[0] != null && row[1] != null) {
                stats.getTypeDistribution().put(row[0].toString(), (Long) row[1]);
            }
        }
        
        return stats;
    }
    
    /**
     * 搜索扫描配置
     */
    public Page<ScanConfig> searchScanConfigs(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        return scanConfigRepository.searchByKeyword(keyword, pageable);
    }
    
    // 私有方法
    private void validateScanConfigRequest(ScanConfigRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("配置名称不能为空");
        }
        
        if (request.getScanEngine() == null) {
            throw new IllegalArgumentException("扫描引擎不能为空");
        }
        
        if (request.getScanType() == null) {
            throw new IllegalArgumentException("扫描类型不能为空");
        }
        
        if (request.getTimeoutMinutes() != null && request.getTimeoutMinutes() <= 0) {
            throw new IllegalArgumentException("超时时间必须大于0");
        }
        
        if (request.getMaxConcurrency() != null && request.getMaxConcurrency() <= 0) {
            throw new IllegalArgumentException("最大并发数必须大于0");
        }
    }
    
    private void updateScanConfigFromRequest(ScanConfig config, ScanConfigRequest request) {
        config.setName(request.getName());
        config.setDescription(request.getDescription());
        config.setScanEngine(request.getScanEngine());
        config.setScanType(request.getScanType());
        config.setScanTemplate(request.getScanTemplate());
        config.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        config.setParameters(request.getParameters());
        config.setTimeoutMinutes(request.getTimeoutMinutes() != null ? request.getTimeoutMinutes() : 60);
        config.setMaxConcurrency(request.getMaxConcurrency() != null ? request.getMaxConcurrency() : 5);
        config.setPortRange(request.getPortRange());
        config.setExcludePorts(request.getExcludePorts());
        config.setScanDepth(request.getScanDepth() != null ? request.getScanDepth() : ScanConfig.ScanDepth.NORMAL);
        config.setScanUdp(request.getScanUdp() != null ? request.getScanUdp() : false);
        config.setServiceDetection(request.getServiceDetection() != null ? request.getServiceDetection() : true);
        config.setOsDetection(request.getOsDetection() != null ? request.getOsDetection() : true);
        config.setScriptScan(request.getScriptScan() != null ? request.getScriptScan() : false);
        config.setVulnerabilityScan(request.getVulnerabilityScan() != null ? request.getVulnerabilityScan() : true);
        config.setWebAppScan(request.getWebAppScan() != null ? request.getWebAppScan() : false);
        config.setCustomScripts(request.getCustomScripts());
        config.setExcludeVulnTypes(request.getExcludeVulnTypes());
        config.setIncludeVulnTypes(request.getIncludeVulnTypes());
        config.setScanPolicy(request.getScanPolicy());
    }
    
    private void copyConfigProperties(ScanConfig source, ScanConfig target) {
        target.setDescription(source.getDescription());
        target.setScanEngine(source.getScanEngine());
        target.setScanType(source.getScanType());
        target.setScanTemplate(source.getScanTemplate());
        target.setEnabled(source.getEnabled());
        target.setParameters(source.getParameters());
        target.setTimeoutMinutes(source.getTimeoutMinutes());
        target.setMaxConcurrency(source.getMaxConcurrency());
        target.setPortRange(source.getPortRange());
        target.setExcludePorts(source.getExcludePorts());
        target.setScanDepth(source.getScanDepth());
        target.setScanUdp(source.getScanUdp());
        target.setServiceDetection(source.getServiceDetection());
        target.setOsDetection(source.getOsDetection());
        target.setScriptScan(source.getScriptScan());
        target.setVulnerabilityScan(source.getVulnerabilityScan());
        target.setWebAppScan(source.getWebAppScan());
        target.setCustomScripts(source.getCustomScripts());
        target.setExcludeVulnTypes(source.getExcludeVulnTypes());
        target.setIncludeVulnTypes(source.getIncludeVulnTypes());
        target.setScanPolicy(source.getScanPolicy());
    }
    
    // 内部类
    public static class ScanConfigRequest {
        private String name;
        private String description;
        private ScanTask.ScanEngine scanEngine;
        private ScanTask.ScanType scanType;
        private ScanTask.ScanTemplate scanTemplate;
        private Boolean enabled;
        private String parameters;
        private Integer timeoutMinutes;
        private Integer maxConcurrency;
        private String portRange;
        private String excludePorts;
        private ScanConfig.ScanDepth scanDepth;
        private Boolean scanUdp;
        private Boolean serviceDetection;
        private Boolean osDetection;
        private Boolean scriptScan;
        private Boolean vulnerabilityScan;
        private Boolean webAppScan;
        private String customScripts;
        private String excludeVulnTypes;
        private String includeVulnTypes;
        private String scanPolicy;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public ScanTask.ScanEngine getScanEngine() { return scanEngine; }
        public void setScanEngine(ScanTask.ScanEngine scanEngine) { this.scanEngine = scanEngine; }
        
        public ScanTask.ScanType getScanType() { return scanType; }
        public void setScanType(ScanTask.ScanType scanType) { this.scanType = scanType; }
        
        public ScanTask.ScanTemplate getScanTemplate() { return scanTemplate; }
        public void setScanTemplate(ScanTask.ScanTemplate scanTemplate) { this.scanTemplate = scanTemplate; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public String getParameters() { return parameters; }
        public void setParameters(String parameters) { this.parameters = parameters; }
        
        public Integer getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(Integer timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
        
        public Integer getMaxConcurrency() { return maxConcurrency; }
        public void setMaxConcurrency(Integer maxConcurrency) { this.maxConcurrency = maxConcurrency; }
        
        public String getPortRange() { return portRange; }
        public void setPortRange(String portRange) { this.portRange = portRange; }
        
        public String getExcludePorts() { return excludePorts; }
        public void setExcludePorts(String excludePorts) { this.excludePorts = excludePorts; }
        
        public ScanConfig.ScanDepth getScanDepth() { return scanDepth; }
        public void setScanDepth(ScanConfig.ScanDepth scanDepth) { this.scanDepth = scanDepth; }
        
        public Boolean getScanUdp() { return scanUdp; }
        public void setScanUdp(Boolean scanUdp) { this.scanUdp = scanUdp; }
        
        public Boolean getServiceDetection() { return serviceDetection; }
        public void setServiceDetection(Boolean serviceDetection) { this.serviceDetection = serviceDetection; }
        
        public Boolean getOsDetection() { return osDetection; }
        public void setOsDetection(Boolean osDetection) { this.osDetection = osDetection; }
        
        public Boolean getScriptScan() { return scriptScan; }
        public void setScriptScan(Boolean scriptScan) { this.scriptScan = scriptScan; }
        
        public Boolean getVulnerabilityScan() { return vulnerabilityScan; }
        public void setVulnerabilityScan(Boolean vulnerabilityScan) { this.vulnerabilityScan = vulnerabilityScan; }
        
        public Boolean getWebAppScan() { return webAppScan; }
        public void setWebAppScan(Boolean webAppScan) { this.webAppScan = webAppScan; }
        
        public String getCustomScripts() { return customScripts; }
        public void setCustomScripts(String customScripts) { this.customScripts = customScripts; }
        
        public String getExcludeVulnTypes() { return excludeVulnTypes; }
        public void setExcludeVulnTypes(String excludeVulnTypes) { this.excludeVulnTypes = excludeVulnTypes; }
        
        public String getIncludeVulnTypes() { return includeVulnTypes; }
        public void setIncludeVulnTypes(String includeVulnTypes) { this.includeVulnTypes = includeVulnTypes; }
        
        public String getScanPolicy() { return scanPolicy; }
        public void setScanPolicy(String scanPolicy) { this.scanPolicy = scanPolicy; }
    }
    
    public static class ScanConfigQueryRequest {
        private String name;
        private ScanTask.ScanEngine scanEngine;
        private ScanTask.ScanType scanType;
        private Boolean enabled;
        private Long createdById;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public ScanTask.ScanEngine getScanEngine() { return scanEngine; }
        public void setScanEngine(ScanTask.ScanEngine scanEngine) { this.scanEngine = scanEngine; }
        
        public ScanTask.ScanType getScanType() { return scanType; }
        public void setScanType(ScanTask.ScanType scanType) { this.scanType = scanType; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        
        public Long getCreatedById() { return createdById; }
        public void setCreatedById(Long createdById) { this.createdById = createdById; }
    }
    
    public static class ScanConfigStatistics {
        private long totalConfigs;
        private long enabledConfigs;
        private java.util.Map<String, Long> engineDistribution = new java.util.HashMap<>();
        private java.util.Map<String, Long> typeDistribution = new java.util.HashMap<>();
        
        // Getters and Setters
        public long getTotalConfigs() { return totalConfigs; }
        public void setTotalConfigs(long totalConfigs) { this.totalConfigs = totalConfigs; }
        
        public long getEnabledConfigs() { return enabledConfigs; }
        public void setEnabledConfigs(long enabledConfigs) { this.enabledConfigs = enabledConfigs; }
        
        public java.util.Map<String, Long> getEngineDistribution() { return engineDistribution; }
        public void setEngineDistribution(java.util.Map<String, Long> engineDistribution) { this.engineDistribution = engineDistribution; }
        
        public java.util.Map<String, Long> getTypeDistribution() { return typeDistribution; }
        public void setTypeDistribution(java.util.Map<String, Long> typeDistribution) { this.typeDistribution = typeDistribution; }
    }
}
