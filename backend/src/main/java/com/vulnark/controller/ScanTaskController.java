package com.vulnark.controller;

import com.vulnark.common.ApiResponse;
import com.vulnark.entity.*;
import com.vulnark.service.ScanTaskService;
import com.vulnark.service.scan.ScanEngineException;
import com.vulnark.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Tag(name = "扫描任务管理", description = "扫描任务的创建、启动、停止、查询等操作")
@RestController
@RequestMapping("/api/scan-tasks")
public class ScanTaskController {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanTaskController.class);
    
    @Autowired
    private ScanTaskService scanTaskService;
    
    @Operation(summary = "创建扫描任务", description = "创建新的扫描任务")
    @PostMapping
    public ResponseEntity<ApiResponse<ScanTask>> createScanTask(
            @Valid @RequestBody ScanTaskService.ScanTaskRequest request,
            @CurrentUser User currentUser) {
        try {
            ScanTask scanTask = scanTaskService.createScanTask(request, currentUser);
            return ResponseEntity.ok(ApiResponse.success("创建扫描任务成功", scanTask));
        } catch (Exception e) {
            logger.error("创建扫描任务失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建扫描任务失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "启动扫描任务", description = "启动指定的扫描任务")
    @PostMapping("/{taskId}/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST')")
    public ResponseEntity<ApiResponse<String>> startScanTask(
            @Parameter(description = "扫描任务ID") @PathVariable Long taskId) {
        try {
            CompletableFuture<Void> future = scanTaskService.startScanTask(taskId);
            return ResponseEntity.ok(ApiResponse.success("扫描任务启动成功", "任务正在后台执行"));
        } catch (Exception e) {
            logger.error("启动扫描任务失败: {}", taskId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("启动扫描任务失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "暂停扫描任务", description = "暂停正在运行的扫描任务")
    @PostMapping("/{taskId}/pause")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST')")
    public ResponseEntity<ApiResponse<String>> pauseScanTask(
            @Parameter(description = "扫描任务ID") @PathVariable Long taskId) {
        try {
            scanTaskService.pauseScanTask(taskId);
            return ResponseEntity.ok(ApiResponse.success("暂停扫描任务成功", null));
        } catch (ScanEngineException e) {
            logger.error("暂停扫描任务失败: {}", taskId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("暂停扫描任务失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "恢复扫描任务", description = "恢复已暂停的扫描任务")
    @PostMapping("/{taskId}/resume")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST')")
    public ResponseEntity<ApiResponse<String>> resumeScanTask(
            @Parameter(description = "扫描任务ID") @PathVariable Long taskId) {
        try {
            scanTaskService.resumeScanTask(taskId);
            return ResponseEntity.ok(ApiResponse.success("恢复扫描任务成功", null));
        } catch (ScanEngineException e) {
            logger.error("恢复扫描任务失败: {}", taskId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("恢复扫描任务失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "停止扫描任务", description = "停止正在运行的扫描任务")
    @PostMapping("/{taskId}/stop")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST')")
    public ResponseEntity<ApiResponse<String>> stopScanTask(
            @Parameter(description = "扫描任务ID") @PathVariable Long taskId) {
        try {
            scanTaskService.stopScanTask(taskId);
            return ResponseEntity.ok(ApiResponse.success("停止扫描任务成功", null));
        } catch (ScanEngineException e) {
            logger.error("停止扫描任务失败: {}", taskId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("停止扫描任务失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取扫描任务列表", description = "分页查询扫描任务列表")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ScanTask>>> getScanTasks(
            @Parameter(description = "任务名称") @RequestParam(required = false) String name,
            @Parameter(description = "任务状态") @RequestParam(required = false) ScanTask.TaskStatus status,
            @Parameter(description = "扫描类型") @RequestParam(required = false) ScanTask.ScanType scanType,
            @Parameter(description = "扫描引擎") @RequestParam(required = false) ScanTask.ScanEngine scanEngine,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "创建者ID") @RequestParam(required = false) Long createdById,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") int size) {
        try {
            ScanTaskService.ScanTaskQueryRequest request = new ScanTaskService.ScanTaskQueryRequest();
            request.setName(name);
            request.setStatus(status);
            request.setScanType(scanType);
            request.setScanEngine(scanEngine);
            request.setProjectId(projectId);
            request.setCreatedById(createdById);
            
            Page<ScanTask> tasks = scanTaskService.getScanTasks(request, page, size);
            return ResponseEntity.ok(ApiResponse.success("获取扫描任务列表成功", tasks));
        } catch (Exception e) {
            logger.error("获取扫描任务列表失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取扫描任务列表失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取扫描任务详情", description = "根据ID获取扫描任务详细信息")
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<ScanTask>> getScanTaskById(
            @Parameter(description = "扫描任务ID") @PathVariable Long taskId) {
        try {
            ScanTask scanTask = scanTaskService.getScanTaskById(taskId);
            return ResponseEntity.ok(ApiResponse.success("获取扫描任务详情成功", scanTask));
        } catch (Exception e) {
            logger.error("获取扫描任务详情失败: {}", taskId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取扫描任务详情失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取扫描任务目标", description = "获取扫描任务的目标资产列表")
    @GetMapping("/{taskId}/targets")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<ScanTaskTarget>>> getScanTaskTargets(
            @Parameter(description = "扫描任务ID") @PathVariable Long taskId) {
        try {
            List<ScanTaskTarget> targets = scanTaskService.getScanTaskTargets(taskId);
            return ResponseEntity.ok(ApiResponse.success("获取扫描任务目标成功", targets));
        } catch (Exception e) {
            logger.error("获取扫描任务目标失败: {}", taskId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取扫描任务目标失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取扫描任务漏洞", description = "获取扫描任务发现的漏洞列表")
    @GetMapping("/{taskId}/vulnerabilities")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST') or hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<Vulnerability>>> getScanTaskVulnerabilities(
            @Parameter(description = "扫描任务ID") @PathVariable Long taskId) {
        try {
            List<Vulnerability> vulnerabilities = scanTaskService.getScanTaskVulnerabilities(taskId);
            return ResponseEntity.ok(ApiResponse.success("获取扫描任务漏洞成功", vulnerabilities));
        } catch (Exception e) {
            logger.error("获取扫描任务漏洞失败: {}", taskId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取扫描任务漏洞失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "删除扫描任务", description = "删除指定的扫描任务")
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteScanTask(
            @Parameter(description = "扫描任务ID") @PathVariable Long taskId) {
        try {
            scanTaskService.deleteScanTask(taskId);
            return ResponseEntity.ok(ApiResponse.success("删除扫描任务成功", null));
        } catch (Exception e) {
            logger.error("删除扫描任务失败: {}", taskId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除扫描任务失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "获取扫描统计信息", description = "获取扫描任务的统计信息")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ScanTaskService.ScanStatistics>> getScanStatistics() {
        try {
            ScanTaskService.ScanStatistics statistics = scanTaskService.getScanStatistics();
            return ResponseEntity.ok(ApiResponse.success("获取扫描统计信息成功", statistics));
        } catch (Exception e) {
            logger.error("获取扫描统计信息失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取扫描统计信息失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "批量启动扫描任务", description = "批量启动多个扫描任务")
    @PostMapping("/batch/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST')")
    public ResponseEntity<ApiResponse<String>> batchStartScanTasks(
            @RequestBody List<Long> taskIds) {
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (Long taskId : taskIds) {
                try {
                    scanTaskService.startScanTask(taskId);
                    successCount++;
                } catch (Exception e) {
                    logger.error("批量启动扫描任务失败: {}", taskId, e);
                    failCount++;
                }
            }
            
            String message = String.format("批量启动完成，成功: %d 个，失败: %d 个", successCount, failCount);
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception e) {
            logger.error("批量启动扫描任务失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量启动扫描任务失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "批量停止扫描任务", description = "批量停止多个扫描任务")
    @PostMapping("/batch/stop")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_ANALYST')")
    public ResponseEntity<ApiResponse<String>> batchStopScanTasks(
            @RequestBody List<Long> taskIds) {
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (Long taskId : taskIds) {
                try {
                    scanTaskService.stopScanTask(taskId);
                    successCount++;
                } catch (Exception e) {
                    logger.error("批量停止扫描任务失败: {}", taskId, e);
                    failCount++;
                }
            }
            
            String message = String.format("批量停止完成，成功: %d 个，失败: %d 个", successCount, failCount);
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception e) {
            logger.error("批量停止扫描任务失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量停止扫描任务失败: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "批量删除扫描任务", description = "批量删除多个扫描任务")
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> batchDeleteScanTasks(
            @RequestBody List<Long> taskIds) {
        try {
            int successCount = 0;
            int failCount = 0;
            
            for (Long taskId : taskIds) {
                try {
                    scanTaskService.deleteScanTask(taskId);
                    successCount++;
                } catch (Exception e) {
                    logger.error("批量删除扫描任务失败: {}", taskId, e);
                    failCount++;
                }
            }
            
            String message = String.format("批量删除完成，成功: %d 个，失败: %d 个", successCount, failCount);
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception e) {
            logger.error("批量删除扫描任务失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("批量删除扫描任务失败: " + e.getMessage()));
        }
    }
}
