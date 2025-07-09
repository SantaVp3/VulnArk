package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.entity.AssetDetection;
import com.vulnark.entity.AssetFingerprint;
import com.vulnark.service.AssetDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Tag(name = "资产检测", description = "资产检测相关接口")
@RestController
@RequestMapping("/api/asset-detection")
public class AssetDetectionController {
    
    @Autowired
    private AssetDetectionService detectionService;
    
    @Operation(summary = "检测单个资产")
    @PostMapping("/detect/{assetId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<AssetDetectionService.DetectionResult> detectAsset(
            @Parameter(description = "资产ID") @PathVariable Long assetId,
            @Parameter(description = "是否包含指纹识别") @RequestParam(defaultValue = "true") boolean includeFingerprint) {
        try {
            CompletableFuture<AssetDetectionService.DetectionResult> future = 
                    detectionService.detectAsset(assetId, includeFingerprint);
            AssetDetectionService.DetectionResult result = future.get();
            
            if (result.isSuccess()) {
                return ApiResponse.success("资产检测完成", result);
            } else {
                return ApiResponse.error("资产检测失败: " + result.getErrorMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("资产检测失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "批量检测资产")
    @PostMapping("/detect/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<List<AssetDetectionService.DetectionResult>> detectAssets(
            @Parameter(description = "资产ID列表") @RequestBody List<Long> assetIds,
            @Parameter(description = "是否包含指纹识别") @RequestParam(defaultValue = "true") boolean includeFingerprint) {
        try {
            CompletableFuture<List<AssetDetectionService.DetectionResult>> future = 
                    detectionService.detectAssets(assetIds, includeFingerprint);
            List<AssetDetectionService.DetectionResult> results = future.get();
            
            long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
            return ApiResponse.success(
                String.format("批量检测完成，成功: %d，失败: %d", successCount, results.size() - successCount), 
                results);
        } catch (Exception e) {
            return ApiResponse.error("批量检测失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "检测项目下的所有资产")
    @PostMapping("/detect/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<List<AssetDetectionService.DetectionResult>> detectProjectAssets(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "是否包含指纹识别") @RequestParam(defaultValue = "true") boolean includeFingerprint) {
        try {
            CompletableFuture<List<AssetDetectionService.DetectionResult>> future = 
                    detectionService.detectProjectAssets(projectId, includeFingerprint);
            List<AssetDetectionService.DetectionResult> results = future.get();
            
            long successCount = results.stream().mapToLong(r -> r.isSuccess() ? 1 : 0).sum();
            return ApiResponse.success(
                String.format("项目资产检测完成，成功: %d，失败: %d", successCount, results.size() - successCount), 
                results);
        } catch (Exception e) {
            return ApiResponse.error("项目资产检测失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取资产检测历史")
    @GetMapping("/history/{assetId}")
    public ApiResponse<Page<AssetDetection>> getAssetDetectionHistory(
            @Parameter(description = "资产ID") @PathVariable Long assetId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size) {
        try {
            Page<AssetDetection> history = detectionService.getAssetDetectionHistory(assetId, page, size);
            return ApiResponse.success("获取检测历史成功", history);
        } catch (Exception e) {
            return ApiResponse.error("获取检测历史失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取资产最新检测状态")
    @GetMapping("/latest/{assetId}")
    public ApiResponse<List<AssetDetection>> getAssetLatestDetections(
            @Parameter(description = "资产ID") @PathVariable Long assetId) {
        try {
            List<AssetDetection> detections = detectionService.getAssetLatestDetections(assetId);
            return ApiResponse.success("获取最新检测状态成功", detections);
        } catch (Exception e) {
            return ApiResponse.error("获取最新检测状态失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取资产指纹信息")
    @GetMapping("/fingerprints/{assetId}")
    public ApiResponse<List<AssetFingerprint>> getAssetFingerprints(
            @Parameter(description = "资产ID") @PathVariable Long assetId) {
        try {
            List<AssetFingerprint> fingerprints = detectionService.getAssetFingerprints(assetId);
            return ApiResponse.success("获取资产指纹成功", fingerprints);
        } catch (Exception e) {
            return ApiResponse.error("获取资产指纹失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取检测统计信息")
    @GetMapping("/statistics/detection")
    public ApiResponse<AssetDetectionService.DetectionStatistics> getDetectionStatistics() {
        try {
            AssetDetectionService.DetectionStatistics stats = detectionService.getDetectionStatistics();
            return ApiResponse.success("获取检测统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error("获取检测统计失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "获取指纹统计信息")
    @GetMapping("/statistics/fingerprint")
    public ApiResponse<AssetDetectionService.FingerprintStatistics> getFingerprintStatistics() {
        try {
            AssetDetectionService.FingerprintStatistics stats = detectionService.getFingerprintStatistics();
            return ApiResponse.success("获取指纹统计成功", stats);
        } catch (Exception e) {
            return ApiResponse.error("获取指纹统计失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "异步检测单个资产（立即返回）")
    @PostMapping("/detect-async/{assetId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<String> detectAssetAsync(
            @Parameter(description = "资产ID") @PathVariable Long assetId,
            @Parameter(description = "是否包含指纹识别") @RequestParam(defaultValue = "true") boolean includeFingerprint) {
        try {
            // 异步执行，不等待结果
            detectionService.detectAsset(assetId, includeFingerprint);
            return ApiResponse.success("资产检测任务已启动", "检测任务已在后台执行");
        } catch (Exception e) {
            return ApiResponse.error("启动资产检测失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "异步批量检测资产（立即返回）")
    @PostMapping("/detect-async/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<String> detectAssetsAsync(
            @Parameter(description = "资产ID列表") @RequestBody List<Long> assetIds,
            @Parameter(description = "是否包含指纹识别") @RequestParam(defaultValue = "true") boolean includeFingerprint) {
        try {
            // 异步执行，不等待结果
            detectionService.detectAssets(assetIds, includeFingerprint);
            return ApiResponse.success("批量检测任务已启动", 
                String.format("已启动 %d 个资产的检测任务", assetIds.size()));
        } catch (Exception e) {
            return ApiResponse.error("启动批量检测失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "异步检测项目资产（立即返回）")
    @PostMapping("/detect-async/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<String> detectProjectAssetsAsync(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "是否包含指纹识别") @RequestParam(defaultValue = "true") boolean includeFingerprint) {
        try {
            // 异步执行，不等待结果
            detectionService.detectProjectAssets(projectId, includeFingerprint);
            return ApiResponse.success("项目资产检测任务已启动", "检测任务已在后台执行");
        } catch (Exception e) {
            return ApiResponse.error("启动项目资产检测失败: " + e.getMessage());
        }
    }
}
