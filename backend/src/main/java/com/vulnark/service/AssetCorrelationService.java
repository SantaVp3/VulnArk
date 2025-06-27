package com.vulnark.service;

import com.vulnark.entity.Asset;
import com.vulnark.entity.AssetDiscoveryResult;
import com.vulnark.repository.AssetRepository;
import com.vulnark.repository.AssetDiscoveryResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 资产关联服务
 * 负责将发现的结果与现有资产进行关联
 */
@Service
@Transactional
public class AssetCorrelationService {

    private static final Logger logger = LoggerFactory.getLogger(AssetCorrelationService.class);

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetDiscoveryResultRepository resultRepository;

    /**
     * 关联发现结果
     */
    public void correlateResults(List<AssetDiscoveryResult> results) {
        logger.info("开始关联 {} 个发现结果", results.size());
        
        for (AssetDiscoveryResult result : results) {
            try {
                correlateResult(result);
            } catch (Exception e) {
                logger.error("关联结果失败: {}", e.getMessage(), e);
            }
        }
        
        logger.info("资产关联完成");
    }

    /**
     * 关联单个发现结果
     */
    public void correlateResult(AssetDiscoveryResult result) {
        // 尝试通过IP地址匹配现有资产
        Optional<Asset> existingAsset = findMatchingAsset(result);
        
        if (existingAsset.isPresent()) {
            Asset asset = existingAsset.get();
            
            // 检查是否需要更新资产信息
            if (shouldUpdateAsset(asset, result)) {
                updateAssetFromResult(asset, result);
                assetRepository.save(asset);
                result.setCorrelationStatus(AssetDiscoveryResult.CorrelationStatus.UPDATED);
                logger.debug("更新现有资产: {} (IP: {})", asset.getName(), result.getIpAddress());
            } else {
                result.setCorrelationStatus(AssetDiscoveryResult.CorrelationStatus.MATCHED);
                logger.debug("匹配现有资产: {} (IP: {})", asset.getName(), result.getIpAddress());
            }
            
            result.setAssetId(asset.getId());
        } else {
            // 创建新资产
            if (shouldCreateNewAsset(result)) {
                Asset newAsset = createAssetFromResult(result);
                Asset savedAsset = assetRepository.save(newAsset);
                result.setAssetId(savedAsset.getId());
                result.setCorrelationStatus(AssetDiscoveryResult.CorrelationStatus.MATCHED);
                logger.info("创建新资产: {} (IP: {})", savedAsset.getName(), result.getIpAddress());
            } else {
                result.setCorrelationStatus(AssetDiscoveryResult.CorrelationStatus.NEW);
                logger.debug("发现新主机但未自动创建资产: {}", result.getIpAddress());
            }
        }
        
        resultRepository.save(result);
    }

    /**
     * 查找匹配的资产
     */
    private Optional<Asset> findMatchingAsset(AssetDiscoveryResult result) {
        // 首先通过IP地址精确匹配
        Optional<Asset> assetByIp = assetRepository.findByIpAddressAndDeletedFalse(result.getIpAddress()).stream().findFirst();
        if (assetByIp.isPresent()) {
            return assetByIp;
        }

        // 如果有主机名，尝试通过主机名匹配
        if (result.getHostname() != null && !result.getHostname().isEmpty()) {
            List<Asset> assetsByName = assetRepository.findByNameContainingAndDeletedFalse(result.getHostname());
            if (!assetsByName.isEmpty()) {
                // 进一步验证是否为同一资产
                for (Asset asset : assetsByName) {
                    if (isSameAsset(asset, result)) {
                        return Optional.of(asset);
                    }
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * 判断是否为同一资产
     */
    private boolean isSameAsset(Asset asset, AssetDiscoveryResult result) {
        // 通过多个维度判断是否为同一资产
        
        // IP地址匹配
        if (asset.getIpAddress() != null && asset.getIpAddress().equals(result.getIpAddress())) {
            return true;
        }
        
        // 主机名匹配
        if (asset.getName() != null && result.getHostname() != null) {
            if (asset.getName().toLowerCase().contains(result.getHostname().toLowerCase()) ||
                result.getHostname().toLowerCase().contains(asset.getName().toLowerCase())) {
                return true;
            }
        }
        
        // MAC地址匹配（如果有）
        if (asset.getDescription() != null && result.getMacAddress() != null) {
            if (asset.getDescription().contains(result.getMacAddress())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 判断是否需要更新资产
     */
    private boolean shouldUpdateAsset(Asset asset, AssetDiscoveryResult result) {
        // 检查资产信息是否有变化
        
        // 状态变化
        if (result.getIsAlive() && asset.getStatus() != Asset.Status.ACTIVE) {
            return true;
        }
        if (!result.getIsAlive() && asset.getStatus() != Asset.Status.INACTIVE) {
            return true;
        }
        
        // 操作系统信息更新
        if (result.getOperatingSystem() != null && 
            (asset.getOperatingSystem() == null || !asset.getOperatingSystem().equals(result.getOperatingSystem()))) {
            return true;
        }
        
        // 设备类型更新
        if (result.getDeviceType() != null && 
            (asset.getDescription() == null || !asset.getDescription().contains(result.getDeviceType()))) {
            return true;
        }
        
        // 最后发现时间超过一定时间间隔
        if (asset.getUpdatedTime().isBefore(LocalDateTime.now().minusHours(24))) {
            return true;
        }
        
        return false;
    }

    /**
     * 从发现结果更新资产信息
     */
    private void updateAssetFromResult(Asset asset, AssetDiscoveryResult result) {
        // 更新状态
        if (result.getIsAlive()) {
            asset.setStatus(Asset.Status.ACTIVE);
        } else {
            asset.setStatus(Asset.Status.INACTIVE);
        }
        
        // 更新操作系统
        if (result.getOperatingSystem() != null && !result.getOperatingSystem().equals("Unknown")) {
            asset.setOperatingSystem(result.getOperatingSystem());
        }
        
        // 更新IP地址（如果不同）
        if (result.getIpAddress() != null && !result.getIpAddress().equals(asset.getIpAddress())) {
            asset.setIpAddress(result.getIpAddress());
        }
        
        // 更新描述信息
        StringBuilder description = new StringBuilder();
        if (asset.getDescription() != null) {
            description.append(asset.getDescription()).append("\n");
        }
        
        if (result.getDeviceType() != null) {
            description.append("设备类型: ").append(result.getDeviceType()).append("\n");
        }
        
        if (result.getVendor() != null) {
            description.append("厂商: ").append(result.getVendor()).append("\n");
        }
        
        if (result.getMacAddress() != null) {
            description.append("MAC地址: ").append(result.getMacAddress()).append("\n");
        }
        
        description.append("最后发现时间: ").append(result.getDiscoveredTime());
        
        asset.setDescription(description.toString());
        asset.setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 判断是否应该创建新资产
     */
    private boolean shouldCreateNewAsset(AssetDiscoveryResult result) {
        // 只有在线的主机才自动创建资产
        if (!result.getIsAlive()) {
            return false;
        }
        
        // 有开放端口的主机更可能是有价值的资产
        if (result.getOpenPorts() != null && !result.getOpenPorts().equals("[]")) {
            return true;
        }
        
        // 有明确服务的主机
        if (result.getServices() != null && !result.getServices().equals("{}")) {
            return true;
        }
        
        // 有操作系统信息的主机
        if (result.getOperatingSystem() != null && !result.getOperatingSystem().equals("Unknown")) {
            return true;
        }
        
        return false;
    }

    /**
     * 从发现结果创建新资产
     */
    private Asset createAssetFromResult(AssetDiscoveryResult result) {
        Asset asset = new Asset();
        
        // 设置基本信息
        if (result.getHostname() != null && !result.getHostname().isEmpty()) {
            asset.setName(result.getHostname());
        } else {
            asset.setName("Host-" + result.getIpAddress().replace(".", "-"));
        }
        
        asset.setIpAddress(result.getIpAddress());
        
        // 根据发现的信息推测资产类型
        Asset.AssetType assetType = guessAssetType(result);
        asset.setType(assetType);
        
        // 设置状态
        asset.setStatus(result.getIsAlive() ? Asset.Status.ACTIVE : Asset.Status.INACTIVE);

        // 设置重要性（默认为中等）
        asset.setImportance(Asset.Importance.MEDIUM);
        
        // 设置操作系统
        if (result.getOperatingSystem() != null) {
            asset.setOperatingSystem(result.getOperatingSystem());
        }
        
        // 构建描述信息
        StringBuilder description = new StringBuilder();
        description.append("自动发现的资产\n");
        description.append("发现时间: ").append(result.getDiscoveredTime()).append("\n");
        
        if (result.getDeviceType() != null) {
            description.append("设备类型: ").append(result.getDeviceType()).append("\n");
        }
        
        if (result.getVendor() != null) {
            description.append("厂商: ").append(result.getVendor()).append("\n");
        }
        
        if (result.getMacAddress() != null) {
            description.append("MAC地址: ").append(result.getMacAddress()).append("\n");
        }
        
        if (result.getOpenPorts() != null) {
            description.append("开放端口: ").append(result.getOpenPorts()).append("\n");
        }
        
        if (result.getServices() != null) {
            description.append("检测到的服务: ").append(result.getServices()).append("\n");
        }
        
        asset.setDescription(description.toString());
        
        // 设置创建时间
        asset.setCreatedTime(LocalDateTime.now());
        asset.setUpdatedTime(LocalDateTime.now());
        
        return asset;
    }

    /**
     * 根据发现结果推测资产类型
     */
    private Asset.AssetType guessAssetType(AssetDiscoveryResult result) {
        // 根据开放端口和服务推测资产类型
        try {
            if (result.getServices() != null) {
                String services = result.getServices().toLowerCase();
                
                if (services.contains("http") || services.contains("https")) {
                    return Asset.AssetType.WEB_APPLICATION;
                }
                
                if (services.contains("mysql") || services.contains("postgresql") || 
                    services.contains("oracle") || services.contains("mssql") ||
                    services.contains("mongodb") || services.contains("redis")) {
                    return Asset.AssetType.DATABASE;
                }
                
                if (services.contains("ssh") || services.contains("rdp") || 
                    services.contains("telnet")) {
                    return Asset.AssetType.SERVER;
                }
                
                if (services.contains("snmp") || services.contains("161") || 
                    services.contains("162")) {
                    return Asset.AssetType.NETWORK_DEVICE;
                }
            }
            
            // 根据设备类型推测
            if (result.getDeviceType() != null) {
                String deviceType = result.getDeviceType().toLowerCase();
                
                if (deviceType.contains("server")) {
                    return Asset.AssetType.SERVER;
                }
                
                if (deviceType.contains("web")) {
                    return Asset.AssetType.WEB_APPLICATION;
                }
                
                if (deviceType.contains("database")) {
                    return Asset.AssetType.DATABASE;
                }
                
                if (deviceType.contains("router") || deviceType.contains("switch") || 
                    deviceType.contains("firewall")) {
                    return Asset.AssetType.NETWORK_DEVICE;
                }
            }
            
        } catch (Exception e) {
            logger.debug("推测资产类型失败: {}", e.getMessage());
        }
        
        // 默认返回服务器类型
        return Asset.AssetType.SERVER;
    }

    /**
     * 手动关联资产
     */
    public void manualCorrelate(Long resultId, Long assetId) {
        AssetDiscoveryResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("发现结果不存在: " + resultId));
        
        Asset asset = assetRepository.findByIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new RuntimeException("资产不存在: " + assetId));
        
        result.setAssetId(assetId);
        result.setCorrelationStatus(AssetDiscoveryResult.CorrelationStatus.MATCHED);
        resultRepository.save(result);
        
        // 更新资产信息
        updateAssetFromResult(asset, result);
        assetRepository.save(asset);
        
        logger.info("手动关联资产: {} -> {}", result.getIpAddress(), asset.getName());
    }

    /**
     * 忽略发现结果
     */
    public void ignoreResult(Long resultId) {
        AssetDiscoveryResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("发现结果不存在: " + resultId));
        
        result.setCorrelationStatus(AssetDiscoveryResult.CorrelationStatus.IGNORED);
        resultRepository.save(result);
        
        logger.info("忽略发现结果: {}", result.getIpAddress());
    }
}
