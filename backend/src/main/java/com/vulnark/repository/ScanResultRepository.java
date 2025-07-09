package com.vulnark.repository;

import com.vulnark.entity.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 扫描结果数据访问层
 */
@Repository
public interface ScanResultRepository extends JpaRepository<ScanResult, Long> {
    
    /**
     * 根据扫描ID查找结果
     */
    List<ScanResult> findByScanIdOrderByCreatedTimeDesc(Long scanId);
    
    /**
     * 根据扫描ID和严重程度查找结果
     */
    List<ScanResult> findByScanIdAndSeverityOrderByCreatedTimeDesc(Long scanId, String severity);
    
    /**
     * 根据扫描ID统计不同严重程度的漏洞数量
     */
    @Query("SELECT r.severity, COUNT(r) FROM ScanResult r WHERE r.scanId = :scanId GROUP BY r.severity")
    List<Object[]> countByScanIdGroupBySeverity(@Param("scanId") Long scanId);
    
    /**
     * 根据扫描ID统计总漏洞数量
     */
    long countByScanId(Long scanId);
    
    /**
     * 根据扫描ID删除结果
     */
    void deleteByScanId(Long scanId);
}
