package com.vulnark.service;

import com.vulnark.dto.AssetQueryRequest;
import com.vulnark.dto.AssetRequest;
import com.vulnark.entity.Asset;
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
        // 检查IP地址是否已存在
        if (StringUtils.hasText(request.getIpAddress())) {
            Optional<Asset> existingAsset = assetRepository.findByIpAddressAndDeletedFalse(request.getIpAddress());
            if (existingAsset.isPresent()) {
                throw new RuntimeException("IP地址已存在");
            }
        }
        
        // 检查域名是否已存在
        if (StringUtils.hasText(request.getDomain())) {
            Optional<Asset> existingAsset = assetRepository.findByDomainAndDeletedFalse(request.getDomain());
            if (existingAsset.isPresent()) {
                throw new RuntimeException("域名已存在");
            }
        }
        
        Asset asset = new Asset();
        BeanUtils.copyProperties(request, asset);
        
        // 设置默认值
        if (asset.getStatus() == null) {
            asset.setStatus(Asset.Status.ACTIVE);
        }
        if (asset.getImportance() == null) {
            asset.setImportance(Asset.Importance.MEDIUM);
        }
        if (asset.getVulnerabilityCount() == null) {
            asset.setVulnerabilityCount(0);
        }
        if (asset.getRiskScore() == null) {
            asset.setRiskScore(0.0);
        }
        
        return assetRepository.save(asset);
    }
    
    /**
     * 更新资产
     */
    public Asset updateAsset(Long id, AssetRequest request) {
        Asset asset = getAssetById(id);
        
        // 检查IP地址是否已被其他资产使用
        if (StringUtils.hasText(request.getIpAddress()) && 
            !request.getIpAddress().equals(asset.getIpAddress())) {
            Optional<Asset> existingAsset = assetRepository.findByIpAddressAndDeletedFalse(request.getIpAddress());
            if (existingAsset.isPresent()) {
                throw new RuntimeException("IP地址已存在");
            }
        }
        
        // 检查域名是否已被其他资产使用
        if (StringUtils.hasText(request.getDomain()) && 
            !request.getDomain().equals(asset.getDomain())) {
            Optional<Asset> existingAsset = assetRepository.findByDomainAndDeletedFalse(request.getDomain());
            if (existingAsset.isPresent()) {
                throw new RuntimeException("域名已存在");
            }
        }
        
        // 更新字段
        if (request.getName() != null) {
            asset.setName(request.getName());
        }
        if (request.getDescription() != null) {
            asset.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            asset.setType(request.getType());
        }
        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }
        if (request.getIpAddress() != null) {
            asset.setIpAddress(request.getIpAddress());
        }
        if (request.getDomain() != null) {
            asset.setDomain(request.getDomain());
        }
        if (request.getPort() != null) {
            asset.setPort(request.getPort());
        }
        if (request.getProtocol() != null) {
            asset.setProtocol(request.getProtocol());
        }
        if (request.getService() != null) {
            asset.setService(request.getService());
        }
        if (request.getVersion() != null) {
            asset.setVersion(request.getVersion());
        }
        if (request.getOperatingSystem() != null) {
            asset.setOperatingSystem(request.getOperatingSystem());
        }
        if (request.getImportance() != null) {
            asset.setImportance(request.getImportance());
        }
        if (request.getProjectId() != null) {
            asset.setProjectId(request.getProjectId());
        }
        if (request.getOwnerId() != null) {
            asset.setOwnerId(request.getOwnerId());
        }
        if (request.getLocation() != null) {
            asset.setLocation(request.getLocation());
        }
        if (request.getVendor() != null) {
            asset.setVendor(request.getVendor());
        }
        if (request.getTags() != null) {
            asset.setTags(request.getTags());
        }
        if (request.getLastScanTime() != null) {
            asset.setLastScanTime(request.getLastScanTime());
        }
        if (request.getRiskScore() != null) {
            asset.setRiskScore(request.getRiskScore());
        }
        if (request.getNotes() != null) {
            asset.setNotes(request.getNotes());
        }
        
        return assetRepository.save(asset);
    }
    
    /**
     * 删除资产（逻辑删除）
     */
    public void deleteAsset(Long id) {
        Asset asset = getAssetById(id);
        asset.setDeleted(true);
        assetRepository.save(asset);
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
        
        // 如果有关键词搜索，使用全文搜索
        if (StringUtils.hasText(request.getKeyword())) {
            return assetRepository.searchByKeyword(request.getKeyword(), pageable);
        }
        
        // 否则使用条件查询
        return assetRepository.findByConditions(
                request.getName(),
                request.getType(),
                request.getStatus(),
                request.getImportance(),
                request.getProjectId(),
                request.getOwnerId(),
                request.getIpAddress(),
                request.getDomain(),
                pageable
        );
    }
    
    /**
     * 获取所有资产
     */
    public List<Asset> getAllAssets() {
        return assetRepository.findByDeletedFalse();
    }
    
    /**
     * 根据项目ID获取资产
     */
    public List<Asset> getAssetsByProjectId(Long projectId) {
        return assetRepository.findByProjectIdAndDeletedFalse(projectId);
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
    public Asset updateAssetStatistics(Long id) {
        Asset asset = getAssetById(id);
        
        // 更新漏洞数量
        // TODO: 需要在Vulnerability实体中添加assetId字段
        // long vulnerabilityCount = vulnerabilityRepository.countByAssetIdAndDeletedFalse(id);
        // asset.setVulnerabilityCount((int) vulnerabilityCount);
        
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
