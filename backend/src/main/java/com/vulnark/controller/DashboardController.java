package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.security.CurrentUser;
import com.vulnark.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<?> getDashboardStats() {
        try {
            return ApiResponse.success(dashboardService.getDashboardStats());
        } catch (Exception e) {
            logger.error("获取仪表盘统计数据失败", e);
            return ApiResponse.error("获取仪表盘统计数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/vulnerability-trend")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<?> getVulnerabilityTrend(@RequestParam(defaultValue = "30") int days) {
        try {
            // 假设我们需要使用现有的方法，比如getVulnerabilityTrends
            return ApiResponse.success(dashboardService.getVulnerabilityTrends(days));
        } catch (Exception e) {
            logger.error("获取漏洞趋势数据失败", e);
            return ApiResponse.error("获取漏洞趋势数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/severity-distribution")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<?> getSeverityDistribution() {
        try {
            // 假设我们需要使用现有的方法
            return ApiResponse.success(dashboardService.getVulnerabilitySeverityDistribution());
        } catch (Exception e) {
            logger.error("获取漏洞严重程度分布数据失败", e);
            return ApiResponse.error("获取漏洞严重程度分布数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/asset-status-distribution")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<?> getAssetStatusDistribution() {
        try {
            return ApiResponse.success(dashboardService.getAssetStatusDistribution());
        } catch (Exception e) {
            logger.error("获取资产状态分布数据失败", e);
            return ApiResponse.error("获取资产状态分布数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/recent-activities")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<?> getRecentActivities(@RequestParam(defaultValue = "10") int limit) {
        try {
            return ApiResponse.success(dashboardService.getRecentActivities(limit));
        } catch (Exception e) {
            logger.error("获取最近活动数据失败", e);
            return ApiResponse.error("获取最近活动数据失败: " + e.getMessage());
        }
    }
}
