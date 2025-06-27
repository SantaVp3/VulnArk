package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.dto.AssetDependencyRequest;
import com.vulnark.dto.AssetDependencyResponse;
import com.vulnark.entity.AssetDependency;
import com.vulnark.entity.User;
import com.vulnark.security.CurrentUser;
import com.vulnark.service.AssetDependencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产依赖关系控制器
 */
@RestController
@RequestMapping("/asset-dependencies")
@Tag(name = "资产依赖关系管理", description = "资产依赖关系的增删改查和拓扑图功能")
public class AssetDependencyController {
    
    private static final Logger logger = LoggerFactory.getLogger(AssetDependencyController.class);
    
    @Autowired
    private AssetDependencyService dependencyService;
    
    @Operation(summary = "创建资产依赖关系", description = "创建新的资产依赖关系")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<AssetDependency>> createDependency(
            @Valid @RequestBody AssetDependencyRequest request,
            @CurrentUser User currentUser) {
        try {
            AssetDependency dependency = dependencyService.createDependency(request, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("创建资产依赖关系成功", dependency));
        } catch (Exception e) {
            logger.error("创建资产依赖关系失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建资产依赖关系失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "更新资产依赖关系", description = "更新指定的资产依赖关系")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<AssetDependency>> updateDependency(
            @Parameter(description = "依赖关系ID") @PathVariable Long id,
            @Valid @RequestBody AssetDependencyRequest request) {
        try {
            AssetDependency dependency = dependencyService.updateDependency(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新资产依赖关系成功", dependency));
        } catch (Exception e) {
            logger.error("更新资产依赖关系失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新资产依赖关系失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "删除资产依赖关系", description = "删除指定的资产依赖关系")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteDependency(
            @Parameter(description = "依赖关系ID") @PathVariable Long id) {
        try {
            dependencyService.deleteDependency(id);
            return ResponseEntity.ok(ApiResponse.success("删除资产依赖关系成功", null));
        } catch (Exception e) {
            logger.error("删除资产依赖关系失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除资产依赖关系失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "批量删除资产依赖关系", description = "批量删除多个资产依赖关系")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> batchDeleteDependencies(
            @RequestBody List<Long> ids) {
        try {
            dependencyService.batchDeleteDependencies(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除资产依赖关系成功", null));
        } catch (Exception e) {
            logger.error("批量删除资产依赖关系失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量删除资产依赖关系失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取资产依赖关系详情", description = "根据ID获取资产依赖关系详情")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetDependencyResponse>> getDependencyById(
            @Parameter(description = "依赖关系ID") @PathVariable Long id) {
        try {
            AssetDependencyResponse dependency = dependencyService.getDependencyById(id);
            return ResponseEntity.ok(ApiResponse.success("获取资产依赖关系成功", dependency));
        } catch (Exception e) {
            logger.error("获取资产依赖关系失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取资产依赖关系失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取资产的所有依赖关系", description = "获取指定资产的所有依赖关系")
    @GetMapping("/asset/{assetId}")
    public ResponseEntity<ApiResponse<List<AssetDependencyResponse>>> getAssetDependencies(
            @Parameter(description = "资产ID") @PathVariable Long assetId) {
        try {
            List<AssetDependencyResponse> dependencies = dependencyService.getAssetDependencies(assetId);
            return ResponseEntity.ok(ApiResponse.success("获取资产依赖关系成功", dependencies));
        } catch (Exception e) {
            logger.error("获取资产依赖关系失败: {}", assetId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取资产依赖关系失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取资产的直接依赖", description = "获取指定资产的直接依赖关系")
    @GetMapping("/asset/{assetId}/direct")
    public ResponseEntity<ApiResponse<List<AssetDependencyResponse>>> getDirectDependencies(
            @Parameter(description = "资产ID") @PathVariable Long assetId) {
        try {
            List<AssetDependencyResponse> dependencies = dependencyService.getDirectDependencies(assetId);
            return ResponseEntity.ok(ApiResponse.success("获取资产直接依赖成功", dependencies));
        } catch (Exception e) {
            logger.error("获取资产直接依赖失败: {}", assetId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取资产直接依赖失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取资产的反向依赖", description = "获取依赖指定资产的其他资产")
    @GetMapping("/asset/{assetId}/reverse")
    public ResponseEntity<ApiResponse<List<AssetDependencyResponse>>> getReverseDependencies(
            @Parameter(description = "资产ID") @PathVariable Long assetId) {
        try {
            List<AssetDependencyResponse> dependencies = dependencyService.getReverseDependencies(assetId);
            return ResponseEntity.ok(ApiResponse.success("获取资产反向依赖成功", dependencies));
        } catch (Exception e) {
            logger.error("获取资产反向依赖失败: {}", assetId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取资产反向依赖失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取项目依赖拓扑图", description = "获取指定项目的资产依赖拓扑图")
    @GetMapping("/topology/project/{projectId}")
    public ResponseEntity<ApiResponse<AssetDependencyService.AssetDependencyTopology>> getProjectTopology(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {
        try {
            AssetDependencyService.AssetDependencyTopology topology = 
                    dependencyService.getProjectDependencyTopology(projectId);
            return ResponseEntity.ok(ApiResponse.success("获取项目依赖拓扑图成功", topology));
        } catch (Exception e) {
            logger.error("获取项目依赖拓扑图失败: {}", projectId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取项目依赖拓扑图失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "分析依赖路径", description = "分析两个资产之间的依赖路径")
    @GetMapping("/path/analyze")
    public ResponseEntity<ApiResponse<AssetDependencyService.DependencyPathAnalysis>> analyzeDependencyPath(
            @Parameter(description = "源资产ID") @RequestParam Long sourceAssetId,
            @Parameter(description = "目标资产ID") @RequestParam Long targetAssetId) {
        try {
            AssetDependencyService.DependencyPathAnalysis analysis = 
                    dependencyService.analyzeDependencyPath(sourceAssetId, targetAssetId);
            return ResponseEntity.ok(ApiResponse.success("依赖路径分析成功", analysis));
        } catch (Exception e) {
            logger.error("依赖路径分析失败: {} -> {}", sourceAssetId, targetAssetId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("依赖路径分析失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "检测循环依赖", description = "检测系统中的循环依赖关系")
    @GetMapping("/circular")
    public ResponseEntity<ApiResponse<List<AssetDependencyResponse>>> detectCircularDependencies() {
        try {
            List<AssetDependencyResponse> circularDeps = dependencyService.detectCircularDependencies();
            return ResponseEntity.ok(ApiResponse.success("循环依赖检测成功", circularDeps));
        } catch (Exception e) {
            logger.error("循环依赖检测失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("循环依赖检测失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取依赖统计信息", description = "获取依赖关系的统计信息")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AssetDependencyService.DependencyStatistics>> getDependencyStatistics(
            @Parameter(description = "项目ID（可选）") @RequestParam(required = false) Long projectId) {
        try {
            AssetDependencyService.DependencyStatistics stats = 
                    dependencyService.getDependencyStatistics(projectId);
            return ResponseEntity.ok(ApiResponse.success("获取依赖统计信息成功", stats));
        } catch (Exception e) {
            logger.error("获取依赖统计信息失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取依赖统计信息失败: " + e.getMessage()));
        }
    }
}
