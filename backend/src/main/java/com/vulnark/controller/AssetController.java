package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.dto.AssetQueryRequest;
import com.vulnark.dto.AssetRequest;
import com.vulnark.entity.Asset;
import com.vulnark.security.CurrentUser;
import com.vulnark.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "资产管理", description = "资产管理相关接口")
@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private static final Logger logger = LoggerFactory.getLogger(AssetController.class);

    @Autowired
    private AssetService assetService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "获取资产列表", description = "支持分页和条件查询")
    public ApiResponse<?> getAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Asset.AssetType type,
            @RequestParam(required = false) Asset.Status status,
            @RequestParam(required = false) Asset.Importance importance,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            AssetQueryRequest request = new AssetQueryRequest();
            request.setPage(page);
            request.setSize(size);
            request.setName(name);
            request.setType(type);
            request.setStatus(status);
            request.setImportance(importance);
            request.setOwnerId(ownerId);
            request.setIpAddress(ipAddress);
            request.setDomain(domain);
            request.setKeyword(keyword);
            request.setSortBy(sortBy);
            request.setSortDir(sortDir);
            
            Page<Asset> assets = assetService.getAssets(request);
            return ApiResponse.success(assets);
        } catch (Exception e) {
            logger.error("获取资产列表失败", e);
            return ApiResponse.error("获取资产列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "获取所有资产", description = "获取所有资产，不分页")
    public ApiResponse<List<Asset>> getAllAssets() {
        try {
            List<Asset> assets = assetService.getAllAssets();
            return ApiResponse.success(assets);
        } catch (Exception e) {
            logger.error("获取所有资产失败", e);
            return ApiResponse.error("获取所有资产失败: " + e.getMessage());
        }
    }

    @PostMapping("/query")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "查询资产", description = "使用POST方式查询资产")
    public ApiResponse<Page<Asset>> queryAssets(@RequestBody AssetQueryRequest request) {
        try {
            Page<Asset> assets = assetService.getAssets(request);
            return ApiResponse.success(assets);
        } catch (Exception e) {
            logger.error("查询资产失败", e);
            return ApiResponse.error("查询资产失败: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "创建资产", description = "创建新的资产")
    public ApiResponse<Asset> createAsset(@RequestBody AssetRequest request) {
        try {
            Asset asset = assetService.createAsset(request);
            return ApiResponse.success(asset);
        } catch (Exception e) {
            logger.error("创建资产失败", e);
            return ApiResponse.error("创建资产失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "获取资产详情", description = "根据ID获取资产详情")
    public ApiResponse<Asset> getAssetById(@PathVariable Long id) {
        try {
            Asset asset = assetService.getAssetById(id);
            return ApiResponse.success(asset);
        } catch (Exception e) {
            logger.error("获取资产详情失败", e);
            return ApiResponse.error("获取资产详情失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "更新资产", description = "更新资产信息")
    public ApiResponse<Asset> updateAsset(@PathVariable Long id, @RequestBody AssetRequest request) {
        try {
            Asset asset = assetService.updateAsset(id, request);
            return ApiResponse.success(asset);
        } catch (Exception e) {
            logger.error("更新资产失败", e);
            return ApiResponse.error("更新资产失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "删除资产", description = "删除资产")
    public ApiResponse<Void> deleteAsset(@PathVariable Long id) {
        try {
            assetService.deleteAsset(id);
            return ApiResponse.success();
        } catch (Exception e) {
            logger.error("删除资产失败", e);
            return ApiResponse.error("删除资产失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "批量删除资产", description = "批量删除资产")
    public ApiResponse<Void> batchDeleteAssets(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            if (ids != null && !ids.isEmpty()) {
                for (Long id : ids) {
                    assetService.deleteAsset(id);
                }
            }
            return ApiResponse.success();
        } catch (Exception e) {
            logger.error("批量删除资产失败", e);
            return ApiResponse.error("批量删除资产失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "获取资产统计", description = "获取资产统计信息")
    public ApiResponse<AssetService.AssetStats> getAssetStats() {
        try {
            AssetService.AssetStats stats = assetService.getAssetStats();
            return ApiResponse.success(stats);
        } catch (Exception e) {
            logger.error("获取资产统计失败", e);
            return ApiResponse.error("获取资产统计失败: " + e.getMessage());
        }
    }
}
