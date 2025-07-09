package com.vulnark.controller;

import com.vulnark.entity.BaselineScan;
import com.vulnark.service.BaselineScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 基线扫描控制器 - 临时实现
 */
@Tag(name = "基线扫描", description = "基线扫描相关接口")
@RestController
@RequestMapping("/api/baseline-scans")
public class BaselineScanController {

    @Autowired
    private BaselineScanService baselineScanService;

    /**
     * 获取基线扫描列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getScans(
            @RequestParam(required = false) String scanName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String scanType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<BaselineScan> scanPage = baselineScanService.getScans(scanName, status, scanType, page, size);

            List<Map<String, Object>> scans = new ArrayList<>();
            for (BaselineScan scan : scanPage.getContent()) {
                Map<String, Object> scanMap = new HashMap<>();
                scanMap.put("id", scan.getId());
                scanMap.put("scanName", scan.getScanName());
                scanMap.put("description", scan.getDescription());
                scanMap.put("assetId", scan.getAssetId());
                scanMap.put("scanType", scan.getScanType());
                scanMap.put("status", scan.getStatus().toString());
                scanMap.put("totalChecks", scan.getTotalChecks());
                scanMap.put("passedChecks", scan.getPassedChecks());
                scanMap.put("failedChecks", scan.getFailedChecks());
                scanMap.put("warningChecks", scan.getWarningChecks());
                scanMap.put("complianceScore", scan.getComplianceScore());
                scanMap.put("createdTime", scan.getCreatedTime().toString());
                scanMap.put("startTime", scan.getStartTime() != null ? scan.getStartTime().toString() : null);
                scanMap.put("endTime", scan.getEndTime() != null ? scan.getEndTime().toString() : null);
                scans.add(scanMap);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("content", scans);
            result.put("totalElements", scanPage.getTotalElements());
            result.put("totalPages", scanPage.getTotalPages());
            result.put("currentPage", page);
            result.put("size", size);

            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "查询成功",
                "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "查询失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取扫描统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = baselineScanService.getStatistics();

            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "查询成功",
                "data", statistics
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "查询统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 创建扫描任务
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createScan(@RequestBody Map<String, Object> request) {
        try {
            // 验证必填字段
            String scanName = (String) request.get("scanName");
            Object assetIdObj = request.get("assetId");
            String scanType = (String) request.get("scanType");

            if (scanName == null || scanName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "扫描名称不能为空"
                ));
            }

            if (assetIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "必须选择目标资产"
                ));
            }

            if (scanType == null || scanType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "必须选择扫描类型"
                ));
            }

            Long assetId = Long.valueOf(assetIdObj.toString());
            String description = (String) request.get("description");
            Boolean executeImmediately = Boolean.TRUE.equals(request.get("executeImmediately"));

            BaselineScan scan = baselineScanService.createScan(scanName, description, assetId, scanType, executeImmediately);

            Map<String, Object> scanMap = new HashMap<>();
            scanMap.put("id", scan.getId());
            scanMap.put("scanName", scan.getScanName());
            scanMap.put("description", scan.getDescription());
            scanMap.put("assetId", scan.getAssetId());
            scanMap.put("scanType", scan.getScanType());
            scanMap.put("status", scan.getStatus().toString());
            scanMap.put("createdTime", scan.getCreatedTime().toString());

            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "基线扫描任务创建成功",
                "data", scanMap
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "创建扫描任务失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 执行扫描
     */
    @PostMapping("/{scanId}/execute")
    public ResponseEntity<Map<String, Object>> executeScan(@PathVariable Long scanId) {
        try {
            baselineScanService.executeScan(scanId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "基线扫描已开始执行",
                "data", Map.of("scanId", scanId, "status", "RUNNING")
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "执行扫描失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 取消扫描
     */
    @PostMapping("/{scanId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelScan(@PathVariable Long scanId) {
        try {
            baselineScanService.cancelScan(scanId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "扫描已取消"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "取消扫描失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 重新执行扫描
     */
    @PostMapping("/{scanId}/rerun")
    public ResponseEntity<Map<String, Object>> rerunScan(@PathVariable Long scanId) {
        try {
            baselineScanService.rerunScan(scanId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "重新执行已开始"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "重新执行失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除扫描
     */
    @DeleteMapping("/{scanId}")
    public ResponseEntity<Map<String, Object>> deleteScan(@PathVariable Long scanId) {
        try {
            baselineScanService.deleteScan(scanId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "删除失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取扫描详情
     */
    @GetMapping("/{scanId}")
    public ResponseEntity<Map<String, Object>> getScanById(@PathVariable Long scanId) {
        try {
            Optional<BaselineScan> scanOpt = baselineScanService.getScanById(scanId);
            if (scanOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 404,
                    "message", "扫描任务不存在: " + scanId
                ));
            }

            BaselineScan scan = scanOpt.get();
            Map<String, Object> scanMap = new HashMap<>();
            scanMap.put("id", scan.getId());
            scanMap.put("scanName", scan.getScanName());
            scanMap.put("description", scan.getDescription());
            scanMap.put("assetId", scan.getAssetId());
            scanMap.put("scanType", scan.getScanType());
            scanMap.put("status", scan.getStatus().toString());
            scanMap.put("totalChecks", scan.getTotalChecks());
            scanMap.put("passedChecks", scan.getPassedChecks());
            scanMap.put("failedChecks", scan.getFailedChecks());
            scanMap.put("warningChecks", scan.getWarningChecks());
            scanMap.put("complianceScore", scan.getComplianceScore());
            scanMap.put("createdTime", scan.getCreatedTime().toString());
            scanMap.put("startTime", scan.getStartTime() != null ? scan.getStartTime().toString() : null);
            scanMap.put("endTime", scan.getEndTime() != null ? scan.getEndTime().toString() : null);
            scanMap.put("errorMessage", scan.getErrorMessage());

            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "查询成功",
                "data", scanMap
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 500,
                "message", "查询扫描详情失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取扫描结果
     */
    @GetMapping("/{scanId}/results")
    public ResponseEntity<Map<String, Object>> getScanResults(@PathVariable Long scanId) {
        // TODO: 实现真实的查询逻辑
        // 1. 验证扫描任务是否存在
        // 2. 查询扫描结果

        List<Map<String, Object>> results = new ArrayList<>();

        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "查询成功",
            "data", results
        ));
    }

    /**
     * 获取失败的检查项
     */
    @GetMapping("/{scanId}/failed-checks")
    public ResponseEntity<Map<String, Object>> getFailedChecks(@PathVariable Long scanId) {
        List<Map<String, Object>> failedChecks = new ArrayList<>();

        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "查询成功",
            "data", failedChecks
        ));
    }

    /**
     * 获取高危失败的检查项
     */
    @GetMapping("/{scanId}/high-risk-failed-checks")
    public ResponseEntity<Map<String, Object>> getHighRiskFailedChecks(@PathVariable Long scanId) {
        List<Map<String, Object>> highRiskFailedChecks = new ArrayList<>();

        return ResponseEntity.ok(Map.of(
            "code", 200,
            "message", "查询成功",
            "data", highRiskFailedChecks
        ));
    }
}
