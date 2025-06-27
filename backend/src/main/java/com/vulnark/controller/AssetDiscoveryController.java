package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.dto.AssetDiscoveryTaskRequest;
import com.vulnark.dto.AssetDiscoveryTaskResponse;
import com.vulnark.entity.AssetDiscoveryResult;
import com.vulnark.entity.AssetDiscoveryTask;
import com.vulnark.entity.User;
import com.vulnark.security.CurrentUser;
import com.vulnark.service.AssetCorrelationService;
import com.vulnark.service.AssetDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产发现控制器
 */
@RestController
@RequestMapping("/asset-discovery")
@Tag(name = "资产发现管理", description = "资产自动发现和指纹识别功能")
public class AssetDiscoveryController {

    private static final Logger logger = LoggerFactory.getLogger(AssetDiscoveryController.class);

    @Autowired
    private AssetDiscoveryService discoveryService;

    @Autowired
    private AssetCorrelationService correlationService;

    @Operation(summary = "创建发现任务", description = "创建新的资产发现任务")
    @PostMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<AssetDiscoveryTaskResponse>> createTask(
            @Valid @RequestBody AssetDiscoveryTaskRequest request,
            @CurrentUser User currentUser) {
        try {
            AssetDiscoveryTask task = request.toEntity();
            AssetDiscoveryTask createdTask = discoveryService.createTask(task, currentUser.getId());
            AssetDiscoveryTaskResponse response = AssetDiscoveryTaskResponse.fromEntity(createdTask);
            return ResponseEntity.ok(ApiResponse.success("创建发现任务成功", response));
        } catch (Exception e) {
            logger.error("创建发现任务失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建发现任务失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "更新发现任务", description = "更新指定的发现任务")
    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<AssetDiscoveryTaskResponse>> updateTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Valid @RequestBody AssetDiscoveryTaskRequest request) {
        try {
            AssetDiscoveryTask taskUpdate = request.toEntity();
            AssetDiscoveryTask updatedTask = discoveryService.updateTask(id, taskUpdate);
            AssetDiscoveryTaskResponse response = AssetDiscoveryTaskResponse.fromEntity(updatedTask);
            return ResponseEntity.ok(ApiResponse.success("更新发现任务成功", response));
        } catch (Exception e) {
            logger.error("更新发现任务失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新发现任务失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "删除发现任务", description = "删除指定的发现任务")
    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteTask(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            discoveryService.deleteTask(id);
            return ResponseEntity.ok(ApiResponse.success("删除发现任务成功", null));
        } catch (Exception e) {
            logger.error("删除发现任务失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除发现任务失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取发现任务详情", description = "根据ID获取发现任务详情")
    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<AssetDiscoveryTaskResponse>> getTaskById(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            AssetDiscoveryTask task = discoveryService.getTaskById(id);
            AssetDiscoveryTaskResponse response = AssetDiscoveryTaskResponse.fromEntity(task);
            return ResponseEntity.ok(ApiResponse.success("获取发现任务成功", response));
        } catch (Exception e) {
            logger.error("获取发现任务失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取发现任务失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "分页查询发现任务", description = "分页查询发现任务列表")
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<Page<AssetDiscoveryTaskResponse>>> getTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<AssetDiscoveryTask> taskPage = discoveryService.getTasks(page, size);
            Page<AssetDiscoveryTaskResponse> responsePage = taskPage.map(AssetDiscoveryTaskResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.success("获取发现任务列表成功", responsePage));
        } catch (Exception e) {
            logger.error("获取发现任务列表失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取发现任务列表失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "执行发现任务", description = "立即执行指定的发现任务")
    @PostMapping("/tasks/{id}/execute")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> executeTask(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            discoveryService.executeTask(id);
            return ResponseEntity.ok(ApiResponse.success("发现任务已开始执行", null));
        } catch (Exception e) {
            logger.error("执行发现任务失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("执行发现任务失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "取消发现任务", description = "取消正在执行的发现任务")
    @PostMapping("/tasks/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> cancelTask(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            discoveryService.cancelTask(id);
            return ResponseEntity.ok(ApiResponse.success("发现任务已取消", null));
        } catch (Exception e) {
            logger.error("取消发现任务失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("取消发现任务失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取任务结果", description = "分页获取任务的发现结果")
    @GetMapping("/tasks/{id}/results")
    public ResponseEntity<ApiResponse<Page<AssetDiscoveryResult>>> getTaskResults(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Page<AssetDiscoveryResult> results = discoveryService.getTaskResults(id, page, size);
            return ResponseEntity.ok(ApiResponse.success("获取任务结果成功", results));
        } catch (Exception e) {
            logger.error("获取任务结果失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取任务结果失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取任务统计", description = "获取任务的统计信息")
    @GetMapping("/tasks/{id}/statistics")
    public ResponseEntity<ApiResponse<AssetDiscoveryService.TaskStatistics>> getTaskStatistics(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            AssetDiscoveryService.TaskStatistics statistics = discoveryService.getTaskStatistics(id);
            return ResponseEntity.ok(ApiResponse.success("获取任务统计成功", statistics));
        } catch (Exception e) {
            logger.error("获取任务统计失败: {}", id, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取任务统计失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取系统统计", description = "获取资产发现系统的统计信息")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<AssetDiscoveryService.SystemStatistics>> getSystemStatistics() {
        try {
            AssetDiscoveryService.SystemStatistics statistics = discoveryService.getSystemStatistics();
            return ResponseEntity.ok(ApiResponse.success("获取系统统计成功", statistics));
        } catch (Exception e) {
            logger.error("获取系统统计失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取系统统计失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "手动关联资产", description = "手动将发现结果关联到指定资产")
    @PostMapping("/results/{resultId}/correlate/{assetId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> manualCorrelate(
            @Parameter(description = "结果ID") @PathVariable Long resultId,
            @Parameter(description = "资产ID") @PathVariable Long assetId) {
        try {
            correlationService.manualCorrelate(resultId, assetId);
            return ResponseEntity.ok(ApiResponse.success("手动关联资产成功", null));
        } catch (Exception e) {
            logger.error("手动关联资产失败: {} -> {}", resultId, assetId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("手动关联资产失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "忽略发现结果", description = "忽略指定的发现结果")
    @PostMapping("/results/{resultId}/ignore")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> ignoreResult(
            @Parameter(description = "结果ID") @PathVariable Long resultId) {
        try {
            correlationService.ignoreResult(resultId);
            return ResponseEntity.ok(ApiResponse.success("忽略发现结果成功", null));
        } catch (Exception e) {
            logger.error("忽略发现结果失败: {}", resultId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("忽略发现结果失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取目标类型选项", description = "获取可用的目标类型选项")
    @GetMapping("/target-types")
    public ResponseEntity<ApiResponse<List<EnumOption>>> getTargetTypes() {
        List<EnumOption> options = List.of(AssetDiscoveryTask.TargetType.values())
                .stream()
                .map(type -> new EnumOption(type.name(), type.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("获取目标类型选项成功", options));
    }

    @Operation(summary = "获取扫描类型选项", description = "获取可用的扫描类型选项")
    @GetMapping("/scan-types")
    public ResponseEntity<ApiResponse<List<EnumOption>>> getScanTypes() {
        List<EnumOption> options = List.of(AssetDiscoveryTask.ScanType.values())
                .stream()
                .map(type -> new EnumOption(type.name(), type.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("获取扫描类型选项成功", options));
    }

    @Operation(summary = "获取调度类型选项", description = "获取可用的调度类型选项")
    @GetMapping("/schedule-types")
    public ResponseEntity<ApiResponse<List<EnumOption>>> getScheduleTypes() {
        List<EnumOption> options = List.of(AssetDiscoveryTask.ScheduleType.values())
                .stream()
                .map(type -> new EnumOption(type.name(), type.getDescription()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("获取调度类型选项成功", options));
    }

    /**
     * 枚举选项DTO
     */
    public static class EnumOption {
        private String value;
        private String label;

        public EnumOption(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
}
