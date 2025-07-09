package com.vulnark.service;

import com.vulnark.entity.Asset;
import com.vulnark.entity.BaselineScan;
import com.vulnark.repository.AssetRepository;
import com.vulnark.repository.BaselineScanRepository;
import com.vulnark.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 基线扫描服务
 */
@Service
@Transactional
public class BaselineScanService {
    
    @Autowired
    private BaselineScanRepository baselineScanRepository;
    
    @Autowired
    private AssetRepository assetRepository;
    
    /**
     * 分页查询扫描记录
     */
    public Page<BaselineScan> getScans(String scanName, String status, String scanType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        
        BaselineScan.ScanStatus scanStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                scanStatus = BaselineScan.ScanStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 忽略无效的状态值
            }
        }
        
        return baselineScanRepository.findByConditions(
            scanName != null && !scanName.trim().isEmpty() ? scanName.trim() : null,
            scanStatus,
            scanType != null && !scanType.trim().isEmpty() ? scanType.trim() : null,
            pageable
        );
    }
    
    /**
     * 创建扫描任务
     */
    public BaselineScan createScan(String scanName, String description, Long assetId, String scanType, boolean executeImmediately) {
        // 验证资产是否存在
        Optional<Asset> assetOpt = assetRepository.findById(assetId);
        if (assetOpt.isEmpty()) {
            throw new RuntimeException("资产不存在: " + assetId);
        }
        
        Asset asset = assetOpt.get();
        
        // 创建扫描记录
        BaselineScan scan = new BaselineScan();
        scan.setScanName(scanName);
        scan.setDescription(description);
        scan.setAssetId(assetId);
        scan.setScanType(scanType);
        scan.setStatus(BaselineScan.ScanStatus.PENDING);
        
        BaselineScan savedScan = baselineScanRepository.save(scan);
        
        // 如果设置了立即执行，启动扫描任务
        if (executeImmediately) {
            executeScan(savedScan.getId());
        }
        
        return savedScan;
    }
    
    /**
     * 获取扫描详情
     */
    public Optional<BaselineScan> getScanById(Long scanId) {
        return baselineScanRepository.findByIdNotDeleted(scanId);
    }
    
    /**
     * 执行扫描
     */
    public void executeScan(Long scanId) {
        Optional<BaselineScan> scanOpt = baselineScanRepository.findByIdNotDeleted(scanId);
        if (scanOpt.isEmpty()) {
            throw new RuntimeException("扫描任务不存在: " + scanId);
        }
        
        BaselineScan scan = scanOpt.get();
        
        // 检查状态
        if (scan.getStatus() == BaselineScan.ScanStatus.RUNNING) {
            throw new RuntimeException("扫描任务正在执行中");
        }
        
        // 更新状态为执行中
        scan.setStatus(BaselineScan.ScanStatus.RUNNING);
        scan.setStartTime(LocalDateTime.now());
        scan.setErrorMessage(null);
        baselineScanRepository.save(scan);
        
        // TODO: 实现真实的扫描逻辑
        // 这里应该启动异步扫描任务
        // 暂时模拟扫描完成
        simulateScanCompletion(scanId);
    }
    
    /**
     * 取消扫描
     */
    public void cancelScan(Long scanId) {
        Optional<BaselineScan> scanOpt = baselineScanRepository.findByIdNotDeleted(scanId);
        if (scanOpt.isEmpty()) {
            throw new RuntimeException("扫描任务不存在: " + scanId);
        }
        
        BaselineScan scan = scanOpt.get();
        
        if (scan.getStatus() != BaselineScan.ScanStatus.PENDING && scan.getStatus() != BaselineScan.ScanStatus.RUNNING) {
            throw new RuntimeException("只能取消等待中或执行中的扫描任务");
        }
        
        scan.setStatus(BaselineScan.ScanStatus.CANCELLED);
        scan.setEndTime(LocalDateTime.now());
        baselineScanRepository.save(scan);
    }
    
    /**
     * 删除扫描
     */
    public void deleteScan(Long scanId) {
        Optional<BaselineScan> scanOpt = baselineScanRepository.findByIdNotDeleted(scanId);
        if (scanOpt.isEmpty()) {
            throw new RuntimeException("扫描任务不存在: " + scanId);
        }
        
        BaselineScan scan = scanOpt.get();
        scan.setDeleted(true);
        baselineScanRepository.save(scan);
    }
    
    /**
     * 重新执行扫描
     */
    public void rerunScan(Long scanId) {
        Optional<BaselineScan> scanOpt = baselineScanRepository.findByIdNotDeleted(scanId);
        if (scanOpt.isEmpty()) {
            throw new RuntimeException("扫描任务不存在: " + scanId);
        }
        
        BaselineScan scan = scanOpt.get();
        
        // 重置扫描状态和结果
        scan.setStatus(BaselineScan.ScanStatus.PENDING);
        scan.setStartTime(null);
        scan.setEndTime(null);
        scan.setTotalChecks(0);
        scan.setPassedChecks(0);
        scan.setFailedChecks(0);
        scan.setWarningChecks(0);
        scan.setComplianceScore(0.0);
        scan.setErrorMessage(null);
        
        baselineScanRepository.save(scan);
        
        // 立即执行
        executeScan(scanId);
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总扫描数量
        Long totalScans = baselineScanRepository.countNotDeleted();
        statistics.put("totalScans", totalScans);
        
        // 平均合规分数
        Double avgScore = baselineScanRepository.getAverageComplianceScore();
        statistics.put("averageComplianceScore", avgScore != null ? avgScore : 0.0);
        
        // 状态分布
        Map<String, Long> statusDistribution = new HashMap<>();
        List<Object[]> statusCounts = baselineScanRepository.countByStatus();
        for (Object[] row : statusCounts) {
            statusDistribution.put(row[0].toString(), (Long) row[1]);
        }
        statistics.put("statusDistribution", statusDistribution);
        
        // 类型分布
        Map<String, Long> typeDistribution = new HashMap<>();
        List<Object[]> typeCounts = baselineScanRepository.countByScanType();
        for (Object[] row : typeCounts) {
            typeDistribution.put(row[0].toString(), (Long) row[1]);
        }
        statistics.put("typeDistribution", typeDistribution);
        
        // 最近扫描
        List<BaselineScan> recentScans = baselineScanRepository.findRecentScans(PageRequest.of(0, 5));
        statistics.put("recentScans", recentScans);
        
        return statistics;
    }
    
    /**
     * 模拟扫描完成（临时方法）
     */
    private void simulateScanCompletion(Long scanId) {
        // 这里应该是异步执行的真实扫描逻辑
        // 暂时直接标记为完成
        Optional<BaselineScan> scanOpt = baselineScanRepository.findByIdNotDeleted(scanId);
        if (scanOpt.isPresent()) {
            BaselineScan scan = scanOpt.get();
            scan.setStatus(BaselineScan.ScanStatus.COMPLETED);
            scan.setEndTime(LocalDateTime.now());
            scan.setTotalChecks(25);
            scan.setPassedChecks(20);
            scan.setFailedChecks(3);
            scan.setWarningChecks(2);
            scan.setComplianceScore(80.0);
            baselineScanRepository.save(scan);
        }
    }
}
