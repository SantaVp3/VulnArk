package com.vulnark.service;

import com.vulnark.entity.ScanResult;
import com.vulnark.repository.ScanResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 扫描结果服务
 */
@Service
public class ScanResultService {
    
    @Autowired
    private ScanResultRepository scanResultRepository;
    
    /**
     * 获取扫描结果
     */
    public List<ScanResult> getScanResults(Long scanId) {
        return scanResultRepository.findByScanIdOrderByCreatedTimeDesc(scanId);
    }
    
    /**
     * 根据严重程度获取扫描结果
     */
    public List<ScanResult> getScanResultsBySeverity(Long scanId, String severity) {
        return scanResultRepository.findByScanIdAndSeverityOrderByCreatedTimeDesc(scanId, severity);
    }
    
    /**
     * 获取扫描结果统计
     */
    public Map<String, Object> getScanResultStats(Long scanId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总数
        long totalCount = scanResultRepository.countByScanId(scanId);
        stats.put("totalCount", totalCount);
        
        // 按严重程度统计
        List<Object[]> severityStats = scanResultRepository.countByScanIdGroupBySeverity(scanId);
        Map<String, Long> severityMap = new HashMap<>();
        
        // 初始化所有严重程度为0
        severityMap.put("HIGH", 0L);
        severityMap.put("MEDIUM", 0L);
        severityMap.put("LOW", 0L);
        severityMap.put("INFO", 0L);
        
        // 填充实际统计数据
        for (Object[] stat : severityStats) {
            String severity = (String) stat[0];
            Long count = (Long) stat[1];
            severityMap.put(severity, count);
        }
        
        stats.put("highCount", severityMap.get("HIGH"));
        stats.put("mediumCount", severityMap.get("MEDIUM"));
        stats.put("lowCount", severityMap.get("LOW"));
        stats.put("infoCount", severityMap.get("INFO"));
        
        return stats;
    }
    
    /**
     * 获取单个扫描结果
     */
    public ScanResult getScanResult(Long resultId) {
        Optional<ScanResult> result = scanResultRepository.findById(resultId);
        if (result.isEmpty()) {
            throw new RuntimeException("扫描结果不存在: " + resultId);
        }
        return result.get();
    }
    
    /**
     * 保存扫描结果
     */
    public ScanResult saveScanResult(ScanResult scanResult) {
        return scanResultRepository.save(scanResult);
    }
    
    /**
     * 批量保存扫描结果
     */
    public List<ScanResult> saveScanResults(List<ScanResult> scanResults) {
        return scanResultRepository.saveAll(scanResults);
    }
    
    /**
     * 删除扫描结果
     */
    public void deleteScanResult(Long resultId) {
        if (!scanResultRepository.existsById(resultId)) {
            throw new RuntimeException("扫描结果不存在: " + resultId);
        }
        scanResultRepository.deleteById(resultId);
    }
    
    /**
     * 删除扫描的所有结果
     */
    @Transactional
    public void deleteScanResults(Long scanId) {
        scanResultRepository.deleteByScanId(scanId);
    }
}
