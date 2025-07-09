package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.entity.ScanResult;
import com.vulnark.service.ScanResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 扫描结果控制器
 */
@RestController
@RequestMapping("/api/scan-results")
public class ScanResultController {
    
    @Autowired
    private ScanResultService scanResultService;
    
    /**
     * 获取扫描结果
     */
    @GetMapping("/scan/{scanId}")
    public ApiResponse<List<ScanResult>> getScanResults(@PathVariable Long scanId) {
        try {
            List<ScanResult> results = scanResultService.getScanResults(scanId);
            return ApiResponse.success("获取扫描结果成功", results);
        } catch (Exception e) {
            return ApiResponse.error("获取扫描结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取扫描结果统计
     */
    @GetMapping("/scan/{scanId}/stats")
    public ApiResponse<Map<String, Object>> getScanResultStats(@PathVariable Long scanId) {
        try {
            Map<String, Object> stats = scanResultService.getScanResultStats(scanId);
            return ApiResponse.success("获取扫描统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error("获取扫描统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据严重程度获取扫描结果
     */
    @GetMapping("/scan/{scanId}/severity/{severity}")
    public ApiResponse<List<ScanResult>> getScanResultsBySeverity(
            @PathVariable Long scanId, 
            @PathVariable String severity) {
        try {
            List<ScanResult> results = scanResultService.getScanResultsBySeverity(scanId, severity);
            return ApiResponse.success("获取扫描结果成功", results);
        } catch (Exception e) {
            return ApiResponse.error("获取扫描结果失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个扫描结果详情
     */
    @GetMapping("/{resultId}")
    public ApiResponse<ScanResult> getScanResult(@PathVariable Long resultId) {
        try {
            ScanResult result = scanResultService.getScanResult(resultId);
            return ApiResponse.success("获取扫描结果详情成功", result);
        } catch (Exception e) {
            return ApiResponse.error("获取扫描结果详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除扫描结果
     */
    @DeleteMapping("/{resultId}")
    public ApiResponse<String> deleteScanResult(@PathVariable Long resultId) {
        try {
            scanResultService.deleteScanResult(resultId);
            return ApiResponse.success("删除扫描结果成功");
        } catch (Exception e) {
            return ApiResponse.error("删除扫描结果失败: " + e.getMessage());
        }
    }

    /**
     * 删除扫描的所有结果
     */
    @DeleteMapping("/scan/{scanId}")
    public ApiResponse<String> deleteScanResults(@PathVariable Long scanId) {
        try {
            scanResultService.deleteScanResults(scanId);
            return ApiResponse.success("删除扫描结果成功");
        } catch (Exception e) {
            return ApiResponse.error("删除扫描结果失败: " + e.getMessage());
        }
    }
}
