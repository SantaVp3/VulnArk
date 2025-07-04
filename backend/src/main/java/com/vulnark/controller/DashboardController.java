package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "仪表盘管理", description = "仪表盘统计数据管理接口")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "测试端点", description = "测试认证是否工作")
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testEndpoint() {
        return ResponseEntity.ok(ApiResponse.success("认证成功", "Hello World"));
    }

    @Operation(summary = "获取仪表盘统计数据", description = "获取系统整体统计数据")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardService.DashboardStats>> getDashboardStats() {
        try {
            DashboardService.DashboardStats stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(ApiResponse.success("获取仪表盘统计数据成功", stats));
        } catch (Exception e) {
            logger.error("获取仪表盘统计数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取仪表盘统计数据失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取漏洞趋势数据", description = "获取指定天数内的漏洞发现和解决趋势")
    @GetMapping("/vulnerability-trends")
    public ResponseEntity<ApiResponse<List<DashboardService.VulnerabilityTrendData>>> getVulnerabilityTrends(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        try {
            List<DashboardService.VulnerabilityTrendData> trends = dashboardService.getVulnerabilityTrends(days);
            return ResponseEntity.ok(ApiResponse.success("获取漏洞趋势数据成功", trends));
        } catch (Exception e) {
            logger.error("获取漏洞趋势数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取漏洞趋势数据失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取漏洞严重程度分布", description = "获取漏洞按严重程度的分布统计")
    @GetMapping("/vulnerability-severity-distribution")
    public ResponseEntity<ApiResponse<List<DashboardService.SeverityDistribution>>> getVulnerabilitySeverityDistribution() {
        try {
            List<DashboardService.SeverityDistribution> distribution = dashboardService.getVulnerabilitySeverityDistribution();
            return ResponseEntity.ok(ApiResponse.success("获取漏洞严重程度分布成功", distribution));
        } catch (Exception e) {
            logger.error("获取漏洞严重程度分布失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取漏洞严重程度分布失败: " + e.getMessage()));
        }
    }



    @Operation(summary = "获取资产状态分布", description = "获取资产按状态的分布统计")
    @GetMapping("/asset-status-distribution")
    public ResponseEntity<ApiResponse<List<DashboardService.AssetStatusDistribution>>> getAssetStatusDistribution() {
        try {
            List<DashboardService.AssetStatusDistribution> distribution = dashboardService.getAssetStatusDistribution();
            return ResponseEntity.ok(ApiResponse.success("获取资产状态分布成功", distribution));
        } catch (Exception e) {
            logger.error("获取资产状态分布失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取资产状态分布失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取最近活动", description = "获取系统最近的活动记录")
    @GetMapping("/recent-activities")
    public ResponseEntity<ApiResponse<List<DashboardService.RecentActivity>>> getRecentActivities(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<DashboardService.RecentActivity> activities = dashboardService.getRecentActivities(limit);
            return ResponseEntity.ok(ApiResponse.success("获取最近活动成功", activities));
        } catch (Exception e) {
            logger.error("获取最近活动失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取最近活动失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取系统健康状态", description = "获取系统运行健康状态")
    @GetMapping("/system-health")
    public ResponseEntity<ApiResponse<DashboardService.SystemHealth>> getSystemHealth() {
        try {
            DashboardService.SystemHealth health = dashboardService.getSystemHealth();
            return ResponseEntity.ok(ApiResponse.success("获取系统健康状态成功", health));
        } catch (Exception e) {
            logger.error("获取系统健康状态失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取系统健康状态失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "刷新仪表盘数据", description = "手动刷新仪表盘缓存数据")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refreshDashboardData() {
        try {
            dashboardService.refreshDashboardData();
            return ResponseEntity.ok(ApiResponse.success("仪表盘数据刷新成功", "数据已刷新"));
        } catch (Exception e) {
            logger.error("刷新仪表盘数据失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("刷新仪表盘数据失败: " + e.getMessage()));
        }
    }
}
