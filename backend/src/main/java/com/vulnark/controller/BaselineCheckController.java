package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.entity.BaselineCheck;
import com.vulnark.entity.BaselineCheckItem;
import com.vulnark.entity.User;
import com.vulnark.service.BaselineCheckService;
import com.vulnark.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 基线检查控制器
 */
@RestController
@RequestMapping("/api/baseline")
@Tag(name = "基线检查管理", description = "基线检查相关接口")
public class BaselineCheckController {

    private static final Logger logger = LoggerFactory.getLogger(BaselineCheckController.class);

    @Autowired
    private BaselineCheckService baselineCheckService;

    @Autowired
    private UserService userService;

    /**
     * 创建基线检查
     */
    @PostMapping("/checks")
    @Operation(summary = "创建基线检查", description = "创建新的基线检查任务")
    public ApiResponse<BaselineCheck> createBaselineCheck(
            @RequestBody BaselineCheck baselineCheck,
            Authentication authentication) {
        try {
            User currentUser = userService.findByUsername(authentication.getName()).orElse(null);
            if (currentUser == null) {
                return ApiResponse.error("用户不存在");
            }
            BaselineCheck created = baselineCheckService.createBaselineCheck(baselineCheck, currentUser);
            return ApiResponse.success(created);
        } catch (Exception e) {
            logger.error("创建基线检查失败", e);
            return ApiResponse.error("创建基线检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取基线检查详情
     */
    @GetMapping("/checks/{id}")
    @Operation(summary = "获取基线检查详情", description = "根据ID获取基线检查详细信息")
    public ApiResponse<BaselineCheck> getBaselineCheck(
            @Parameter(description = "基线检查ID") @PathVariable Long id) {
        try {
            Optional<BaselineCheck> baselineCheck = baselineCheckService.getBaselineCheckById(id);
            if (baselineCheck.isPresent()) {
                return ApiResponse.success(baselineCheck.get());
            } else {
                return ApiResponse.error("基线检查不存在");
            }
        } catch (Exception e) {
            logger.error("获取基线检查详情失败", e);
            return ApiResponse.error("获取基线检查详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询基线检查
     */
    @GetMapping("/checks/search")
    @Operation(summary = "搜索基线检查", description = "分页查询基线检查列表")
    public ApiResponse<Page<BaselineCheck>> searchBaselineChecks(
            @Parameter(description = "检查名称") @RequestParam(required = false) String name,
            @Parameter(description = "检查类型") @RequestParam(required = false) BaselineCheck.CheckType checkType,
            @Parameter(description = "检查状态") @RequestParam(required = false) BaselineCheck.CheckStatus status,
            @Parameter(description = "资产ID") @RequestParam(required = false) Long assetId,
            @Parameter(description = "创建者ID") @RequestParam(required = false) Long createdById,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdTime") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BaselineCheck> result = baselineCheckService.searchBaselineChecks(
                name, checkType, status, assetId, createdById, pageable);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("搜索基线检查失败", e);
            return ApiResponse.error("搜索基线检查失败: " + e.getMessage());
        }
    }

    /**
     * 启动基线检查
     */
    @PostMapping("/checks/{id}/start")
    @Operation(summary = "启动基线检查", description = "启动指定的基线检查任务")
    public ApiResponse<String> startBaselineCheck(
            @Parameter(description = "基线检查ID") @PathVariable Long id) {
        try {
            baselineCheckService.startBaselineCheck(id);
            return ApiResponse.success("基线检查已启动");
        } catch (Exception e) {
            logger.error("启动基线检查失败", e);
            return ApiResponse.error("启动基线检查失败: " + e.getMessage());
        }
    }

    /**
     * 停止基线检查
     */
    @PostMapping("/checks/{id}/stop")
    @Operation(summary = "停止基线检查", description = "停止正在运行的基线检查任务")
    public ApiResponse<String> stopBaselineCheck(
            @Parameter(description = "基线检查ID") @PathVariable Long id) {
        try {
            baselineCheckService.stopBaselineCheck(id);
            return ApiResponse.success("基线检查已停止");
        } catch (Exception e) {
            logger.error("停止基线检查失败", e);
            return ApiResponse.error("停止基线检查失败: " + e.getMessage());
        }
    }

    /**
     * 删除基线检查
     */
    @DeleteMapping("/checks/{id}")
    @Operation(summary = "删除基线检查", description = "删除指定的基线检查任务")
    public ApiResponse<String> deleteBaselineCheck(
            @Parameter(description = "基线检查ID") @PathVariable Long id) {
        try {
            baselineCheckService.deleteBaselineCheck(id);
            return ApiResponse.success("基线检查已删除");
        } catch (Exception e) {
            logger.error("删除基线检查失败", e);
            return ApiResponse.error("删除基线检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取检查项列表
     */
    @GetMapping("/checks/{id}/items")
    @Operation(summary = "获取检查项列表", description = "获取基线检查的检查项列表")
    public ApiResponse<Page<BaselineCheckItem>> getCheckItems(
            @Parameter(description = "基线检查ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "itemCode") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BaselineCheckItem> result = baselineCheckService.getCheckItems(id, pageable);
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取检查项列表失败", e);
            return ApiResponse.error("获取检查项列表失败: " + e.getMessage());
        }
    }

    /**
     * 搜索检查项
     */
    @GetMapping("/checks/{id}/items/search")
    @Operation(summary = "搜索检查项", description = "条件搜索基线检查项")
    public ApiResponse<Page<BaselineCheckItem>> searchCheckItems(
            @Parameter(description = "基线检查ID") @PathVariable Long id,
            @Parameter(description = "检查项名称") @RequestParam(required = false) String itemName,
            @Parameter(description = "检查分类") @RequestParam(required = false) String category,
            @Parameter(description = "严重级别") @RequestParam(required = false) BaselineCheckItem.SeverityLevel severity,
            @Parameter(description = "检查状态") @RequestParam(required = false) BaselineCheckItem.ItemStatus status,
            @Parameter(description = "检查结果") @RequestParam(required = false) BaselineCheckItem.ItemResult result,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "itemCode") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BaselineCheckItem> searchResult = baselineCheckService.searchCheckItems(
                id, itemName, category, severity, status, result, pageable);

            return ApiResponse.success(searchResult);
        } catch (Exception e) {
            logger.error("搜索检查项失败", e);
            return ApiResponse.error("搜索检查项失败: " + e.getMessage());
        }
    }

    /**
     * 获取失败的检查项
     */
    @GetMapping("/checks/{id}/items/failed")
    @Operation(summary = "获取失败的检查项", description = "获取基线检查中失败的检查项")
    public ApiResponse<List<BaselineCheckItem>> getFailedItems(
            @Parameter(description = "基线检查ID") @PathVariable Long id) {
        try {
            List<BaselineCheckItem> failedItems = baselineCheckService.getFailedItems(id);
            return ApiResponse.success(failedItems);
        } catch (Exception e) {
            logger.error("获取失败检查项失败", e);
            return ApiResponse.error("获取失败检查项失败: " + e.getMessage());
        }
    }

    /**
     * 获取高风险失败项
     */
    @GetMapping("/checks/{id}/items/high-risk")
    @Operation(summary = "获取高风险失败项", description = "获取基线检查中高风险的失败项")
    public ApiResponse<List<BaselineCheckItem>> getHighRiskFailedItems(
            @Parameter(description = "基线检查ID") @PathVariable Long id) {
        try {
            List<BaselineCheckItem> highRiskItems = baselineCheckService.getHighRiskFailedItems(id);
            return ApiResponse.success(highRiskItems);
        } catch (Exception e) {
            logger.error("获取高风险失败项失败", e);
            return ApiResponse.error("获取高风险失败项失败: " + e.getMessage());
        }
    }

    /**
     * 获取检查统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取检查统计信息", description = "获取基线检查的统计信息")
    public ApiResponse<BaselineCheckService.BaselineCheckStatistics> getCheckStatistics() {
        try {
            BaselineCheckService.BaselineCheckStatistics statistics = baselineCheckService.getCheckStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            logger.error("获取检查统计信息失败", e);
            return ApiResponse.error("获取检查统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取资产检查统计
     */
    @GetMapping("/statistics/assets")
    @Operation(summary = "获取资产检查统计", description = "获取各资产的基线检查统计信息")
    public ApiResponse<List<BaselineCheckService.AssetCheckStatistics>> getAssetCheckStatistics() {
        try {
            List<BaselineCheckService.AssetCheckStatistics> statistics = baselineCheckService.getAssetCheckStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            logger.error("获取资产检查统计失败", e);
            return ApiResponse.error("获取资产检查统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取检查类型枚举
     */
    @GetMapping("/check-types")
    @Operation(summary = "获取检查类型", description = "获取所有可用的基线检查类型")
    public ApiResponse<BaselineCheck.CheckType[]> getCheckTypes() {
        return ApiResponse.success(BaselineCheck.CheckType.values());
    }

    /**
     * 获取严重级别枚举
     */
    @GetMapping("/severity-levels")
    @Operation(summary = "获取严重级别", description = "获取所有可用的严重级别")
    public ApiResponse<BaselineCheckItem.SeverityLevel[]> getSeverityLevels() {
        return ApiResponse.success(BaselineCheckItem.SeverityLevel.values());
    }
}
