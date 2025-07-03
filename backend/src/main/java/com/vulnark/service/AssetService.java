package com.vulnark.service;

import com.vulnark.dto.AssetQueryRequest;
import com.vulnark.dto.AssetRequest;
import com.vulnark.entity.Asset;
import com.vulnark.exception.BusinessException;
import com.vulnark.repository.AssetRepository;
import com.vulnark.repository.VulnerabilityRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssetService {
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;
    
    /**
     * 创建资产
     */
    public Asset createAsset(AssetRequest request) {
        // 验证IP地址唯一性
        if (assetRepository.findByIpAddressAndDeletedFalse(request.getIpAddress()).isPresent()) {
            throw new BusinessException("IP地址已存在: " + request.getIpAddress());
        }
        
        Asset asset = new Asset();
        asset.setName(request.getName());
        asset.setDescription(request.getDescription());
        asset.setType(request.getType());
        asset.setStatus(request.getStatus());
        asset.setIpAddress(request.getIpAddress());
        asset.setDomain(request.getDomain());
        asset.setPort(request.getPort());
        asset.setProtocol(request.getProtocol());
        asset.setService(request.getService());
        asset.setVersion(request.getVersion());
        asset.setOperatingSystem(request.getOperatingSystem());
        asset.setImportance(request.getImportance());
        asset.setProjectId(request.getProjectId());
        asset.setOwnerId(request.getOwnerId());
        asset.setLocation(request.getLocation());
        asset.setVendor(request.getVendor());
        asset.setTags(request.getTags());
        asset.setRiskScore(request.getRiskScore());
        asset.setNotes(request.getNotes());
        
        Asset savedAsset = assetRepository.save(asset);
        return savedAsset;
    }
    
    /**
     * 更新资产
     */
    public Asset updateAsset(Long id, AssetRequest request) {
        Asset asset = getAssetById(id);
        
        // 验证IP地址唯一性（排除自己）
        Optional<Asset> existingAsset = assetRepository.findByIpAddressAndDeletedFalse(request.getIpAddress());
        if (existingAsset.isPresent() && !existingAsset.get().getId().equals(id)) {
            throw new BusinessException("IP地址已存在: " + request.getIpAddress());
        }
        
        // 更新资产信息
        asset.setName(request.getName());
        asset.setDescription(request.getDescription());
        asset.setType(request.getType());
        asset.setStatus(request.getStatus());
        asset.setIpAddress(request.getIpAddress());
        asset.setDomain(request.getDomain());
        asset.setPort(request.getPort());
        asset.setProtocol(request.getProtocol());
        asset.setService(request.getService());
        asset.setVersion(request.getVersion());
        asset.setOperatingSystem(request.getOperatingSystem());
        asset.setImportance(request.getImportance());
        asset.setProjectId(request.getProjectId());
        asset.setOwnerId(request.getOwnerId());
        asset.setLocation(request.getLocation());
        asset.setVendor(request.getVendor());
        asset.setTags(request.getTags());
        asset.setRiskScore(request.getRiskScore());
        asset.setNotes(request.getNotes());
        
        return assetRepository.save(asset);
    }
    
    /**
     * 删除资产（硬删除）
     */
    public void deleteAsset(Long id) {
        Asset asset = getAssetById(id);
        assetRepository.delete(asset);
    }
    
    /**
     * 根据ID获取资产
     */
    public Asset getAssetById(Long id) {
        return assetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("资产不存在或已被删除"));
    }
    
    /**
     * 分页查询资产
     */
    public Page<Asset> getAssets(AssetQueryRequest request) {
        // 创建分页对象
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        
        // 检查是否有任何过滤条件
        boolean hasFilters = StringUtils.hasText(request.getName()) || 
                             request.getType() != null || 
                             request.getStatus() != null || 
                             request.getImportance() != null || 
                             request.getOwnerId() != null || 
                             StringUtils.hasText(request.getIpAddress()) || 
                             StringUtils.hasText(request.getDomain()) || 
                             StringUtils.hasText(request.getKeyword());
        
        Page<Asset> result;
        // 如果有关键词搜索，使用全文搜索
        if (StringUtils.hasText(request.getKeyword())) {
            result = assetRepository.searchByKeyword(request.getKeyword(), pageable);
        } else if (hasFilters) {
            // 使用条件查询
            result = assetRepository.findByConditions(
                    request.getName(),
                    request.getType(),
                    request.getStatus(),
                    request.getImportance(),
                    request.getOwnerId(),
                    request.getIpAddress(),
                    request.getDomain(),
                    pageable
            );
        } else {
            // 如果没有任何过滤条件，返回所有未删除的资产
            result = assetRepository.findByDeletedFalse(pageable);
        }
        
        return result;
    }
    
    /**
     * 获取所有资产
     */
    public List<Asset> getAllAssets() {
        return assetRepository.findByDeletedFalse();
    }
    
    /**
     * 根据项目ID获取资产 - 已删除项目功能
     */
    public List<Asset> getAssetsByProjectId(Long projectId) {
        // 项目功能已删除，返回空列表
        return new ArrayList<>();
    }
    
    /**
     * 根据负责人ID获取资产
     */
    public List<Asset> getAssetsByOwnerId(Long ownerId) {
        return assetRepository.findByOwnerIdAndDeletedFalse(ownerId);
    }
    
    /**
     * 获取最近的资产
     */
    public List<Asset> getRecentAssets(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return assetRepository.findRecentAssets(pageable);
    }
    
    /**
     * 获取高风险资产
     */
    public List<Asset> getHighRiskAssets(Double minRiskScore) {
        if (minRiskScore == null) {
            minRiskScore = 7.0; // 默认风险评分阈值
        }
        return assetRepository.findHighRiskAssets(minRiskScore);
    }
    
    /**
     * 获取需要扫描的资产
     */
    public List<Asset> getAssetsNeedingScan(int daysBefore) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(daysBefore);
        return assetRepository.findAssetsNeedingScan(beforeTime);
    }
    
    /**
     * 获取活跃资产
     */
    public List<Asset> getActiveAssets() {
        return assetRepository.findActiveAssets();
    }
    
    /**
     * 更新资产状态
     */
    public Asset updateAssetStatus(Long id, Asset.Status status) {
        Asset asset = getAssetById(id);
        asset.setStatus(status);
        return assetRepository.save(asset);
    }
    
    /**
     * 更新资产风险评分
     */
    public Asset updateAssetRiskScore(Long id, Double riskScore) {
        if (riskScore < 0 || riskScore > 10) {
            throw new RuntimeException("风险评分必须在0-10之间");
        }
        
        Asset asset = getAssetById(id);
        asset.setRiskScore(riskScore);
        return assetRepository.save(asset);
    }
    
    /**
     * 更新资产扫描时间
     */
    public Asset updateAssetScanTime(Long id) {
        Asset asset = getAssetById(id);
        asset.setLastScanTime(LocalDateTime.now());
        return assetRepository.save(asset);
    }
    
    /**
     * 更新资产统计信息
     */
    public boolean updateAssetStatistics(Long id) {
        try {
            Asset asset = getAssetById(id);
            
            // 更新漏洞数量
            long vulnerabilityCount = vulnerabilityRepository.countByAssetIdAndDeletedFalse(id);
            asset.setVulnerabilityCount((int) vulnerabilityCount);
            
            // TODO: 可以在这里添加更多统计逻辑，比如计算风险评分
            
            assetRepository.save(asset);
            return true;
        } catch (RuntimeException e) {
            // 如果资产不存在，返回false而不是抛出异常
            return false;
        }
    }

    /**
     * 更新资产统计信息并返回资产对象（用于API）
     */
    public Asset updateAssetStatisticsAndReturn(Long id) {
        Asset asset = getAssetById(id);
        
        // 更新漏洞数量
        long vulnerabilityCount = vulnerabilityRepository.countByAssetIdAndDeletedFalse(id);
        asset.setVulnerabilityCount((int) vulnerabilityCount);
        
        // TODO: 可以在这里添加更多统计逻辑，比如计算风险评分
        
        return assetRepository.save(asset);
    }
    
    /**
     * 批量导入资产
     */
    public List<Asset> importAssets(List<AssetRequest> requests) {
        List<Asset> assets = new ArrayList<>();
        for (AssetRequest request : requests) {
            try {
                Asset asset = createAsset(request);
                assets.add(asset);
            } catch (Exception e) {
                // 记录错误但继续处理其他资产
                // 可以考虑返回导入结果详情
            }
        }
        return assets;
    }
    
    /**
     * 批量导出资产
     */
    public List<Asset> exportAssets(List<Long> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) {
            return getAllAssets();
        }
        
        List<Asset> assets = new ArrayList<>();
        for (Long id : assetIds) {
            try {
                Asset asset = getAssetById(id);
                assets.add(asset);
            } catch (Exception e) {
                // 忽略不存在的资产
            }
        }
        return assets;
    }
    
    /**
     * 获取资产统计信息
     */
    public AssetStats getAssetStats() {
        AssetStats stats = new AssetStats();
        stats.setTotal(assetRepository.countByDeletedFalse());
        stats.setActive(assetRepository.countByStatusAndDeletedFalse(Asset.Status.ACTIVE));
        stats.setInactive(assetRepository.countByStatusAndDeletedFalse(Asset.Status.INACTIVE));
        stats.setMaintenance(assetRepository.countByStatusAndDeletedFalse(Asset.Status.MAINTENANCE));
        stats.setHigh(assetRepository.countByImportanceAndDeletedFalse(Asset.Importance.HIGH));
        stats.setCritical(assetRepository.countByImportanceAndDeletedFalse(Asset.Importance.CRITICAL));
        return stats;
    }
    
    /**
     * 获取所有资产总数
     */
    public long countAllAssets() {
        return assetRepository.countByDeletedFalse();
    }
    
    // 内部类：资产统计信息
    public static class AssetStats {
        private long total;
        private long active;
        private long inactive;
        private long maintenance;
        private long high;
        private long critical;
        
        // Getters and Setters
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public long getActive() { return active; }
        public void setActive(long active) { this.active = active; }
        public long getInactive() { return inactive; }
        public void setInactive(long inactive) { this.inactive = inactive; }
        public long getMaintenance() { return maintenance; }
        public void setMaintenance(long maintenance) { this.maintenance = maintenance; }
        public long getHigh() { return high; }
        public void setHigh(long high) { this.high = high; }
        public long getCritical() { return critical; }
        public void setCritical(long critical) { this.critical = critical; }
    }
}
