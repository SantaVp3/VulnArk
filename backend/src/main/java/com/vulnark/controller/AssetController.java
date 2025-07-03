package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.dto.AssetQueryRequest;
import com.vulnark.dto.AssetRequest;
import com.vulnark.entity.Asset;
import com.vulnark.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "资产管理", description = "资产管理相关接口")
@RestController
@RequestMapping("/assets")
public class AssetController {
    
    @Autowired
    private AssetService assetService;
    
    @Operation(summary = "创建资产")
    @PostMapping
    public ApiResponse<Asset> createAsset(@Valid @RequestBody AssetRequest request) {
        try {
            Asset asset = assetService.createAsset(request);
            return ApiResponse.success("资产创建成功", asset);
        } catch (Exception e) {
            return ApiResponse.error("创建失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "更新资产")
    @PutMapping("/{id}")
    public ApiResponse<Asset> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        try {
            Asset asset = assetService.updateAsset(id, request);
            return ApiResponse.success("资产更新成功", asset);
        } catch (Exception e) {
            return ApiResponse.error("更新失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "删除资产")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteAsset(
            @Parameter(description = "资产ID") @PathVariable Long id) {
        try {
            assetService.deleteAsset(id);
            return ApiResponse.success("资产删除成功", "删除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据ID获取资产详情")
    @GetMapping("/{id}")
    public ApiResponse<Asset> getAssetById(
            @Parameter(description = "资产ID") @PathVariable Long id) {
        try {
            Asset asset = assetService.getAssetById(id);
            return ApiResponse.success("获取资产详情成功", asset);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "分页查询资产")
    @GetMapping
    public ApiResponse<Page<Asset>> getAssets(
            @Parameter(description = "资产名称关键词") @RequestParam(required = false) String name,
            @Parameter(description = "资产类型") @RequestParam(required = false) Asset.AssetType type,
            @Parameter(description = "资产状态") @RequestParam(required = false) Asset.Status status,
            @Parameter(description = "重要性等级") @RequestParam(required = false) Asset.Importance importance,
            @Parameter(description = "所属项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "负责人ID") @RequestParam(required = false) Long ownerId,
            @Parameter(description = "IP地址关键词") @RequestParam(required = false) String ipAddress,
            @Parameter(description = "域名关键词") @RequestParam(required = false) String domain,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdTime") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletResponse response) {
        try {
            // 添加缓存控制头，防止浏览器缓存
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            // 打印所有查询参数
            AssetQueryRequest request = new AssetQueryRequest();
            request.setName(name);
            request.setType(type);
            request.setStatus(status);
            request.setImportance(importance);
            request.setProjectId(projectId);
            request.setOwnerId(ownerId);
            request.setIpAddress(ipAddress);
            request.setDomain(domain);
            request.setKeyword(keyword);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDir(sortDir);
            
            Page<Asset> assets = assetService.getAssets(request);
            return ApiResponse.success("获取资产列表成功", assets);
        } catch (Exception e) {
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取所有资产")
    @GetMapping("/all")
    public ApiResponse<List<Asset>> getAllAssets() {
        try {
            List<Asset> assets = assetService.getAllAssets();
            return ApiResponse.success("获取所有资产成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据项目ID获取资产")
    @GetMapping("/project/{projectId}")
    public ApiResponse<List<Asset>> getAssetsByProjectId(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        try {
            List<Asset> assets = assetService.getAssetsByProjectId(projectId);
            return ApiResponse.success("获取项目资产成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "根据负责人ID获取资产")
    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Asset>> getAssetsByOwnerId(
            @Parameter(description = "负责人ID") @PathVariable Long ownerId) {
        try {
            List<Asset> assets = assetService.getAssetsByOwnerId(ownerId);
            return ApiResponse.success("获取负责人资产成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取最近的资产")
    @GetMapping("/recent")
    public ApiResponse<List<Asset>> getRecentAssets(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Asset> assets = assetService.getRecentAssets(limit);
            return ApiResponse.success("获取最近资产成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取高风险资产")
    @GetMapping("/high-risk")
    public ApiResponse<List<Asset>> getHighRiskAssets(
            @Parameter(description = "最小风险评分") @RequestParam(required = false) Double minRiskScore) {
        try {
            List<Asset> assets = assetService.getHighRiskAssets(minRiskScore);
            return ApiResponse.success("获取高风险资产成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取需要扫描的资产")
    @GetMapping("/need-scan")
    public ApiResponse<List<Asset>> getAssetsNeedingScan(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer daysBefore) {
        try {
            List<Asset> assets = assetService.getAssetsNeedingScan(daysBefore);
            return ApiResponse.success("获取需要扫描的资产成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取活跃资产")
    @GetMapping("/active")
    public ApiResponse<List<Asset>> getActiveAssets() {
        try {
            List<Asset> assets = assetService.getActiveAssets();
            return ApiResponse.success("获取活跃资产成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新资产状态")
    @PutMapping("/{id}/status")
    public ApiResponse<Asset> updateAssetStatus(
            @Parameter(description = "资产ID") @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Asset.Status status = Asset.Status.valueOf(request.get("status"));
            Asset asset = assetService.updateAssetStatus(id, status);
            return ApiResponse.success("资产状态更新成功", asset);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新资产风险评分")
    @PutMapping("/{id}/risk-score")
    public ApiResponse<Asset> updateAssetRiskScore(
            @Parameter(description = "资产ID") @PathVariable Long id,
            @RequestBody Map<String, Double> request) {
        try {
            Double riskScore = request.get("riskScore");
            Asset asset = assetService.updateAssetRiskScore(id, riskScore);
            return ApiResponse.success("资产风险评分更新成功", asset);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新资产扫描时间")
    @PutMapping("/{id}/scan-time")
    public ApiResponse<Asset> updateAssetScanTime(
            @Parameter(description = "资产ID") @PathVariable Long id) {
        try {
            Asset asset = assetService.updateAssetScanTime(id);
            return ApiResponse.success("资产扫描时间更新成功", asset);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新资产统计信息")
    @PutMapping("/{id}/statistics")
    public ApiResponse<Asset> updateAssetStatistics(
            @Parameter(description = "资产ID") @PathVariable Long id) {
        try {
            Asset asset = assetService.updateAssetStatisticsAndReturn(id);
            return ApiResponse.success("资产统计信息更新成功", asset);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "获取资产统计信息")
    @GetMapping("/stats")
    public ApiResponse<AssetService.AssetStats> getAssetStats() {
        try {
            AssetService.AssetStats stats = assetService.getAssetStats();
            return ApiResponse.success("获取资产统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "批量导入资产")
    @PostMapping("/import")
    public ApiResponse<List<Asset>> importAssets(@RequestBody List<AssetRequest> requests) {
        try {
            List<Asset> assets = assetService.importAssets(requests);
            return ApiResponse.success("资产导入成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "批量导出资产")
    @PostMapping("/export")
    public ApiResponse<List<Asset>> exportAssets(@RequestBody(required = false) List<Long> assetIds) {
        try {
            List<Asset> assets = assetService.exportAssets(assetIds);
            return ApiResponse.success("资产导出成功", assets);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @Operation(summary = "批量删除资产")
    @PostMapping("/batch-delete")
    public ApiResponse<String> batchDeleteAssets(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ApiResponse.error("资产ID列表不能为空");
            }
            
            for (Long id : ids) {
                assetService.deleteAsset(id);
            }
            
            return ApiResponse.success("批量删除资产成功", "删除成功");
        } catch (Exception e) {
            return ApiResponse.error("批量删除失败: " + e.getMessage());
        }
    }
}
