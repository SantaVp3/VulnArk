package com.vulnark.controller;

import com.vulnark.dto.ScanTaskRequest;
import com.vulnark.dto.ScanTaskResponse;
import com.vulnark.entity.ScanResult;
import com.vulnark.entity.ScanTask;
import com.vulnark.service.ScanningService;
import com.vulnark.common.ApiResponse;
import com.vulnark.util.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

/**
 * 扫描管理控制器
 */
@Tag(name = "扫描管理", description = "漏洞扫描任务管理API")
@RestController
@RequestMapping("/scan")
@CrossOrigin(origins = "*")
public class ScanController {

    private static final Logger logger = LoggerFactory.getLogger(ScanController.class);

    @Autowired
    private ScanningService scanningService;

    /**
     * 创建扫描任务
     */
    @Operation(summary = "创建扫描任务", description = "创建新的漏洞扫描任务")
    @PostMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ScanTaskResponse>> createScanTask(
            @Valid @RequestBody ScanTaskRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            ScanTaskResponse response = scanningService.createScanTask(request, userId);
            return ResponseEntity.ok(ApiResponse.success("扫描任务创建成功", response));
        } catch (Exception e) {
            logger.error("创建扫描任务失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新扫描任务
     */
    @Operation(summary = "更新扫描任务", description = "更新指定的扫描任务信息")
    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ScanTaskResponse>> updateScanTask(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Valid @RequestBody ScanTaskRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            ScanTaskResponse response = scanningService.updateScanTask(id, request, userId);
            return ResponseEntity.ok(ApiResponse.success("扫描任务更新成功", response));
        } catch (Exception e) {
            logger.error("更新扫描任务失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除扫描任务
     */
    @Operation(summary = "删除扫描任务", description = "删除指定的扫描任务")
    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteScanTask(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            scanningService.deleteScanTask(id, userId);
            return ResponseEntity.ok(ApiResponse.success("扫描任务删除成功"));
        } catch (Exception e) {
            logger.error("删除扫描任务失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取扫描任务详情
     */
    @Operation(summary = "获取扫描任务详情", description = "根据ID获取扫描任务的详细信息")
    @GetMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ScanTaskResponse>> getScanTask(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            ScanTaskResponse response = scanningService.getScanTask(id);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            logger.error("获取扫描任务详情失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取扫描任务列表
     */
    @Operation(summary = "获取扫描任务列表", description = "分页获取扫描任务列表")
    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ScanTaskResponse>>> getScanTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdTime") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ScanTaskResponse> response = scanningService.getScanTasks(pageable);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            logger.error("获取扫描任务列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 搜索扫描任务
     */
    @Operation(summary = "搜索扫描任务", description = "根据条件搜索扫描任务")
    @GetMapping("/tasks/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ScanTaskResponse>>> searchScanTasks(
            @Parameter(description = "任务名称") @RequestParam(required = false) String name,
            @Parameter(description = "任务状态") @RequestParam(required = false) ScanTask.TaskStatus status,
            @Parameter(description = "扫描类型") @RequestParam(required = false) ScanTask.ScanType scanType,
            @Parameter(description = "扫描引擎") @RequestParam(required = false) ScanTask.ScanEngine scanEngine,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "创建者ID") @RequestParam(required = false) Long createdById,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdTime") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ScanTaskResponse> response = scanningService.searchScanTasks(
                name, status, scanType, scanEngine, projectId, createdById, pageable);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            logger.error("搜索扫描任务失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 启动扫描任务
     */
    @Operation(summary = "启动扫描任务", description = "启动指定的扫描任务")
    @PostMapping("/tasks/{id}/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> startScanTask(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            CompletableFuture<Void> future = scanningService.startScanTask(id, userId);
            return ResponseEntity.ok(ApiResponse.success("扫描任务启动成功"));
        } catch (Exception e) {
            logger.error("启动扫描任务失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 停止扫描任务
     */
    @Operation(summary = "停止扫描任务", description = "停止指定的扫描任务")
    @PostMapping("/tasks/{id}/stop")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> stopScanTask(
            @Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            scanningService.stopScanTask(id, userId);
            return ResponseEntity.ok(ApiResponse.success("扫描任务停止成功"));
        } catch (Exception e) {
            logger.error("停止扫描任务失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取扫描结果
     */
    @Operation(summary = "获取扫描结果", description = "获取指定扫描任务的结果")
    @GetMapping("/tasks/{id}/results")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ScanResult>>> getScanResults(
            @Parameter(description = "任务ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "discoveredTime") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<ScanResult> response = scanningService.getScanResults(id, pageable);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            logger.error("获取扫描结果失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取扫描引擎列表
     */
    @Operation(summary = "获取扫描引擎列表", description = "获取支持的扫描引擎列表")
    @GetMapping("/engines")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ScanTask.ScanEngine[]>> getScanEngines() {
        try {
            return ResponseEntity.ok(ApiResponse.success(ScanTask.ScanEngine.values()));
        } catch (Exception e) {
            logger.error("获取扫描引擎列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取扫描类型列表
     */
    @Operation(summary = "获取扫描类型列表", description = "获取支持的扫描类型列表")
    @GetMapping("/types")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ScanTask.ScanType[]>> getScanTypes() {
        try {
            return ResponseEntity.ok(ApiResponse.success(ScanTask.ScanType.values()));
        } catch (Exception e) {
            logger.error("获取扫描类型列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取扫描模板列表
     */
    @Operation(summary = "获取扫描模板列表", description = "获取支持的扫描模板列表")
    @GetMapping("/templates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ScanTask.ScanTemplate[]>> getScanTemplates() {
        try {
            return ResponseEntity.ok(ApiResponse.success(ScanTask.ScanTemplate.values()));
        } catch (Exception e) {
            logger.error("获取扫描模板列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取任务状态列表
     */
    @Operation(summary = "获取任务状态列表", description = "获取任务状态列表")
    @GetMapping("/statuses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ScanTask.TaskStatus[]>> getTaskStatuses() {
        try {
            return ResponseEntity.ok(ApiResponse.success(ScanTask.TaskStatus.values()));
        } catch (Exception e) {
            logger.error("获取任务状态列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
